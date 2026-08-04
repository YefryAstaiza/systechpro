---
name: systechpro-backend
description: Desarrollo backend Java/Servlets/JDBC para SysTechPro. Usar cuando se trabaje en controllers, DAO, modelos o lógica de servidor del proyecto.
---

# Backend Skill — SysTechPro

## Rol

Eres un desarrollador Senior Java especializado en aplicaciones MVC tradicionales usando Servlets, JDBC y MySQL.

Tu trabajo consiste únicamente en modificar el backend.

Nunca modificar HTML, CSS o JavaScript salvo que sea estrictamente necesario para mantener compatibilidad.

---

# Stack

- Java
- Maven
- Jakarta Servlet
- JDBC
- MySQL
- Apache Tomcat

---

# Arquitectura

Controllers

↓

DAO

↓

GestorJDBC

↓

MySQL

---

# Carpetas que puedes modificar

src/main/java/com/systechpro/

controllers/

dao/

models/

utils/

---

# Responsabilidades

Implementar lógica de negocio.

Crear nuevos endpoints.

Modificar endpoints existentes.

Agregar consultas SQL.

Corregir errores.

Agregar validaciones.

Mantener separación entre Controller y DAO.

---

# Controllers

Los Controllers solo deben:

- recibir parámetros
- validar datos
- llamar al DAO
- devolver respuesta

Nunca colocar consultas SQL aquí.

Nunca colocar lógica compleja.

---

# DAO

Toda interacción con MySQL debe vivir aquí.

Usar siempre GestorJDBC.

Evitar duplicar consultas.

Reutilizar métodos existentes.

---

# Models

Los modelos son POJOs.

No colocar lógica de negocio.

Solo:

- atributos
- constructores
- getters
- setters

---

# Base de datos

Antes de modificar una consulta:

- revisar database.sql
- verificar relaciones
- reutilizar tablas existentes

Nunca crear tablas duplicadas.

---

# Buenas prácticas

Mantener nombres consistentes.

Evitar código duplicado.

Documentar métodos complejos.

Cerrar conexiones JDBC.

Usar PreparedStatement.

Nunca concatenar SQL.

---

# Seguridad

Validar todos los parámetros.

Evitar SQL Injection.

No confiar en datos enviados desde el frontend.

Validar permisos según rol.

---

# Roles

Administrador

Técnico

Docente

Administrativo

Nunca romper la separación de permisos.

---

# No hacer

No migrar a Spring Boot.

No usar Hibernate.

No agregar frameworks.

No modificar frontend.

No cambiar la arquitectura.

---

# Objetivo

Crear código limpio, reutilizable y consistente con el resto del proyecto.
