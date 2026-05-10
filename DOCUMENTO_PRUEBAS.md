# Documento de Ejecución de Pruebas y Casos de Prueba - SysTechPro

Este documento contiene el plan de pruebas formal para el sistema **SysTechPro**, diseñado mediante el análisis estático de los flujos reales implementados en el frontend (`index.html`, `admin.html`, y sus scripts asociados). 

## 1. Análisis de Flujos Críticos y Priorización
Basado en el análisis de la interfaz y la lógica de negocio real, se identifican los siguientes flujos críticos (Prioridad Alta):
1.  **Autenticación y Autorización**: El sistema depende completamente de la redirección basada en roles (`auth.js`). Un fallo aquí compromete la seguridad.
2.  **Ciclo de Vida de Préstamos**: Solicitud -> Aprobación/Rechazo. Involucra cambios de estado en múltiples entidades (Préstamo y Dispositivo).
3.  **Ciclo de Mantenimiento**: Registro -> Finalización, que afecta la disponibilidad del inventario.
4.  **Gestión de Dispositivos**: Es el núcleo del sistema; sin dispositivos no hay préstamos ni mantenimientos.

## 2. Inconsistencias y Observaciones de Usabilidad Detectadas (Riesgos)
*   **Gestión de Sedes y Salones Inexistente**: Los formularios de *Préstamo* y *Mantenimiento* requieren seleccionar una Sede y un Salón (`#prestamo-sede`, `#prestamo-salon`). Sin embargo, **no existe interfaz** para crear o administrar estas sedes. *Riesgo*: Si la base de datos no está pre-poblada por SQL, el sistema es inoperable.
*   **Flujo de Usuario Nuevo**: Al crear un usuario desde el panel (`#modal-usuario`), se exige contraseña manual. Sería ideal enviar un correo o forzar cambio de clave en el primer login.

---

## 3. Suite de Casos de Prueba

### Módulo 1: Autenticación y Autorización (Login)

**CP-001 | Login exitoso como Administrador y carga de interfaz**
*   **Módulo**: Login / Seguridad
*   **Precondiciones**: Base de datos conectada. Usuario administrador existe.
*   **Datos de Prueba**: Correo: `admin@systechpro.com`, Password: `[clave_valida]`
*   **Pasos**:
    1. Navegar a `index.html`.
    2. Ingresar el correo y contraseña en el formulario `#login-form`.
    3. Clic en "Ingresar".
*   **Resultado Esperado**: Redirección a `admin.html`. El script `auth.js` evalúa el rol (ADMINISTRADOR) y muestra el panel principal (`#panel-dashboard`) con el sidebar completo (Inicio, Dispositivos, Préstamos, Mantenimiento, Usuarios, Auditoría, Reportes).
*   **Resultado Actual**: N/A
*   **Estado**: Pendiente
*   **Prioridad**: Alta
*   **Tipo de Prueba**: Funcional (Camino Feliz)

**CP-002 | Validación de Seguridad - Restricción de Roles (Docente)**
*   **Módulo**: Autorización / Sidebar
*   **Precondiciones**: Usuario con rol `DOCENTE` existe y está autenticado.
*   **Datos de Prueba**: Login con credenciales de un Docente.
*   **Pasos**:
    1. Iniciar sesión correctamente.
    2. Observar la redirección a `admin.html`.
    3. Inspeccionar el menú lateral (sidebar).
*   **Resultado Esperado**: El usuario solo debe ver "Inicio" (`#nav-inicio-btn`) y "Mis Solicitudes" (`#nav-mis-solicitudes-btn`). Paneles como Auditoría, Usuarios o Dispositivos deben estar ocultos/inaccesibles. El panel activo inicial debe ser `#panel-inicio-docente`.
*   **Resultado Actual**: N/A
*   **Estado**: Pendiente
*   **Prioridad**: Alta
*   **Tipo de Prueba**: Seguridad / Funcional

