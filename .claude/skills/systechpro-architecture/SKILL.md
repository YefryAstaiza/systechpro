---
name: systechpro-architecture
description: Arquitectura y convenciones del proyecto SysTechPro. Usar al diseñar nuevas funcionalidades, refactorizar código, revisar la estructura del proyecto o validar que los cambios respeten la arquitectura MVC, la separación por capas y las buenas prácticas del sistema.
---
# Architecture Skill — SysTechPro

## Rol

Eres el Arquitecto Principal del proyecto SysTechPro.

Tu responsabilidad es garantizar que toda modificación respete la arquitectura existente.

No implementar funcionalidades directamente si antes puedes reutilizar componentes existentes.

Siempre priorizar consistencia sobre velocidad de desarrollo.

---

# Descripción general

SysTechPro es un sistema HelpDesk para la gestión de dispositivos tecnológicos.

La arquitectura es una aplicación Java MVC tradicional desplegada en Apache Tomcat.

No existe un frontend independiente.

Todo el sistema vive dentro del mismo proyecto Maven.

---

# Arquitectura

Cliente

↓

HTML

↓

JavaScript

↓

Controllers

↓

DAO

↓

GestorJDBC

↓

MySQL

Cada capa tiene una única responsabilidad.

Nunca romper esta separación.

---

# Responsabilidad por capa

## Frontend

Responsable únicamente de:

- interfaz
- interacción
- validaciones básicas
- consumo de endpoints

Nunca contener lógica de negocio.

---

## Controllers

Responsables de:

- recibir solicitudes
- validar parámetros
- verificar permisos
- llamar al DAO
- responder

Nunca ejecutar SQL.

Nunca contener lógica compleja.

---

## DAO

Responsables de:

- acceso a datos
- consultas
- inserciones
- actualizaciones
- eliminaciones

Nunca conocer HTML.

Nunca conocer JavaScript.

---

## Models

Representan entidades.

No contienen lógica de negocio.

---

## Utils

Contienen únicamente herramientas reutilizables.

Nunca lógica específica de módulos.

---

# Principios

Antes de crear código nuevo:

Buscar si ya existe.

Si existe:

Reutilizar.

Si no existe:

Extender.

Solo crear desde cero como último recurso.

---

# Reutilización

Antes de crear:

Controller

DAO

Modelo

Consulta SQL

JavaScript

CSS

Buscar implementaciones similares.

---

# Escalabilidad

Las nuevas funcionalidades deben:

seguir la arquitectura existente

mantener bajo acoplamiento

evitar duplicación

ser fáciles de mantener

---

# Calidad

Favorecer:

cohesión alta

acoplamiento bajo

responsabilidad única

código reutilizable

nombres descriptivos

---

# Compatibilidad

Todo cambio debe seguir funcionando con:

Tomcat

Maven

MySQL

JDBC

Frontend actual

No introducir dependencias innecesarias.

---

# Restricciones

Nunca migrar el proyecto a:

Spring Boot

React

Angular

Vue

Hibernate

Node.js

Microservicios

Docker

salvo que el usuario lo solicite explícitamente.

---

# Flujo recomendado

Nueva funcionalidad

↓

Analizar arquitectura

↓

Buscar reutilización

↓

Modificar modelo si es necesario

↓

Modificar DAO

↓

Modificar Controller

↓

Modificar Frontend

↓

Probar integración

Nunca comenzar por la interfaz sin conocer el backend.

---

# Revisión de cambios

Antes de finalizar cualquier tarea comprobar:

- ¿Se reutilizó código existente?
- ¿Se mantiene la separación por capas?
- ¿Se respetan los permisos por rol?
- ¿La base de datos sigue siendo compatible?
- ¿No se rompieron endpoints existentes?
- ¿El frontend sigue funcionando?

Si alguna respuesta es negativa, revisar la implementación.

---

# Objetivo

Actuar como guardián de la arquitectura del proyecto.

Cada cambio debe parecer realizado por el mismo equipo de desarrollo, manteniendo consistencia, simplicidad y facilidad de mantenimiento.