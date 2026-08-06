# Muestra de código fuente — SysTechPro (para depósito ante la DNDA)

Extracto representativo del código fuente completo, versión `v1.0-dnda` (commit `03dd208`), incluido junto a `documentacion_tecnica_systechpro.md` y `manual_usuario_systechpro.md` como material de soporte para el registro.

## Contenido

- **`PrestamoController.java`** — Servlet completo del módulo de Préstamos. Se eligió como muestra de Controller porque es el que concentra la lógica más representativa del sistema: autorización por rol, validación de solapamiento de horarios entre reservas, notificaciones automáticas y el flujo completo de solicitar/aprobar/rechazar/devolver.
- **`PrestamoDAO.java`** — DAO correspondiente al mismo módulo, con las consultas SQL (siempre con `PreparedStatement`), la construcción dinámica de filtros de búsqueda, y el cálculo de solapamiento de horarios.
- **`database.sql`** — Esquema completo original de la base de datos (`src/main/resources/database.sql`), sin modificar.

## Por qué esta pareja Controller/DAO

Se priorizó Préstamos sobre otros módulos porque es el que mejor demuestra la originalidad y complejidad real del desarrollo (reserva anticipada con validación de solapamiento, distinción RESERVADO/EN CURSO/VENCIDO, autoservicio de devolución, trazabilidad de aprobador) frente a un CRUD más genérico como Usuarios o Dispositivos.

El repositorio completo (16 Controllers, 11 DAO, 15 modelos, 6 utilidades, y todo el frontend) está disponible en el historial de Git bajo el tag `v1.0-dnda`; esta carpeta es solo un extracto curado para no depositar el repositorio entero.