**CP-003 | Acceso denegado con credenciales incorrectas**
*   **Módulo**: Login
*   **Precondiciones**: Aplicación corriendo en la ruta principal.
*   **Datos de Prueba**: Correo: `admin@systechpro.com`, Password: `clave_falsa_123`
*   **Pasos**:
    1. Ingresar credenciales en `index.html`.
    2. Clic en "Ingresar".
*   **Resultado Esperado**: No hay redirección. Se muestra un texto de error en el elemento `#login-error` y el evento no avanza.
*   **Resultado Actual**: N/A
*   **Estado**: Pendiente
*   **Prioridad**: Alta
*   **Tipo de Prueba**: Negativa

### Módulo 2: Gestión de Dispositivos (Inventario)

**CP-004 | Registro de nuevo dispositivo (Camino Feliz)**
*   **Módulo**: Dispositivos
*   **Precondiciones**: Sesión iniciada como Administrador.
*   **Datos de Prueba**: Nombre: "Proyector Epson X10", Tipo: "PROYECTOR", Estado: "DISPONIBLE", Descripción: "Ubicado en almacén central".
*   **Pasos**:
    1. Clic en el tab "Dispositivos" del sidebar.
    2. En la columna derecha (`#disp-form-panel`), llenar los campos especificados.
    3. Clic en "Guardar".
*   **Resultado Esperado**: El formulario se limpia. La tabla de la izquierda (`#tabla-dispositivos`) se actualiza mostrando el nuevo proyector. El contador de "Total de dispositivos" del dashboard incrementa en 1.
*   **Resultado Actual**: N/A
*   **Estado**: Pendiente
*   **Prioridad**: Alta
*   **Tipo de Prueba**: Funcional

**CP-005 | Búsqueda y Filtrado Dinámico de Dispositivos**
*   **Módulo**: Dispositivos
*   **Precondiciones**: Al menos 3 dispositivos creados (1 DISPONIBLE, 1 EN_USO, 1 MANTENIMIENTO).
*   **Datos de Prueba**: Término de búsqueda: "Epson", Filtro: "DISPONIBLE".
*   **Pasos**:
    1. En el panel de dispositivos, escribir "Epson" en el input `#disp-buscar`.
    2. Cambiar el select `#disp-filtro-estado` a "DISPONIBLE".
*   **Resultado Esperado**: La tabla se filtra en tiempo real mostrando únicamente los dispositivos que contengan "Epson" en su nombre/código Y cuyo estado sea exactamente "DISPONIBLE".
*   **Resultado Actual**: N/A
*   **Estado**: Pendiente
*   **Prioridad**: Media
*   **Tipo de Prueba**: Funcional / UI

### Módulo 3: Flujo de Préstamos

**CP-006 | Solicitud de préstamo (Validación de dependencias de UI)**
*   **Módulo**: Préstamos
*   **Precondiciones**: Sesión iniciada como Docente. Debe existir al menos 1 sede y 1 salón en la DB. Debe haber 1 dispositivo "DISPONIBLE".
*   **Datos de Prueba**: Seleccionar dispositivo disponible, seleccionar Sede, seleccionar Salón.
*   **Pasos**:
    1. Ir a "Inicio" o "Mis Solicitudes" y hacer clic en "Solicitar préstamo".
    2. En el modal `#modal-crear-prestamo`, desplegar el select de Dispositivos.
    3. Seleccionar la Sede.
    4. Seleccionar el Salón (verificar que se cargan los salones dependientes de la sede).
    5. Ingresar Fechas de Inicio y Fin.
    6. Clic en "Solicitar".
*   **Resultado Esperado**: El select de Salón se habilita solo tras seleccionar la Sede. El campo `#prestamo-ubicacion-generada` se auto-completa. Tras guardar, la solicitud aparece en la tabla con estado "PENDIENTE".
*   **Resultado Actual**: N/A
*   **Estado**: Pendiente
*   **Prioridad**: Alta
*   **Tipo de Prueba**: Funcional / Borde (Validación en cascada)

