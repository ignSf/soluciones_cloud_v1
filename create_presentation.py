import os
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN
from pptx.enum.shapes import MSO_SHAPE

def build_presentation():
    prs = Presentation()
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    blank_layout = prs.slide_layouts[6]

    # Paleta de Colores de Alta Fidelidad
    C_BG = RGBColor(11, 19, 43)        # Navy Profundo (#0B132B)
    C_CARD = RGBColor(24, 34, 58)      # Slate Ejecutivo (#18223A)
    C_BORDER = RGBColor(48, 65, 98)    # Borde Sutil (#304162)
    C_CYAN = RGBColor(0, 180, 216)     # Cyan Acento (#00B4D8)
    C_MINT = RGBColor(6, 214, 160)     # Menta / Éxito (#06D6A0)
    C_AMBER = RGBColor(255, 183, 3)    # Ámbar / Alerta (#FFB703)
    C_WHITE = RGBColor(255, 255, 255)  # Blanco Puro
    C_SILVER = RGBColor(226, 232, 240) # Slate Claro / Lectura (#E2E8F0)
    C_MUTED = RGBColor(148, 163, 184)  # Slate Secundario (#94A3B8)

    FONT_FAMILY = "Segoe UI"

    def apply_base(slide):
        """Fondo general limpio con acento superior sutil y pie de página."""
        # Fondo oscuro
        bg = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, Inches(13.333), Inches(7.5))
        bg.fill.solid()
        bg.fill.fore_color.rgb = C_BG
        bg.line.fill.background()

        # Barra tecnológica superior
        top_bar = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, 0, 0, Inches(13.333), Inches(0.06))
        top_bar.fill.solid()
        top_bar.fill.fore_color.rgb = C_CYAN
        top_bar.line.fill.background()

        # Footer minimalista
        tb_foot = slide.shapes.add_textbox(Inches(0.8), Inches(7.05), Inches(11.733), Inches(0.35))
        tf_foot = tb_foot.text_frame
        tf_foot.word_wrap = True
        p_foot = tf_foot.paragraphs[0]
        p_foot.text = "Soluciones Cloud v1 • DSY1107 • Duoc UC • Arquitectura Cloud Native Segura"
        p_foot.font.size = Pt(9.5)
        p_foot.font.color.rgb = C_MUTED
        p_foot.font.name = FONT_FAMILY

    def add_header(slide, title, category, slide_num):
        """Encabezado espacioso con badge de sección, título de gran jerarquía y contador."""
        # Badge de Categoría
        badge = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(0.42), Inches(2.8), Inches(0.32))
        badge.fill.solid()
        badge.fill.fore_color.rgb = RGBColor(16, 38, 70)
        badge.line.color.rgb = C_CYAN
        badge.line.width = Pt(1)
        tf_b = badge.text_frame
        p_b = tf_b.paragraphs[0]
        p_b.text = category.upper()
        p_b.font.size = Pt(9.5)
        p_b.font.bold = True
        p_b.font.color.rgb = C_CYAN
        p_b.font.name = FONT_FAMILY
        p_b.alignment = PP_ALIGN.CENTER

        # Título Principal (Grande, directo y legible)
        title_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.82), Inches(10.2), Inches(0.75))
        tf_t = title_box.text_frame
        tf_t.word_wrap = True
        p_t = tf_t.paragraphs[0]
        p_t.text = title
        p_t.font.size = Pt(26)
        p_t.font.bold = True
        p_t.font.color.rgb = C_WHITE
        p_t.font.name = FONT_FAMILY

        # Indicador de Diapositiva
        num_box = slide.shapes.add_textbox(Inches(11.3), Inches(0.5), Inches(1.2), Inches(0.45))
        tf_n = num_box.text_frame
        p_n = tf_n.paragraphs[0]
        p_n.text = f"{slide_num:02d} / 10"
        p_n.font.size = Pt(13)
        p_n.font.bold = True
        p_n.font.color.rgb = C_CYAN
        p_n.font.name = FONT_FAMILY
        p_n.alignment = PP_ALIGN.RIGHT

        # Línea divisoria muy tenue
        sep = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(1.65), Inches(11.733), Inches(0.015))
        sep.fill.solid()
        sep.fill.fore_color.rgb = RGBColor(38, 52, 78)
        sep.line.fill.background()

    def add_card(slide, left, top, width, height, title="", title_color=C_CYAN, bg_color=C_CARD, border_color=C_BORDER):
        """Tarjeta limpia con respiro y bordes elegantes."""
        card = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(left), Inches(top), Inches(width), Inches(height))
        card.fill.solid()
        card.fill.fore_color.rgb = bg_color
        card.line.color.rgb = border_color
        card.line.width = Pt(1)

        if title:
            tb = slide.shapes.add_textbox(Inches(left + 0.25), Inches(top + 0.2), Inches(width - 0.5), Inches(0.5))
            tf = tb.text_frame
            tf.word_wrap = True
            p = tf.paragraphs[0]
            p.text = title
            p.font.size = Pt(16)
            p.font.bold = True
            p.font.color.rgb = title_color
            p.font.name = FONT_FAMILY
        return card

    def add_notes(slide, notes_text):
        """Incrusta las notas completas para el orador en PowerPoint."""
        notes_slide = slide.notes_slide
        tf = notes_slide.notes_text_frame
        tf.text = notes_text

    # =========================================================================
    # DIAPOSITIVA 1: PORTADA EJECUTIVA
    # =========================================================================
    s1 = prs.slides.add_slide(blank_layout)
    apply_base(s1)

    # Header de contexto
    tb1_badge = s1.shapes.add_textbox(Inches(1.0), Inches(1.0), Inches(11.333), Inches(0.4))
    tf1_b = tb1_badge.text_frame
    p1_b = tf1_b.paragraphs[0]
    p1_b.text = "EVALUACIÓN PARCIAL N° 1 • DSY1107 • DUOC UC"
    p1_b.font.size = Pt(13)
    p1_b.font.bold = True
    p1_b.font.color.rgb = C_CYAN
    p1_b.font.name = FONT_FAMILY

    # Título Principal
    tb1_title = s1.shapes.add_textbox(Inches(1.0), Inches(1.5), Inches(11.333), Inches(1.8))
    tf1_t = tb1_title.text_frame
    tf1_t.word_wrap = True
    p1_t = tf1_t.paragraphs[0]
    p1_t.text = "Soluciones Cloud v1"
    p1_t.font.size = Pt(44)
    p1_t.font.bold = True
    p1_t.font.color.rgb = C_WHITE
    p1_t.font.name = FONT_FAMILY

    p1_sub = tf1_t.add_paragraph()
    p1_sub.text = "Arquitectura Cloud Native Segura: Gestión de Productos con IDaaS (AWS Cognito), API Gateway, Spring Boot y React"
    p1_sub.font.size = Pt(17)
    p1_sub.font.color.rgb = C_SILVER
    p1_sub.font.name = FONT_FAMILY
    p1_sub.space_before = Pt(8)

    # Badges Tecnológicos Concisos
    tech_chips = [
        "React + Vite (Vercel)",
        "AWS Cognito (PKCE)",
        "Amazon API Gateway",
        "Spring Boot 3 (Render)",
        "PostgreSQL (Supabase)"
    ]
    for i, chip in enumerate(tech_chips):
        x = 1.0 + (i * 2.3)
        pill = s1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(x), Inches(3.9), Inches(2.15), Inches(0.45))
        pill.fill.solid()
        pill.fill.fore_color.rgb = RGBColor(16, 36, 68)
        pill.line.color.rgb = C_CYAN
        pill.line.width = Pt(1)
        tf_p = pill.text_frame
        p_p = tf_p.paragraphs[0]
        p_p.text = chip
        p_p.font.size = Pt(10.5)
        p_p.font.bold = True
        p_p.font.color.rgb = C_WHITE
        p_p.font.name = FONT_FAMILY
        p_p.alignment = PP_ALIGN.CENTER

    # Tarjeta de Integrantes & Repositorio
    add_card(s1, 1.0, 4.9, 11.333, 1.6, "Equipo de Ingeniería & Enlaces de Producción", C_CYAN)
    tb_meta = s1.shapes.add_textbox(Inches(1.25), Inches(5.5), Inches(10.8), Inches(0.9))
    tf_m = tb_meta.text_frame
    tf_m.word_wrap = True

    pm1 = tf_m.paragraphs[0]
    pm1.text = "Integrantes: Ignacio Salazar • Cristofer Cifuentes"
    pm1.font.size = Pt(14)
    pm1.font.bold = True
    pm1.font.color.rgb = C_WHITE
    pm1.font.name = FONT_FAMILY

    pm2 = tf_m.add_paragraph()
    pm2.text = "Producción: Frontend (Vercel) • Backend BFF (Render) • API Gateway (AWS us-east-1)"
    pm2.font.size = Pt(12)
    pm2.font.color.rgb = C_SILVER
    pm2.font.name = FONT_FAMILY
    pm2.space_before = Pt(4)

    add_notes(s1, 
        "DISCURSO DE APERTURA:\n\n"
        "• Buenos días estimado profesor y evaluadores.\n"
        "• Hoy presentamos 'Soluciones Cloud v1', una plataforma web de catálogo y gestión de inventario construida 100% bajo el paradigma Cloud Native.\n"
        "• El propósito central de este proyecto no fue solo crear una aplicación que funcione, sino demostrar una arquitectura desacoplada, resiliente y de seguridad bancaria: cero manejo de contraseñas locales, delegación en AWS Cognito y defensa perimetral con Amazon API Gateway."
    )

    # =========================================================================
    # DIAPOSITIVA 2: EL DESAFÍO - MONOLITO VS CLOUD NATIVE
    # =========================================================================
    s2 = prs.slides.add_slide(blank_layout)
    apply_base(s2)
    add_header(s2, "El Reto: Monolito Tradicional vs. Arquitectura Cloud Native", "01 • Fundamentos", 2)

    # Columna 1: Monolito Tradicional (Problemas)
    add_card(s2, 0.8, 2.0, 5.7, 4.7, "Riesgos del Enfoque Tradicional", C_AMBER, bg_color=RGBColor(32, 24, 38))
    tb_m = s2.shapes.add_textbox(Inches(1.1), Inches(2.75), Inches(5.1), Inches(3.6))
    tf_m = tb_m.text_frame
    tf_m.word_wrap = True

    m_points = [
        ("Gestión de Contraseñas Locales", "Guardar hashes en BD propia expone al negocio a filtraciones críticas y ataques de fuerza bruta."),
        ("Acoplamiento y Sesiones en Memoria", "Sesiones HTTP en el servidor impiden escalar horizontalmente sin réplicas complejas."),
        ("Punto Único de Falla (SPOF)", "Si la base de datos o el servidor fallan, todo el ecosistema colapsa sin aislamiento.")
    ]
    for i, (head, desc) in enumerate(m_points):
        p = tf_m.paragraphs[0] if i == 0 else tf_m.add_paragraph()
        p.text = f"✖  {head}"
        p.font.size = Pt(14)
        p.font.bold = True
        p.font.color.rgb = C_AMBER
        p.font.name = FONT_FAMILY
        if i > 0: p.space_before = Pt(12)

        pd = tf_m.add_paragraph()
        pd.text = desc
        pd.font.size = Pt(12)
        pd.font.color.rgb = C_SILVER
        pd.font.name = FONT_FAMILY
        pd.space_before = Pt(2)

    # Columna 2: Solución Cloud Native (Ventajas)
    add_card(s2, 6.8, 2.0, 5.7, 4.7, "Nuestra Solución: Cloud Native & Zero Trust", C_MINT, bg_color=RGBColor(20, 38, 52))
    tb_c = s2.shapes.add_textbox(Inches(7.1), Inches(2.75), Inches(5.1), Inches(3.6))
    tf_c = tb_c.text_frame
    tf_c.word_wrap = True

    c_points = [
        ("Identidad Delegada (IDaaS)", "AWS Cognito asume el 100% de la responsabilidad de usuarios, passwords, MFA y tokens."),
        ("Arquitectura 100% Stateless (JWT)", "El backend no almacena estados ni sesiones; cada petición valida asimétricamente su token."),
        ("Desacoplamiento Multi-Cloud Total", "Servicios independientes en Vercel, AWS, Render y Supabase con máxima resiliencia.")
    ]
    for i, (head, desc) in enumerate(c_points):
        p = tf_c.paragraphs[0] if i == 0 else tf_c.add_paragraph()
        p.text = f"✔  {head}"
        p.font.size = Pt(14)
        p.font.bold = True
        p.font.color.rgb = C_MINT
        p.font.name = FONT_FAMILY
        if i > 0: p.space_before = Pt(12)

        pd = tf_c.add_paragraph()
        pd.text = desc
        pd.font.size = Pt(12)
        pd.font.color.rgb = C_SILVER
        pd.font.name = FONT_FAMILY
        pd.space_before = Pt(2)

    add_notes(s2,
        "PUNTOS CLAVE PARA EL DISCURSO:\n\n"
        "• Al diseñar este sistema nos preguntamos: ¿Por qué seguir guardando contraseñas en bases de datos locales en pleno 2025/2026?\n"
        "• Un monolito tradicional gestiona sesiones en memoria y almacena contraseñas, convirtiéndose en un blanco fácil para vulnerabilidades OWASP.\n"
        "• Nuestra solución adopta la filosofía Zero Trust: delegamos la identidad en un proveedor bancario (AWS Cognito), eliminamos el estado del backend mediante tokens JWT y distribuimos la carga en la nube."
    )

    # =========================================================================
    # DIAPOSITIVA 3: TOPOLOGÍA ARQUITECTÓNICA
    # =========================================================================
    s3 = prs.slides.add_slide(blank_layout)
    apply_base(s3)
    add_header(s3, "Topología del Sistema: Ecosistema Multi-Cloud Desacoplado", "02 • Arquitectura", 3)

    tiers = [
        ("1. Frontend SPA (Vercel)", "React 19 + TypeScript + Vite", "Aplicación estática de alto rendimiento. Acceso público al catálogo y orquestación de autenticación.", C_CYAN),
        ("2. Identidad IDaaS (AWS Cognito)", "User Pool + Hosted UI + PKCE", "Gestor autoritativo de identidad. Emite tokens criptográficos JWT (IdToken, AccessToken).", C_MINT),
        ("3. Perímetro & BFF (AWS API Gateway + Render)", "API Gateway + Spring Boot 3", "Puerta de enlace perimetral con filtrado de CORS y Resource Server en Java 17.", C_WHITE),
        ("4. Persistencia Cloud (Supabase)", "PostgreSQL 15+ & Session Pooler", "Base de datos relacional administrada conectada mediante pool transaccional seguro (puerto 5432).", C_AMBER)
    ]

    for i, (title, stack, desc, color) in enumerate(tiers):
        y = 1.95 + (i * 1.22)
        add_card(s3, 0.8, y, 11.733, 1.08, title, color, bg_color=RGBColor(20, 30, 52))
        
        tb = s3.shapes.add_textbox(Inches(1.05), Inches(y + 0.45), Inches(11.2), Inches(0.55))
        tf = tb.text_frame
        tf.word_wrap = True
        
        p1 = tf.paragraphs[0]
        p1.text = f"Tecnología: {stack}  |  Rol: {desc}"
        p1.font.size = Pt(12)
        p1.font.color.rgb = C_SILVER
        p1.font.name = FONT_FAMILY

    add_notes(s3,
        "PUNTOS CLAVE PARA EL DISCURSO:\n\n"
        "• Esta lámina ilustra las 4 capas de nuestra topología.\n"
        "• Capa 1: El frontend en Vercel es ligero y rápido; cualquiera puede ver el catálogo sin login.\n"
        "• Capa 2: Si el usuario desea gestionar productos, interactúa con AWS Cognito mediante Hosted UI.\n"
        "• Capa 3: El tráfico protegido viaja por Amazon API Gateway hacia nuestro backend en Render.\n"
        "• Capa 4: Los datos descansan de forma aislada en Supabase PostgreSQL. Cada pieza tiene una única responsabilidad bien definida."
    )

    # =========================================================================
    # DIAPOSITIVA 4: IDENTIDAD FEDERADA - AWS COGNITO + PKCE
    # =========================================================================
    s4 = prs.slides.add_slide(blank_layout)
    apply_base(s4)
    add_header(s4, "Gestión de Identidad: AWS Cognito y Protocolo PKCE", "03 • Seguridad IDaaS", 4)

    cog_cards = [
        ("Cero Contraseñas en el Sistema", [
            "Nuestra aplicación ni el servidor ven jamás las credenciales del usuario.",
            "Eliminamos el riesgo de filtraciones y cumplimos normativas internacionales.",
            "Validación automática por correo con códigos seguros de 6 dígitos."
        ], 0.8, C_CYAN),
        ("Hosted UI Oficial de AWS", [
            "Interfaz de inicio de sesión alojada en la infraestructura global de Amazon.",
            "Protección nativa contra ataques de fuerza bruta, bots y credential stuffing.",
            "Aislamiento absoluto entre la interfaz del usuario y los servidores de backend."
        ], 4.8, C_MINT),
        ("Flujo Criptográfico PKCE", [
            "Proof Key for Code Exchange (RFC 7636) diseñado para SPAs públicas.",
            "Genera code_verifier y code_challenge al vuelo para evitar intercepciones.",
            "Garantiza que sólo quien solicitó el login pueda canjear los tokens JWT."
        ], 8.8, C_AMBER)
    ]

    for title, items, x, color in cog_cards:
        add_card(s4, x, 2.0, 3.7, 4.7, title, color)
        tb = s4.shapes.add_textbox(Inches(x + 0.2), Inches(2.75), Inches(3.3), Inches(3.7))
        tf = tb.text_frame
        tf.word_wrap = True
        for idx, item in enumerate(items):
            p = tf.paragraphs[0] if idx == 0 else tf.add_paragraph()
            p.text = f"•  {item}"
            p.font.size = Pt(13)
            p.font.color.rgb = C_SILVER
            p.font.name = FONT_FAMILY
            if idx > 0: p.space_before = Pt(14)

    add_notes(s4,
        "PUNTOS CLAVE PARA EL DISCURSO (DEFENSA TÉCNICA):\n\n"
        "• Pregunta típica de examen: ¿Por qué usan PKCE en lugar del flujo OAuth clásico?\n"
        "• Respuesta: Porque React es una SPA (cliente público). En el navegador el código JavaScript es visible para cualquiera; no podemos esconder un 'Client Secret'.\n"
        "• PKCE soluciona esto generando un secreto temporal (code_verifier) y enviando su huella matemática (code_challenge) a Amazon. Cuando Amazon responde, el navegador demuestra que tiene el secreto original. Esto neutraliza los ataques de hombre en el medio (MITM)."
    )

    # =========================================================================
    # DIAPOSITIVA 5: DEFENSA EN PROFUNDIDAD - EL DOBLE FILTRO
    # =========================================================================
    s5 = prs.slides.add_slide(blank_layout)
    apply_base(s5)
    add_header(s5, "Defensa en Profundidad: Doble Filtro de Seguridad", "04 • Protección de Red", 5)

    # Filtro 1: Perímetro
    add_card(s5, 0.8, 2.0, 5.7, 4.7, "1° Filtro: Perímetro (Amazon API Gateway)", C_CYAN)
    tb_f1 = s5.shapes.add_textbox(Inches(1.1), Inches(2.8), Inches(5.1), Inches(3.6))
    tf_f1 = tb_f1.text_frame
    tf_f1.word_wrap = True

    f1_items = [
        ("Cognito Authorizer en el Borde", "El Gateway intercepta la cabecera Authorization antes de reenviar el tráfico al servidor."),
        ("Bloqueo Inmediato (HTTP 401)", "Peticiones con tokens vencidos o fraudulentos son rechazadas en el perímetro."),
        ("Ahorro de Cómputo y Resiliencia", "El backend en Render nunca procesa tráfico malicioso, ahorrando memoria y CPU.")
    ]
    for idx, (title, desc) in enumerate(f1_items):
        p = tf_f1.paragraphs[0] if idx == 0 else tf_f1.add_paragraph()
        p.text = f"🛡️  {title}"
        p.font.size = Pt(14)
        p.font.bold = True
        p.font.color.rgb = C_CYAN
        p.font.name = FONT_FAMILY
        if idx > 0: p.space_before = Pt(12)

        pd = tf_f1.add_paragraph()
        pd.text = desc
        pd.font.size = Pt(12)
        pd.font.color.rgb = C_SILVER
        pd.font.name = FONT_FAMILY
        pd.space_before = Pt(2)

    # Filtro 2: Backend
    add_card(s5, 6.8, 2.0, 5.7, 4.7, "2° Filtro: Backend (Spring Security 6)", C_MINT)
    tb_f2 = s5.shapes.add_textbox(Inches(7.1), Inches(2.8), Inches(5.1), Inches(3.6))
    tf_f2 = tb_f2.text_frame
    tf_f2.word_wrap = True

    f2_items = [
        ("Resource Server Criptográfico", "Spring Boot valida asimétricamente (RS256) la firma digital del token."),
        ("Verificación con JWKS de AWS", "Descarga claves públicas directas desde Amazon; sin contraseñas compartidas."),
        ("Extracción de Identidad del Usuario", "Asocia cada acción al correo del claim 'email' garantizando no repudio.")
    ]
    for idx, (title, desc) in enumerate(f2_items):
        p = tf_f2.paragraphs[0] if idx == 0 else tf_f2.add_paragraph()
        p.text = f"🔒  {title}"
        p.font.size = Pt(14)
        p.font.bold = True
        p.font.color.rgb = C_MINT
        p.font.name = FONT_FAMILY
        if idx > 0: p.space_before = Pt(12)

        pd = tf_f2.add_paragraph()
        pd.text = desc
        pd.font.size = Pt(12)
        pd.font.color.rgb = C_SILVER
        pd.font.name = FONT_FAMILY
        pd.space_before = Pt(2)

    add_notes(s5,
        "PUNTOS CLAVE PARA EL DISCURSO:\n\n"
        "• Destacar el concepto de 'Defensa en Profundidad' (Defense in Depth).\n"
        "• No dependemos de un único punto de seguridad. Tenemos dos aduanas:\n"
        "  1. La aduana perimetral en Amazon API Gateway, que frena ataques o peticiones no autenticadas en la puerta de entrada sin molestar al servidor.\n"
        "  2. La aduana interna en Spring Security, que verifica criptográficamente que el token pertenezca a nuestro User Pool exacto y extrae la identidad del usuario.\n"
        "• Esto protege la infraestructura contra denegación de servicio y ahorra costos."
    )

    # =========================================================================
    # DIAPOSITIVA 6: ANATOMÍA DEL TOKEN JWT (RFC 7519)
    # =========================================================================
    s6 = prs.slides.add_slide(blank_layout)
    apply_base(s6)
    add_header(s6, "Anatomía del Token JWT: El Pasaporte Digital", "05 • Criptografía JWT", 6)

    jwt_cols = [
        ("1. Header (Encabezado)", [
            "Algoritmo: RS256 (Criptografía asimétrica)",
            "Tipo de Token: JWT",
            "Key ID (kid): Identifica la clave pública en el JWKS"
        ], 0.8, C_CYAN),
        ("2. Payload (Declaraciones)", [
            "sub: Identificador único del usuario (UUID)",
            "email: Correo electrónico verificado",
            "iss: Emisor oficial (AWS Cognito Pool)",
            "exp: Timestamp exacto de expiración"
        ], 4.8, C_MINT),
        ("3. Signature (Firma Digital)", [
            "Generada con la clave privada de Amazon",
            "Garantiza integridad matemática absoluta",
            "Si se altera un solo bit, la firma queda invalidada"
        ], 8.8, C_AMBER)
    ]

    for title, items, x, color in jwt_cols:
        add_card(s6, x, 2.0, 3.7, 3.4, title, color)
        tb = s6.shapes.add_textbox(Inches(x + 0.2), Inches(2.65), Inches(3.3), Inches(2.5))
        tf = tb.text_frame
        tf.word_wrap = True
        for idx, item in enumerate(items):
            p = tf.paragraphs[0] if idx == 0 else tf.add_paragraph()
            p.text = f"•  {item}"
            p.font.size = Pt(12.5)
            p.font.color.rgb = C_SILVER
            p.font.name = FONT_FAMILY
            if idx > 0: p.space_before = Pt(8)

    # Franja conceptual inferior
    add_card(s6, 0.8, 5.65, 11.733, 1.05, "La Analogía del Pasaporte Digital", C_WHITE, bg_color=RGBColor(20, 32, 60))
    tb_pas = s6.shapes.add_textbox(Inches(1.05), Inches(6.05), Inches(11.2), Inches(0.55))
    tf_pas = tb_pas.text_frame
    tf_pas.word_wrap = True
    p_pas = tf_pas.paragraphs[0]
    p_pas.text = "El JWT es como un pasaporte sellado por un consulado (AWS): el backend no necesita llamar al consulado cada vez que entra un turista; solo revisa que el sello sea auténtico con la firma pública."
    p_pas.font.size = Pt(12.5)
    p_pas.font.bold = True
    p_pas.font.color.rgb = C_CYAN
    p_pas.font.name = FONT_FAMILY

    add_notes(s6,
        "PUNTOS CLAVE PARA EL DISCURSO:\n\n"
        "• Explicar de forma muy pedagógica qué es un JWT:\n"
        "• Tres partes separadas por puntos y codificadas en Base64Url: Header, Payload y Firma.\n"
        "• Lo fundamental es la firma: Amazon firma con su llave PRIVADA en la nube. Nuestro backend solo descarga la llave PÚBLICA (JWKS) para verificar la autenticidad.\n"
        "• Como la verificación es matemática y local, el backend responde en microsegundos sin hacer consultas a bases de datos de usuarios."
    )

    # =========================================================================
    # DIAPOSITIVA 7: BACKEND STATELESS CON SPRING SECURITY 6
    # =========================================================================
    s7 = prs.slides.add_slide(blank_layout)
    apply_base(s7)
    add_header(s7, "Backend Stateless: Spring Boot 3 & Resource Server", "06 • Backend & Lógica", 7)

    be_points = [
        ("Arquitectura 100% Stateless", "El backend no guarda sesiones en memoria ni cookies de sesión. Cada petición HTTP es autónoma y autosuficiente, permitiendo réplicas instantáneas en Render sin sincronización.", C_CYAN),
        ("Sincronización Automática con JWKS", "Spring Security consulta el endpoint de llaves públicas de Cognito: valida emisor (iss), audiencia y vigencia temporal (exp) de forma asimétrica.", C_MINT),
        ("Extracción de Identidad Segura", "Mediante @AuthenticationPrincipal Jwt, el controlador obtiene directamente el email verificado del usuario para registrar la autoría del producto creado.", C_AMBER)
    ]

    for idx, (title, desc, color) in enumerate(be_points):
        y = 2.0 + (idx * 1.55)
        add_card(s7, 0.8, y, 11.733, 1.35, title, color)
        tb = s7.shapes.add_textbox(Inches(1.05), Inches(y + 0.55), Inches(11.2), Inches(0.7))
        tf = tb.text_frame
        tf.word_wrap = True
        p = tf.paragraphs[0]
        p.text = desc
        p.font.size = Pt(13)
        p.font.color.rgb = C_SILVER
        p.font.name = FONT_FAMILY

    add_notes(s7,
        "PUNTOS CLAVE PARA EL DISCURSO:\n\n"
        "• En el backend configuramos Spring Boot 3.4 como un OAuth2 Resource Server formal.\n"
        "• Eliminamos la sesión del servidor: SessionCreationPolicy.STATELESS.\n"
        "• Cuando llega un producto nuevo, Spring Boot extrae el email del token verificado. No hay posibilidad de que un usuario suplante la identidad de otro, porque el token está blindado criptográficamente."
    )

    # =========================================================================
    # DIAPOSITIVA 8: PERSISTENCIA CLOUD - SUPABASE POSTGRESQL
    # =========================================================================
    s8 = prs.slides.add_slide(blank_layout)
    apply_base(s8)
    add_header(s8, "Persistencia Cloud: Supabase PostgreSQL Gestionado", "07 • Almacenamiento", 8)

    db_points = [
        ("Base de Datos Relacional ACID", "PostgreSQL 15+ administrado en la nube con integridad transaccional, claves foráneas, índices optimizados y respaldos continuos sin mantenimiento manual.", C_CYAN),
        ("Session Pooler (Puerto 5432)", "Integración optimizada con HikariCP. El pooler de Supabase multiplexa las conexiones concurrentes, evitando la saturación del servidor bajo alta concurrencia.", C_MINT),
        ("Aislamiento Total de Credenciales", "La base de datos sólo acepta conexiones SSL cifradas. Las credenciales residen en variables de entorno seguras en Render, jamás expuestas en el repositorio.", C_AMBER)
    ]

    for idx, (title, desc, color) in enumerate(db_points):
        y = 2.0 + (idx * 1.55)
        add_card(s8, 0.8, y, 11.733, 1.35, title, color)
        tb = s8.shapes.add_textbox(Inches(1.05), Inches(y + 0.55), Inches(11.2), Inches(0.7))
        tf = tb.text_frame
        tf.word_wrap = True
        p = tf.paragraphs[0]
        p.text = desc
        p.font.size = Pt(13)
        p.font.color.rgb = C_SILVER
        p.font.name = FONT_FAMILY

    add_notes(s8,
        "PUNTOS CLAVE PARA EL DISCURSO:\n\n"
        "• Pregunta típica: ¿Por qué conectarse por el puerto 5432 (Session Pooler) en Supabase?\n"
        "• Respuesta: Porque los entornos Cloud como Render generan conexiones dinámicas. El Session Pooler gestiona eficientemente el ciclo de vida de las conexiones y evita que se agoten los sockets de PostgreSQL.\n"
        "• Además, mediante Spring Data JPA e Hibernate, el esquema se valida automáticamente al iniciar la aplicación."
    )

    # =========================================================================
    # DIAPOSITIVA 9: DEMOSTRACIÓN TÉCNICA EN VIVO
    # =========================================================================
    s9 = prs.slides.add_slide(blank_layout)
    apply_base(s9)
    add_header(s9, "Demostración Práctica: Circuito de Validación en Vivo", "08 • Live Demo", 9)

    steps = [
        ("Paso 1: Acceso Público", "Ingreso a Vercel sin sesión. El catálogo carga públicamente desde Supabase sin restricciones de acceso.", C_CYAN),
        ("Paso 2: Login con Cognito", "Clic en 'Iniciar sesión'. Redirección segura a la Hosted UI oficial de AWS y validación con PKCE.", C_MINT),
        ("Paso 3: Creación de Producto", "Completar formulario. Petición POST protegida con cabecera 'Authorization: Bearer <token>'.", C_AMBER),
        ("Paso 4: Auditoría y Persistencia", "El backend valida el token, guarda en Supabase (puerto 5432) y la UI se actualiza al instante.", C_WHITE)
    ]

    for idx, (title, desc, color) in enumerate(steps):
        col = idx % 2
        row = idx // 2
        x = 0.8 + (col * 5.95)
        y = 2.0 + (row * 2.35)

        add_card(s9, x, y, 5.75, 2.1, title, color, bg_color=RGBColor(18, 28, 50))
        tb = s9.shapes.add_textbox(Inches(x + 0.25), Inches(y + 0.75), Inches(5.25), Inches(1.15))
        tf = tb.text_frame
        tf.word_wrap = True
        p = tf.paragraphs[0]
        p.text = desc
        p.font.size = Pt(13)
        p.font.color.rgb = C_SILVER
        p.font.name = FONT_FAMILY

    add_notes(s9,
        "GUÍA PARA LA DEMOSTRACIÓN ANTE LA COMISIÓN:\n\n"
        "1. Mostrar la URL de Vercel (https://soluciones-cloud-v1.vercel.app): evidenciar que los productos cargan sin login.\n"
        "2. Abrir la pestaña 'Network' (Red) en las herramientas de desarrollador del navegador.\n"
        "3. Pulsar 'Iniciar sesión con Cognito' y mostrar la URL de amazon cognito en la barra de direcciones.\n"
        "4. Iniciar sesión y crear un producto nuevo (ej. 'Monitor Gamer 4K').\n"
        "5. Señalar en la consola de red la cabecera 'Authorization: Bearer' y el código HTTP 201 Created.\n"
        "6. Mostrar el nuevo producto visible en la lista."
    )

    # =========================================================================
    # DIAPOSITIVA 10: CONCLUSIONES Y RONDA DE PREGUNTAS
    # =========================================================================
    s10 = prs.slides.add_slide(blank_layout)
    apply_base(s10)
    add_header(s10, "Conclusiones: Arquitectura de Grado Empresarial", "09 • Cierre & Defensa", 10)

    # Gran tarjeta central de logros
    add_card(s10, 0.8, 2.0, 11.733, 2.4, "Pilares Cumplidos del Proyecto", C_CYAN, bg_color=RGBColor(16, 28, 52))
    tb_c1 = s10.shapes.add_textbox(Inches(1.1), Inches(2.65), Inches(11.1), Inches(1.6))
    tf_c1 = tb_c1.text_frame
    tf_c1.word_wrap = True

    kpis = [
        "✔  Desacoplamiento 100% Multi-Cloud: Cada servicio vive en su propio proveedor óptimo (Vercel, AWS, Render, Supabase).",
        "✔  Seguridad Bancaria Delegada: Cero contraseñas en código o BD; protección total con AWS Cognito y PKCE.",
        "✔  Defensa en Profundidad: Doble barrera de filtrado perimetral con Amazon API Gateway y verificación asimétrica en Spring Boot."
    ]
    for idx, kpi in enumerate(kpis):
        p = tf_c1.paragraphs[0] if idx == 0 else tf_c1.add_paragraph()
        p.text = kpi
        p.font.size = Pt(13.5)
        p.font.color.rgb = C_SILVER
        p.font.name = FONT_FAMILY
        if idx > 0: p.space_before = Pt(10)

    # Tarjeta de Preguntas
    add_card(s10, 0.8, 4.65, 11.733, 2.05, "Ronda de Preguntas Técnicas", C_MINT, bg_color=RGBColor(18, 38, 50))
    tb_q = s10.shapes.add_textbox(Inches(1.1), Inches(5.25), Inches(11.1), Inches(1.3))
    tf_q = tb_q.text_frame
    tf_q.word_wrap = True

    pq1 = tf_q.paragraphs[0]
    pq1.text = "Muchas gracias por su atención."
    pq1.font.size = Pt(18)
    pq1.font.bold = True
    pq1.font.color.rgb = C_WHITE
    pq1.font.name = FONT_FAMILY

    pq2 = tf_q.add_paragraph()
    pq2.text = "Quedamos a disposición de la comisión evaluadora para responder dudas y profundizar en la arquitectura."
    pq2.font.size = Pt(13.5)
    pq2.font.color.rgb = C_SILVER
    pq2.font.name = FONT_FAMILY
    pq2.space_before = Pt(6)

    add_notes(s10,
        "CIERRE FORMAL:\n\n"
        "• Con esto concluimos la defensa de nuestra arquitectura para 'Soluciones Cloud v1'.\n"
        "• Hemos demostrado que es posible construir una solución de comercio e inventario moderna, segura y de costo cero en desarrollo, aplicando los mismos estándares que utilizan las grandes empresas tecnológicas.\n"
        "• Muchas gracias, profesor. Quedamos atentos a sus preguntas y observaciones."
    )

    # Guardar en las dos ubicaciones para conveniencia
    out_dir = r"c:\Users\sours\OneDrive\Escritorio\Proyectos programacion\Nueva carpeta"
    out1 = os.path.join(out_dir, "CloudStore_Presentacion_Final.pptx")
    out2 = os.path.join(out_dir, "SolucionesCloud_Defensa_Final.pptx")
    prs.save(out1)
    prs.save(out2)
    print(f"Presentación generada con éxito en:\n1. {out1}\n2. {out2}")

if __name__ == "__main__":
    build_presentation()
