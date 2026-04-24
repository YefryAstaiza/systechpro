# Guía para Iniciar la Aplicación SysTechPro

Esta guía te ayudará a iniciar la aplicación SysTechPro desde cero. Asegúrate de tener instalados Java JDK 17+, Apache Maven y Apache Tomcat 10+.

## Configuración de la Base de Datos
Antes de iniciar la aplicación, configura la base de datos:
1. Inicia MySQL desde XAMPP Control Panel.
2. Crea la base de datos: Ejecuta en MySQL `CREATE DATABASE systechpro3;`
3. Importa el esquema: `mysql -u root systechpro3 < src/main/resources/database.sql`

Nota: El usuario root de MySQL no tiene contraseña por defecto en XAMPP.

## Paso 1: Compilar el Proyecto
Navega al directorio raíz del proyecto (`systechpro`) y ejecuta:
```
mvn clean package
```
Esto generará el archivo `systechpro.war` en la carpeta `target/`.

## Paso 2: Configurar Variables de Entorno
Antes de iniciar Tomcat, configura las variables de entorno en PowerShell:
```
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot"
$env:CATALINA_HOME = "c:\xampp\tomcat"
```

## Paso 3: Iniciar Tomcat
Ejecuta el comando para iniciar el servidor Tomcat:
```
& "c:\xampp\tomcat\bin\startup.bat"
```
Tomcat debería iniciarse y mostrar mensajes de confirmación.

## Paso 4: Desplegar la Aplicación
Copia el archivo WAR al directorio webapps de Tomcat:
```
Copy-Item "target\systechpro.war" "c:\xampp\tomcat\webapps\"
```
Tomcat detectará automáticamente el archivo y lo desplegará.

## Paso 5: Acceder a la Aplicación
Abre tu navegador web y ve a:
```
http://localhost:8080/systechpro/
```

## Credenciales de Administrador
- **Correo**: `admin@systechpro.com`
- **Contraseña**: `admin123`

## Detener Tomcat
Para detener el servidor, ejecuta:
```
& "c:\xampp\tomcat\bin\shutdown.bat"
```

## Notas Adicionales
- Asegúrate de que MySQL esté corriendo en XAMPP para la base de datos.
- Si hay errores, verifica los logs en `c:\xampp\tomcat\logs\`.
- La aplicación usa Jakarta Servlet 6.0, compatible con Tomcat 10+.</content>
<parameter name="filePath">c:\Users\pc1\Documents\javaSystech\systechpro\GUIA_INICIO.md