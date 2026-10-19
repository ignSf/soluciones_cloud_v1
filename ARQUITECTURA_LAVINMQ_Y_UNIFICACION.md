# Documento Técnico: Arquitectura de Mensajería con LavinMQ y Unificación Tri-Broker

---

## 1. Fundamentos de Colas de Mensajería y Protocolo AMQP 0-9-1

### 1.1 ¿Qué es un Message Broker y por qué se utiliza?
En una arquitectura de microservicios tradicional basada en llamadas síncronas HTTP/REST (punto a punto), el fallo o la lentitud de un servicio secundario degrada en cascada toda la plataforma. 

Un **Message Broker** es un intermediario que desacopla la comunicación en el tiempo y en el espacio:
- **Desacoplamiento temporal**: El emisor (productor) no necesita que el receptor (consumidor) esté en línea al momento del envío.
- **Asincronía y no bloqueo**: El servicio que recibe la petición del usuario procesa lo crítico (ej. guardar la orden) y delega tareas secundarias (envío, inventario, correo) a través de eventos en segundo plano.
- **Resiliencia y búfer**: Si hay picos de tráfico, las colas absorben la carga y los microservicios procesan a su propio ritmo.

### 1.2 Componentes del Protocolo AMQP 0-9-1
El estándar AMQP 0-9-1 estructura la mensajería en cuatro entidades fundamentales:

```
[Productor] ---> (Exchange) ---[Routing Key]---> [Queue] ---> [Consumidor]
                      | (en caso de fallo)
                      v
                 (Dead Letter Exchange) ---> [Dead Letter Queue (DLQ)]
```

1. **Exchange (Intercambio)**: Enrutador del broker. Recibe mensajes del productor y decide a qué cola enviarlos según su tipo:
   - **Direct Exchange** *(utilizado en nuestra solución)*: Enruta el mensaje exactamente a las colas cuyo enlace coincida con la clave (`Routing Key`).
2. **Queue (Cola)**: Búfer FIFO en memoria/disco donde esperan los mensajes hasta ser consumidos.
   - **Durable**: Si el broker se reinicia, la cola y los mensajes persistidos no se destruyen.
3. **Binding (Enlace)**: Regla de conexión que une un Exchange con una Queue bajo una `Routing Key`.
4. **Mecanismo de Confirmación (ACK / NACK)**:
   - **ACK (Acknowledge)**: El consumidor avisa que procesó el mensaje; el broker lo elimina de la cola.
   - **NACK / Reject**: El consumidor avisa que falló. Puede reencolarse o descartarse.
5. **Dead Letter Exchange (DLX) y Dead Letter Queue (DLQ)**:
   - Si un mensaje es rechazado sin reencolar (`requeue=false`) o supera el límite de reintentos, el broker lo transfiere automáticamente a la DLQ. Esto evita bucles infinitos de fallo y permite auditoría forense de mensajes corruptos.

---

## 2. LavinMQ: Naturaleza y Ventajas

### 2.1 ¿Qué es LavinMQ?
**LavinMQ** es un broker de mensajería moderno de código abierto desarrollado por **CloudAMQP**, escrito en el lenguaje de programación **Crystal**. 

### 2.2 Características Distintivas
- **100% Compatible con AMQP 0-9-1**: Utiliza el mismo protocolo binario que RabbitMQ. Cualquier cliente o biblioteca compatible (como `Spring AMQP`, `Pika` en Python o `amqplib` en Node.js) se conecta a LavinMQ sin requerir drivers propietarios.
- **Rendimiento Extremo**: Capaz de procesar más de 1 millón de mensajes por segundo con latencias de microsegundos, superando con creces la sobrecarga de la máquina virtual Erlang de RabbitMQ.
- **Uso Mínimo de Hardware**: Consume una fracción minúscula de CPU y memoria RAM.
- **Panel Web Integrado**: Expone una interfaz gráfica de administración en el puerto `1234` con métricas en tiempo real.

---

## 3. Implementación de LavinMQ en la Arquitectura

### 3.1 Infraestructura en Docker (`docker-compose.yml`)
Para permitir la convivencia pacífica entre RabbitMQ (puerto estándar 5672) y LavinMQ, se aisló LavinMQ mapeando sus puertos:

```yaml
  lavinmq:
    image: cloudamqp/lavinmq:latest
    container_name: lavinmq
    ports:
      - "5673:5672"   # Puerto AMQP: el host usa 5673, dentro del contenedor es 5672
      - "1234:1234"   # Panel de Administración Web
    healthcheck:
      test: ["CMD", "lavinmqctl", "status"]
      interval: 15s
      timeout: 10s
      retries: 5
    restart: unless-stopped
```

