# Guía de Inicio y Despliegue - SysTechPro

Este documento detalla los pasos para configurar, compilar y desplegar la aplicación en tu entorno local.

## 1. Requisitos Previos
*   **XAMPP**: Necesitas tener Apache y MySQL corriendo.
*   **Java JDK 17 o superior**: (Actualmente tienes JDK 25 instalado).
*   **Apache Maven**: (Instalado en `C:\Users\pc1\apache-maven-3.9.9`).
*   **Apache Tomcat 10 o superior**: Es necesario porque el proyecto usa Jakarta Servlet 6.0.

## 2. Configuración de la Base de Datos
1.  Abre **phpMyAdmin** (`http://localhost/phpmyadmin`).
2.  Crea una base de datos llamada `systechpro3`.
3.  Importa el archivo [database.sql](file:///c:/Users/pc1/Documents/javaSystech/systechpro/src/main/resources/database.sql).

## 3. Compilación del Proyecto (Generar el archivo .war)
Para compilar el proyecto y generar el archivo que se despliega en el servidor, ejecuta el siguiente comando en la raíz del proyecto:

```bash
mvn clean package
```

Esto creará una carpeta llamada `target/` y dentro de ella un archivo llamado `systechpro.war`.

## 4. Despliegue en Tomcat
Existen dos formas principales de desplegar:

### Opción A: Despliegue Manual (Recomendado)
1.  Copia el archivo `target/systechpro.war`.
2.  Pégalo en la carpeta `webapps` de tu instalación de **Tomcat 10+**.
3.  Tomcat lo detectará automáticamente y creará una carpeta `systechpro`.
4.  Accede a: `http://localhost:8080/systechpro/`

### Opción B: Desde el IDE (IntelliJ / NetBeans / Eclipse)
1.  Configura un servidor Tomcat 10+ en tu IDE.
2.  Agrega el proyecto SysTechPro como un "Artifact" de tipo Web.
3.  Haz clic en "Run" o "Deploy".

## 5. Credenciales del Administrador
*   **Correo**: `admin@systechpro.com`
*   **Contraseña**: `admin123`

---
*He ejecutado el comando de compilación por ti para asegurar que todo esté correcto y generar el primer paquete de despliegue.*