**CP-007 | Aprobación de Préstamo por Administrador**
*   **Módulo**: Préstamos / Dashboard
*   **Precondiciones**: Sesión como Admin. Existe una solicitud en estado "PENDIENTE".
*   **Datos de Prueba**: N/A
*   **Pasos**:
    1. Ir al panel "Préstamos".
    2. Localizar el registro pendiente y hacer clic en "Ver Detalle" (Acción).
    3. En el modal `#modal-detalle-prestamo`, hacer clic en "Aprobar".
*   **Resultado Esperado**: El modal se cierra. El estado en la tabla cambia a "APROBADO". En segundo plano, el estado del Dispositivo asociado debe haber cambiado automáticamente a "EN_USO".
*   **Resultado Actual**: N/A
*   **Estado**: Pendiente
*   **Prioridad**: Alta
*   **Tipo de Prueba**: Funcional End-to-End

**CP-008 | Intento de préstamo de fechas invertidas (Validación Negativa)**
*   **Módulo**: Préstamos
*   **Precondiciones**: Sesión iniciada, modal de préstamo abierto.
*   **Datos de Prueba**: Fecha Inicio: `2026-05-10 10:00`, Fecha Fin: `2026-05-09 10:00`.
*   **Pasos**:
    1. Llenar todos los datos correctamente.
    2. Ingresar una fecha de fin anterior a la fecha de inicio.
    3. Clic en Solicitar.
*   **Resultado Esperado**: El sistema rechaza la solicitud. La UI (o backend) arroja una alerta o mensaje de error indicando que el rango de fechas es inválido.
*   **Resultado Actual**: N/A
*   **Estado**: Pendiente
*   **Prioridad**: Media
*   **Tipo de Prueba**: Negativa / Borde

### Módulo 4: Mantenimiento Técnico

**CP-009 | Registro y finalización de Mantenimiento**
*   **Módulo**: Mantenimiento
*   **Precondiciones**: Sesión como Técnico. Un dispositivo requiere atención.
*   **Datos de Prueba**: Dispositivo X, Sede Y, Salón Z, Tipo: "CORRECTIVO", Descripción: "Cambio de lámpara".
*   **Pasos**:
    1. En "Inicio Técnico", clic en "Registrar mantenimiento".
    2. Llenar el modal `#modal-crear-mantenimiento` (notar que Estado está deshabilitado en "EN PROCESO").
    3. Guardar.
    4. Ir a la tabla, abrir el detalle del mantenimiento recién creado.
    5. Clic en "Finalizar Mantenimiento".
*   **Resultado Esperado**: Al guardar, el registro se crea como "EN_PROCESO". Al finalizar, cambia a "FINALIZADO" y el dispositivo asociado vuelve a estar "DISPONIBLE" para el sistema de préstamos.
*   **Resultado Actual**: N/A
*   **Estado**: Pendiente
*   **Prioridad**: Alta
*   **Tipo de Prueba**: Funcional End-to-End

### Módulo 5: Exportación e Informes

**CP-010 | Exportación a CSV de Inventario**
*   **Módulo**: Reportes
*   **Precondiciones**: Sesión como Admin. Existen al menos 5 dispositivos registrados.
*   **Datos de Prueba**: N/A
*   **Pasos**:
    1. Ir al panel de "Reportes".
    2. Clic en "Exportar Inventario (CSV)" (`#btn-exportar-csv`).
*   **Resultado Esperado**: El navegador inicia la descarga automática de un archivo `.csv`. Al abrir el archivo, los datos reflejan exactamente los registros de la base de datos (columnas legibles y codificación UTF-8 correcta).
*   **Resultado Actual**: N/A
*   **Estado**: Pendiente
*   **Prioridad**: Baja
*   **Tipo de Prueba**: Funcional

---
*Este documento ha sido generado mediante inspección directa del código fuente del frontend, asegurando que los identificadores HTML (`#`) y flujos descritos existen e impactan en la lógica programada.*