---

### 3.2 Implementación en el Productor (`demo`)

#### A. Conexión Dual AMQP (`LavinMqConfig.java`)
Por defecto, Spring Boot solo crea una conexión a RabbitMQ. Para conectarse simultáneamente a **LavinMQ**, se implementó una segunda fábrica de conexiones explícita:

```java
@Configuration
public class LavinMqConfig {

    public static final String SHIPPING_EXCHANGE = "shipping.exchange";
    public static final String SHIPPING_CREATED_KEY = "shipping.created";

    @Bean
    public ConnectionFactory lavinMqConnectionFactory(
            @Value("${app.lavinmq.host:localhost}") String host,
            @Value("${app.lavinmq.port:5673}") int port,
            @Value("${app.lavinmq.username:guest}") String username,
            @Value("${app.lavinmq.password:guest}") String password) {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory;
    }

    @Bean(name = "lavinMqRabbitTemplate")
    public RabbitTemplate lavinMqRabbitTemplate(ConnectionFactory lavinMqConnectionFactory) {
        RabbitTemplate template = new RabbitTemplate(lavinMqConnectionFactory);
        template.setMessageConverter(new JacksonJsonMessageConverter());
        return template;
    }
}
```

*Nota técnica:* En `RabbitConfig.java` se etiquetó la plantilla de RabbitMQ como `@Primary`, garantizando que cada componente inyecte su template correspondiente sin ambigüedad.

#### B. Publicador Resiliente (`LavinMqShippingPublisher.java`)
Al emitir el evento, se captura `AmqpException`. Si LavinMQ estuviera reiniciándose o fuera de línea temporalmente, **el pedido del cliente no se cancela**:

```java
@Component
public class LavinMqShippingPublisher {
    // Inyección calificada al template de LavinMQ
    public LavinMqShippingPublisher(@Qualifier("lavinMqRabbitTemplate") RabbitTemplate template, ...) { ... }

    public void publicarDespachoPedido(Order order) {
        ShippingOrderEvent evento = new ShippingOrderEvent(...);
        try {
            lavinMqRabbitTemplate.convertAndSend(
                LavinMqConfig.SHIPPING_EXCHANGE,
                LavinMqConfig.SHIPPING_CREATED_KEY,
                evento
            );
        } catch (AmqpException e) {
            log.error("LavinMQ ERROR al publicar despacho pedidoId={}: {}", order.getId(), e.getMessage());
        }
    }
}
```

---

### 3.3 Implementación en el Consumidor (`shipping-service`)

#### A. Topología AMQP en LavinMQ (`LavinMqConfig.java`)
El microservicio declara de forma declarativa su intercambio, cola principal y Dead Letter Queue:

```java
@Configuration
public class LavinMqConfig {
    public static final String SHIPPING_EXCHANGE = "shipping.exchange";
    public static final String SHIPPING_CREATED_KEY = "shipping.created";
    public static final String SHIPPING_QUEUE = "shipping.order-created.queue";
    public static final String DLX = "shipping.dlx";
    public static final String DLQ = "shipping.order-created.dlq";
    public static final String DLQ_KEY = "shipping.order-created.dead";

    @Bean
    public Queue shippingQueue() {
        return QueueBuilder.durable(SHIPPING_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_KEY)
                .build();
    }

    @Bean
    public Binding shippingBinding(...) {
        return BindingBuilder.bind(shippingQueue()).to(shippingExchange()).with(SHIPPING_CREATED_KEY);
    }
}
```

#### B. Consumidor de Eventos (`ShippingConsumer.java`)
Utiliza `@RabbitListener` apuntando a la cola declarada en LavinMQ. Si ocurre una excepción grave de negocio, arroja `AmqpRejectAndDontRequeueException`, indicándole a LavinMQ que desvíe el mensaje directamente a la DLQ:

```java
@Component
public class ShippingConsumer {
    @RabbitListener(queues = LavinMqConfig.SHIPPING_QUEUE)
    public void handleOrderCreated(ShippingOrderEvent event) {
        try {
            Shipment shipment = shippingService.processOrderShipping(event);
            log.info("LavinMQ ÉXITO: despacho #{} creado con tracking {}", shipment.getId(), shipment.getTrackingNumber());
        } catch (Exception e) {
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}
```

