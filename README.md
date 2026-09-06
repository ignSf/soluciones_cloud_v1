# Soluciones Cloud - Fase 1: Base Funcional

Proyecto de demostración de arquitectura Cloud compuesto por un backend en Spring Boot y un frontend modular en React + Vite.

## Estructura del Repositorio

- `demo/`: Backend REST con Spring Boot, Spring Security (modo permisivo inicial) y base de datos relacional H2.
- `frontend/`: Aplicación cliente en React con TypeScript, Vite y componentes modulares desacoplados.

## Requisitos de Ejecución

### 1. Backend (Spring Boot)
- Java 21 o superior.
- Ejecutar:
  ```bash
  cd demo
  ./mvnw spring-boot:run
  ```
  API disponible en: `http://localhost:8080/api/products`  
  Consola H2 en: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`)

### 2. Frontend (React + Vite)
- Node.js 18+ y npm.
- Ejecutar:
  ```bash
  cd frontend
  npm install
  npm run dev
  ```
  Interfaz disponible en: `http://localhost:5173`
