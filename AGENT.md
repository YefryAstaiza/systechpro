# AGENT.md

# SysTechPro - Contexto para el Agente

## Objetivo

Este repositorio corresponde al proyecto **SysTechPro**, un sistema HelpDesk para la gestión de dispositivos tecnológicos.

El sistema permite administrar:

- Inventario de dispositivos
- Préstamos
- Mantenimientos
- Usuarios
- Sedes
- Salones
- Auditoría
- Reportes
- Autenticación

El proyecto fue desarrollado como una aplicación web Java tradicional (Java EE/Jakarta), desplegada sobre Apache Tomcat.

---

# Stack tecnológico

Backend

- Java
- Maven
- Jakarta Servlet
- JDBC
- MySQL

Frontend

- HTML
- CSS
- JavaScript Vanilla

Servidor

- Apache Tomcat 10+

Base de datos

- MySQL (XAMPP)

---

# Lo que NO utiliza

Este proyecto NO utiliza:

- Angular
- React
- Vue
- Spring Boot
- Hibernate/JPA
- Node.js
- npm
- Yarn
- Vite
- Webpack

No asumir la existencia de ninguna de estas tecnologías.

---

# Arquitectura

La arquitectura sigue un patrón MVC sencillo.

```
Cliente
    │
    ▼
HTML + JS
    │
    ▼
Controllers (Servlets)
    │
    ▼
DAO
    │
    ▼
GestorJDBC
    │
    ▼
MySQL
```

---

# Organización del proyecto

```
src/main/java/com/systechpro/

controllers/
dao/
models/
utils/

src/main/resources/

database.sql

src/main/webapp/

index.html
admin.html
css/
js/
img/
WEB-INF/
```

---

# Responsabilidad de cada carpeta

## controllers

Contiene los Servlets que exponen la lógica del sistema.

Ejemplos:

- AuthController
- UsuarioController
- PrestamoController
- DispositivoController
- ReporteController
- AuditoriaController

Los controllers reciben peticiones HTTP y delegan el trabajo a los DAO.

---

## dao

Contiene toda la lógica de acceso a la base de datos.

Aquí se ejecutan:

- SELECT
- INSERT
- UPDATE
- DELETE

Los DAO utilizan GestorJDBC.

No colocar lógica de presentación aquí.

---

## models

Representan las entidades del sistema.

Ejemplos:

- Usuario
- Prestamo
- Dispositivo
- Salon
- Sede
- Auditoria
- Mantenimiento

Son POJOs con atributos, getters y setters.

---

## utils

Contiene utilidades comunes.

Ejemplos:

- GestorJDBC
- Encriptador

No duplicar lógica existente.

---

## webapp

Aquí vive todo el frontend.

No existe un frontend separado.

Contiene:

HTML

CSS

JavaScript

Imágenes

WEB-INF

---

# Flujo de autenticación

Login

↓

AuthController

↓

UsuarioDAO

↓

MySQL

↓

Respuesta JSON

↓

JavaScript

↓

Redirección según rol

---

# Roles del sistema

Administrador

- CRUD completo
- Aprobar préstamos
- Gestión de usuarios
- Reportes

Técnico

- Gestión de mantenimientos
- Actualización de estados

Docente

- Solicitud de préstamos
- Consulta de dispositivos

Administrativo

- Solicitud de préstamos
- Consulta de disponibilidad

Nunca eliminar la separación por roles.

---

# Base de datos

La base de datos principal se encuentra en:

```
src/main/resources/database.sql
```

También existen scripts adicionales:

- usuarios_demo.sql
- datos_prueba.sql
- update_db_forgot_password.sql

Antes de modificar consultas verificar la estructura existente.

---

# Ejecución del proyecto

## Base de datos

Iniciar MySQL desde XAMPP.

Crear la base correspondiente.

Importar:

database.sql

---

## Compilar

```
mvn clean package
```

Genera:

```
target/systechpro.war
```

---

## Despliegue

Copiar el WAR a:

```
Tomcat/webapps/
```

Iniciar Tomcat.

Abrir:

```
http://localhost:8080/systechpro/
```

No existe un servidor independiente para el frontend.

---

# Convenciones

Mantener el estilo actual.

No introducir frameworks nuevos.

No migrar a Spring Boot.

No migrar a React.

No migrar a Angular.

No reemplazar JDBC por Hibernate.

Las mejoras deben integrarse respetando la arquitectura existente.

---

# Buenas prácticas

Siempre reutilizar:

DAO existentes

Modelos existentes

Utilidades existentes

Evitar duplicar consultas SQL.

No romper compatibilidad con los controladores actuales.

Mantener nombres consistentes con el resto del proyecto.

---

# Antes de modificar código

Siempre revisar:

1. Si ya existe un DAO para esa funcionalidad.
2. Si ya existe un modelo equivalente.
3. Si ya existe un Controller que pueda reutilizarse.
4. Si el JavaScript ya consume un endpoint similar.
5. Si la funcionalidad afecta permisos por rol.

---

# Skills disponibles

Las instrucciones especializadas por capa viven como Skills en:

```
.claude/skills/systechpro-backend/SKILL.md
.claude/skills/systechpro-frontend/SKILL.md
```

`systechpro-backend`

- Controllers, DAO, models, utils
- Java, Jakarta Servlet, JDBC, MySQL

`systechpro-frontend`

- src/main/webapp (HTML, CSS, JS)
- JavaScript Vanilla, sin frameworks

Antes de modificar una capa, aplicar la Skill correspondiente.

---

# Objetivo del agente

El agente debe actuar como un desarrollador senior de Java MVC clásico.

Debe:

- comprender la arquitectura existente;
- reutilizar el código antes de crear nuevos componentes;
- respetar la organización del proyecto;
- mantener compatibilidad con Tomcat y MySQL;
- evitar introducir dependencias innecesarias;
- producir código limpio, consistente y fácil de mantener.