#### C. Dominio Logístico y Generación de Tracking (`ShippingService.java`)
- **Idempotencia**: Valida si el `orderId` ya fue procesado para evitar guías de despacho duplicadas ante reintentos de red.
- **Generación de Tracking**: Construye identificadores únicos con formato `TRK-CL-XXXXXXXX`.
- **Estados Logísticos**: `PREPARING` -> `IN_TRANSIT` -> `DELIVERED`.
- **Persistencia**: Tabla `shipments` en base de datos H2 en memoria / PostgreSQL.

#### D. API REST de Consulta y Rastreo (`ShippingController.java`)
- `GET /api/shipping`: Listado general de envíos.
- `GET /api/shipping/order/{orderId}`: Consulta por número de orden.
- `GET /api/shipping/track/{trackingNumber}`: Rastreo público de guía.
- `PUT /api/shipping/{id}/status`: Actualización de estado logístico.

---

## 4. La Estrategia de Unificación Tri-Broker (Resolución de Conflictos)

### 4.1 El Conflicto Original entre Ramas
- **Rama Compañero 1**: Implementó `inventory-service` conectado a RabbitMQ. En `OrderService.java` invocaba:
  ```java
  orderEventPublisher.publishOrderCreated(saved);
  ```
- **Rama Compañero 2 (`rama-entraid`)**: Implementó `notificaciones-service` conectado a AWS SQS. Al implementar su cambio, **borró** la llamada de RabbitMQ y la reemplazó por:
  ```java
  sqsNotificacionPublisher.publicarPedidoCreado(saved);
  ```
  *Consecuencia del conflicto:* Si se integraba la rama 2, el microservicio de inventario de la rama 1 dejaba de recibir eventos y quedaba roto.

### 4.2 La Solución Unificada
En lugar de elegir una rama o descartar el trabajo de alguno de sus compañeros, se diseñó un **Orquestador Concurrente** en `OrderService.java`:

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    
    // Bus 1: RabbitMQ (Compañero 1: Inventario)
    private final OrderEventPublisher orderEventPublisher;

    // Bus 2: Amazon SQS (Compañero 2: Notificaciones y Correo)
    private final SqsNotificacionPublisher sqsNotificacionPublisher;

    // Bus 3: LavinMQ (Su Servicio: Despacho y Tracking)
    private final LavinMqShippingPublisher lavinMqShippingPublisher;

    public Order createOrder(Order order) {
        // Paso transaccional síncrono obligatorio
        Order saved = orderRepository.save(order);

        // Paso asíncrono 1: RabbitMQ -> Descuenta stock
        orderEventPublisher.publishOrderCreated(saved);

        // Paso asíncrono 2: AWS SQS -> Encola envío de correo
        sqsNotificacionPublisher.publicarPedidoCreado(saved);

        // Paso asíncrono 3: LavinMQ -> Genera despacho y tracking
        lavinMqShippingPublisher.publicarDespachoPedido(saved);

        return saved;
    }
}
```

### 4.3 Matriz de Aislamiento y Resiliencia
Cada uno de los tres publicadores implementa bloques `try/catch` independientes sobre excepciones de transporte (`AmqpException`, `AwsServiceException`). 

Si cualquiera de los tres brokers está caído, saturado o sin conexión:
1. El error se registra en el log del backend con nivel `ERROR`.
2. Los otros dos brokers continúan operando con total normalidad.
3. El cliente que realiza la compra recibe un HTTP `200 OK` con su orden confirmada.

---

## 5. Resumen de Puertos y Servicios

| Microservicio / Broker | Puerto Local | Tecnología / Rol |
|---|---|---|
| **`frontend`** | `:5173` | React 19 + TypeScript + Vite |
| **`demo`** | `:8080` | Spring Boot (Catálogo, Órdenes, Emisor Tri-Broker) |
| **`inventory-service`** | `:8081` | Spring Boot + **RabbitMQ** (Stock) |
| **`notificaciones-service`** | `:8082` | Spring Boot + **Amazon SQS** (Avisos) |
| **`shipping-service`** | `:8083` | Spring Boot + **LavinMQ** (Envíos y Tracking) |
| **RabbitMQ Management** | `:15672` | Panel Web RabbitMQ (`guest` / `guest`) |
| **LavinMQ Management** | `:1234` | Panel Web LavinMQ (`guest` / `guest`) |
| **RabbitMQ AMQP** | `:5672` | Protocolo AMQP Broker 1 |
| **LavinMQ AMQP** | `:5673` | Protocolo AMQP Broker 3 |

---

## 6. Diagrama de Secuencia y Flujo de Datos Tri-Broker

A continuación se detalla el ciclo de vida completo cuando un usuario realiza una compra en la plataforma:

```
[Usuario / Frontend]
        |
        | 1. POST /api/orders
        v
 [demo / OrderService]
        |
        | 2. Guarda Orden en BD local (transaccional)
        |
        +-----------------------+-------------------------+
        |                       |                         |
        | 3a. AMQP (5672)       | 3b. HTTPS AWS SDK       | 3c. AMQP (5673)
        v                       v                         v
   [RabbitMQ]               [Amazon SQS]              [LavinMQ]
        |                       |                         |
        | Encola evento         | Encola mensaje          | Encola evento
        v                       v                         v
 [inventory-service]    [notificaciones-service]   [shipping-service]
  (Puerto :8081)          (Puerto :8082)            (Puerto :8083)
        |                       |                         |
        v                       v                         v
 Descuenta stock         Envía correo / aviso      Crea guía y Tracking
 (OrderConsumer)         (NotificacionWorker)     (TRK-CL-XXXXXXXX)
```

---

## 7. Tabla Comparativa: RabbitMQ vs Amazon SQS vs LavinMQ

| Dimensión | RabbitMQ | Amazon SQS | LavinMQ |
|---|---|---|---|
| **Desarrollador / Origen** | VMware / Pivotal | Amazon Web Services | CloudAMQP (84codes) |
| **Lenguaje de Programación** | Erlang (BEAM VM) | Propietario (C++/Java) | **Crystal** (compilado nativo LLVM) |
| **Protocolo de Comunicación** | AMQP 0-9-1 / MQTT / STOMP | HTTP/HTTPS (REST API) | **AMQP 0-9-1** |
| **Modelo de Consumo** | Push continuo (`@RabbitListener`) | Long Polling (`ReceiveMessage`) | Push continuo (`@RabbitListener`) |
| **Throughput / Rendimiento** | ~30.000 - 50.000 msg/s | Escalable pero alta latencia HTTP | **> 1.000.000 msg/s** (baja latencia) |
| **Consumo de Recursos** | Alto (requiere 512MB - 1GB RAM) | N/A (Serverless administrado) | **Ultra-ligero** (< 50MB RAM) |
| **Enrutamiento Avanzado** | Direct, Topic, Fanout, Headers | Cola simple o FIFO (básico) | Direct, Topic, Fanout, Headers |
| **Dead Letter Queue (DLQ)** | Soportado nativo (vía DLX) | Soportado nativo (Redrive Policy) | **Soportado nativo (vía DLX)** |
| **Despliegue Local / Docker** | Imagen Docker oficial | Requiere LocalStack o AWS real | **Imagen Docker oficial (CloudAMQP)** |

---

## 8. Auditoría de Integridad: Proyectos de Compañeros

Para garantizar total transparencia y respeto al trabajo en equipo, se auditó exhaustivamente el estado de los proyectos de ambos compañeros tras la unificación:

### A. Proyecto de Compañero 1 (`inventory-service`)
- **Archivos de código Java modificados:** **0** (Cero).
- **Archivos de configuración (`application.properties`, `pom.xml`) modificados:** **0** (Cero).
- **Lógica de negocio:** Mantiene intacto su modelo de datos `InventoryItem`, su repositorio, su servicio de control de stock y su consumidor AMQP `OrderConsumer.java` escuchando en RabbitMQ.

### B. Proyecto de Compañero 2 (`notificaciones-service`)
- **Archivos de código Java modificados:** **0** (Cero).
- **Archivos de configuración (`application.properties`, `pom.xml`) modificados:** **0** (Cero).
- **Lógica de negocio:** Mantiene intacto su consumidor `NotificacionWorker.java`, su servicio `NotificacionService.java`, su modelo y su endpoint de monitoreo de colas.
- **Único archivo añadido:** Se incorporó un `Dockerfile` en la raíz de su carpeta exclusivamente para que `docker compose` pudiera compilarlo y levantarlo como contenedor. Su código fuente no fue alterado en ninguna sola línea.

### C. Dónde se realizó la Unificación
El punto de convergencia y resolución del conflicto se concentró exclusivamente en el **Backend Central (`demo`)**:
1. Se recuperó la llamada de RabbitMQ que había sido eliminada accidentalmente en `rama-entraid`.
2. Se mantuvo la llamada a SQS del compañero 2.
3. Se integró la nueva llamada a LavinMQ para el servicio de envíos.
4. Se aislaron los 3 emisores con manejo de excepciones independiente para que ninguna falla en un broker afecte a los otros dos.
