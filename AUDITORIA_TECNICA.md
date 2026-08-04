# Auditoría Técnica — SysTechPro

**Alcance:** todo `src/main` (13 controllers, 9 DAO, 8 models, 2 utils, `database.sql`, `web.xml`, `pom.xml`, frontend completo: `index.html`, `admin.html`, `app.js`, `admin.js`, `auth.js`, `styles.css`).
**Metodología:** lectura completa de cada archivo fuente (~8.800 líneas), sin ejecutar la aplicación. No se modificó código en este informe, conforme al plan de auditoría (`HELPDESK (1).md`).

---

## 1. Resumen ejecutivo

SysTechPro es una aplicación Java EE clásica (Servlets + JDBC + MySQL + JS Vanilla) funcionalmente completa: cubre autenticación por rol, inventario de dispositivos, préstamos, mantenimientos, auditoría y reportes. La arquitectura por capas (Controller → DAO → GestorJDBC → MySQL) se respeta de forma razonable y casi todas las consultas usan `PreparedStatement`, por lo que **no se encontró SQL Injection explotable**.

Sin embargo, el proyecto arrastra deuda técnica seria en cuatro frentes:

1. **Esquema de base de datos desactualizado respecto al código** (`database.sql` no crea la tabla `solicitud_password` ni la columna `usuario.cambio_obligatorio` que el código sí usa) — esto rompe una instalación limpia desde cero.
2. **Un frontend completo y muerto** (`index.html` + `app.js`, ~800 líneas) que ya no es alcanzable por ningún rol válido del sistema, coexistiendo con el frontend real (`admin.html` + `admin.js`, 2.277 líneas).
3. **XSS almacenado sistemático** en `admin.js`: decenas de puntos insertan datos del servidor en `innerHTML` sin escapar.
4. **Artefactos de depuración empaquetados en producción** (`LoginTester.java`, `test-db.jsp`) que exponen credenciales de prueba y detalles internos de conexión.

Nada de esto requiere cambiar el stack. Son correcciones y refactors dentro de la arquitectura actual (Fase 3 y Roadmap, más abajo).

---

## 2. Problemas encontrados

### 2.1 Seguridad

| # | Hallazgo | Evidencia | Severidad |
|---|---|---|---|
| S1 | Credenciales de MySQL hardcodeadas en el código fuente (`root` / password vacío) | `src/main/java/com/systechpro/utils/GestorJDBC.java:8-10` | Alta |
| S2 | JSP de debug público que expone detalles internos de conexión/errores SQL sin autenticación | `src/main/webapp/test-db.jsp` (todo el archivo, mapeado en cualquier despliegue por convención de JSP) | Alta |
| S3 | Clase con `main()` empaquetada en el WAR, imprime hash BCrypt de un usuario real por consola y usa contraseña de prueba hardcodeada | `src/main/java/com/systechpro/LoginTester.java:11-12,29-33` | Media |
| S4 | XSS almacenado: interpolación directa de datos del servidor en `innerHTML` sin escapar (nombre, correo, descripción, IP, tabla_afectada, etc.) | `src/main/webapp/js/admin.js:774` (`d.nombre`), `:1106-1110`, `:1186-1188`, `:1238-1244`, `:1953-1958`, y ~15 puntos más del mismo patrón | Alta |
| S5 | Sin protección CSRF en operaciones de escritura (POST/PUT/DELETE autenticadas solo por cookie de sesión) | Todos los controllers, ej. `PrestamoController.java:88-247`; ningún `Filter` de CSRF en `web.xml` | Media |
| S6 | Claves temporales de recuperación de contraseña se guardan en texto plano y quedan expuestas indefinidamente vía API a cualquier administrador | `src/main/java/com/systechpro/dao/SolicitudPasswordDAO.java:45-58` (columna `password_temporal`), renderizado en `admin.js:2190` | Media |
| S7 | Sin límite de intentos de login (fuerza bruta no mitigada) | `src/main/java/com/systechpro/controllers/AuthController.java:44-108` | Media |
| S8 | Cookies de sesión sin configuración explícita de `Secure`/`SameSite` | `src/main/webapp/WEB-INF/web.xml` (sin `<session-config>`) | Baja |
| S9 | `API_BASE` hardcodeado a `http://localhost:8080` en el frontend muerto — indicativo de que ese flujo nunca se probó en un entorno real | `src/main/webapp/js/app.js:2` | Baja (mitigada por S en #2.4, ya que el archivo es código muerto) |

### 2.2 Base de datos

| # | Hallazgo | Evidencia |
|---|---|---|
| BD1 | `database.sql` **no crea** la tabla `solicitud_password`, pero `SolicitudPasswordDAO` y el flujo completo de "olvidé mi contraseña" dependen de ella | `src/main/resources/database.sql` (tabla ausente) vs. `src/main/java/com/systechpro/dao/SolicitudPasswordDAO.java:12` |
| BD2 | `database.sql` no define la columna `usuario.cambio_obligatorio`, usada en login, alta y edición de usuarios | `database.sql:10-18` vs. `UsuarioDAO.java:29,68,97` y `AuthController.java:65` |
| BD3 | Sin índices en columnas de filtro frecuente (`prestamo.estado`, `mantenimiento.estado`, `dispositivo.estado`) más allá de los que MySQL crea automáticamente para FKs | `database.sql` completo |
| BD4 | `SELECT *` en casi todos los DAO, incluyendo el hash de contraseña que viaja de la BD a la capa de aplicación en cada listado de usuarios (se limpia recién en el controller) | `UsuarioDAO.java:48`, `DispositivoDAO.java:11-24`, `PrestamoDAO.java:13`, `MantenimientoDAO.java:16` |
| BD5 | Toda paginación es en memoria sobre el dataset completo (no hay `LIMIT`/`OFFSET` en ningún DAO); no escala más allá de unos pocos miles de filas | Todos los métodos `listar()` de los DAO + lógica de paginación cliente en `admin.js` (6 implementaciones similares: dispositivos, usuarios, auditoría, mantenimientos, préstamos, solicitudes) |
| BD6 | Subconsulta de "última ubicación" del dispositivo se recalcula en cada `listar()`/`buscarPorId()` sin índice compuesto que la respalde | `DispositivoDAO.java:11-24` (`BASE_QUERY`) |

### 2.3 Backend

| # | Hallazgo | Evidencia |
|---|---|---|
| B1 | *(Corregido tras revisión — la versión anterior de este hallazgo tenía una transcripción incorrecta de `web.xml`)* `web.xml` registra manualmente solo 4 de los 13 controllers (`AuthController`, `UsuarioController`, `AdministrativoController`, `DispositivoController`); los otros 9 dependen solo de `@WebServlet`. Además, para `AdministrativoController` el `url-pattern` declarado en `web.xml` (`/api/administrativos/*`, plural) **no coincide** con el de su propia anotación `@WebServlet` (`/api/administrativo/*`, singular) — al no declararse `metadata-complete="true"`, el servidor registra ambos patrones para la misma clase, dejándola accesible por dos rutas distintas, ninguna de las cuales usa el frontend actual | `src/main/webapp/WEB-INF/web.xml:26-33` vs. anotación en `AdministrativoController.java` |
| B2 | Sin pool de conexiones: cada operación DAO abre una conexión nueva vía `DriverManager.getConnection()` | `GestorJDBC.java:22-35`, invocado en cada método de cada DAO |
| B3 | Logging por `System.out`/`System.err` sin niveles, timestamps ni rotación; `GestorJDBC` imprime la URL de conexión y el resultado de cada conexión a stdout | `GestorJDBC.java:18,24,26,29-32` y `catch` de prácticamente todos los DAO |
| B4 | Fuga de detalle interno en respuestas de error (`e.getMessage()` devuelto al cliente) en algunos controllers pero no en otros — inconsistente | `UsuarioController.java:180`, `PrestamoController.java:163` vs. el resto que responde genérico |
| B5 | Validación de texto libre (regex de nombre) duplicada palabra por palabra en dos controllers distintos, y triplicada en el frontend | `UsuarioController.java:34-44` (`validarTextoLibre`) = `DispositivoController.java:33-43` (`validarNombreDispositivo`) = `admin.js:621-631` (`esTextoLibreValido`) |
| B6 | `AdministrativoController` y `DocenteController` son casi idénticos línea por línea (mismo parseo, misma validación de fechas, mismo insert de préstamo) | `AdministrativoController.java:53-116` vs. `DocenteController.java:54-118` |
| B7 | Método muerto `parseTimestamp` definido y nunca invocado | `MantenimientoController.java:142-151` |
| B8 | `MantenimientoController.doPut` no valida el valor de `estado` contra el enum permitido (a diferencia de `PrestamoController.doPut`, que sí lo hace) — rigor inconsistente entre controllers similares | `MantenimientoController.java:180-218` vs. `PrestamoController.java:209-213` |
| B9 | Comentario de incertidumbre de diseño dejado en código de producción (`// Asumiendo que el ADMIN es quien puede modificar dispositivos...`) | `DispositivoController.java:29` |
| B10 | Roles y estados como *magic strings* repetidos en cada controller, cada DAO y el frontend, sin una única fuente de verdad (enum Java / constantes JS) | Transversal a todo el backend y a `admin.js`/`auth.js` |

### 2.4 Frontend

| # | Hallazgo | Evidencia |
|---|---|---|
| F1 | **Frontend completo muerto**: `index.html` (vista `dashboard-view`, líneas 57-102) + `app.js` (709 líneas) implementan un CRUD paralelo completo que ya no es alcanzable — `checkSession()`/`handleLogin()` redirigen siempre a `admin.html` para los 4 roles válidos del sistema | `app.js:22-38,104-139` |
| F2 | El frontend muerto ni siquiera está sincronizado con el backend actual: usa estados inexistentes (`PRESTADO` en vez de `EN_USO`, `COMPLETADO` en vez de `FINALIZADO`) y `type="date"` en vez de `datetime-local`, que rompería el parseo `ISO_LOCAL_DATE_TIME` del backend si alguna vez se ejecutara | `app.js:321-323,439-442,561-563` |
| F3 | XSS almacenado sistemático (ver S4) — mismo patrón repetido en ~20 puntos de `admin.js` | Ver tabla de seguridad |
| F4 | Función `parseFecha` **definida dos veces** en el mismo archivo; la segunda sobreescribe silenciosamente a la primera | `admin.js:1001-1020` y `admin.js:1039-1071` |
| F5 | Dos sistemas de permisos de sidebar independientes y redundantes cargados simultáneamente en `admin.html`, cada uno con su propio mapa rol→elementos y su propia lógica de show/hide | `auth.js:10-90` (`aplicarPermisosSidebar`) vs. `admin.js:20-60` (`aplicarPermisosPorRol`) |
| F6 | `admin.js` es un archivo monolítico de 2.277 líneas sin módulos, mezclando lógica de 7 entidades distintas en un único listener `DOMContentLoaded` | `admin.js` completo |
| F7 | Cientos de atributos `style="..."` inline repetidos en vez de clases CSS reutilizables | `admin.html` (prácticamente todas sus 1.086 líneas) |
| F8 | `styles.css` contiene dos sistemas de diseño superpuestos sin deslinde claro: el sistema "admin" (líneas 1-409) y el sistema legado de `index.html`/`app.js` (líneas 410-799, con clases que solo usa el código muerto de F1) | `styles.css:1-799` |
| F9 | Sin build/minificación; cache-busting manual por query string que hay que recordar incrementar a mano en cada deploy (`?v=9`, `?v=6`, `?v=4`) | `admin.html:8,1083,1084`, `index.html:151` |
| F10 | Dependencias de CDN sin pin de versión ni integridad (SRI): `lucide@latest` (sin versión fija) | `index.html:11` |
| F11 | UX inconsistente entre módulos: `alert()`/`confirm()` nativos en unos flujos (usuarios, mantenimientos, préstamos) y un sistema de `showToast()` propio en otros (dispositivos) | `admin.js:567,588,591` (`alert`) vs. `admin.js:854,858` (`showToast`) |
| F12 | Accesibilidad limitada: iconos SVG sin `aria-label`, estado comunicado solo por color en los badges, tablas dinámicas sin atributos ARIA | `admin.html`, badges en `admin.js:722-737` |
| F13 | Paginación client-side reimplementada 6 veces con variaciones sutiles en vez de una función reutilizable | `admin.js` (dispositivos, usuarios, auditoría, mantenimientos, préstamos, solicitudes) |

### 2.5 Calidad general

| # | Hallazgo | Evidencia |
|---|---|---|
| C1 | Cero pruebas automatizadas (unitarias o de integración) en todo el proyecto | Ausencia de carpeta `src/test` |
| C2 | El único intento de verificación es una clase con `main()` manual, no un test real | `LoginTester.java` |
| C3 | Sin capa de servicio entre Controller y DAO: validación, autorización, orquestación de auditoría y serialización JSON mezcladas en el mismo método, lo que infla los controllers (`UsuarioController` 334 líneas, `DispositivoController` 329, `PrestamoController` 248) | Ver archivos citados |
| C4 | Sin documentación de despliegue centralizada más allá de archivos sueltos (`GUIA_INICIO.md`, `INSTRUCCIONES.md`, `DOCUMENTO_PRUEBAS.md`) sin un único punto de entrada | Raíz de `systechpro/` |

---

## 3. Fortalezas

- Separación Controller → DAO → GestorJDBC respetada de forma consistente; ningún DAO contiene lógica de presentación.
- Uso correcto y consistente de `PreparedStatement` — no se encontró SQL Injection explotable en ningún punto del código auditado.
- Contraseñas hasheadas con BCrypt (`Encriptador.java`), no en texto plano ni con hash débil.
- Control de autorización por rol presente en **todos** los endpoints sensibles (verificación de sesión + rol antes de cada operación de escritura).
- Registro de auditoría (`auditoria` + `login_log`) implementado de forma transversal para las operaciones críticas (login, alta/edición/baja de usuario y dispositivo).
- El sistema de permisos de rol en el backend es exhaustivo: los 4 roles (`ADMINISTRADOR`, `TECNICO`, `DOCENTE`, `ADMINISTRATIVO`) tienen reglas de acceso explícitas en cada controller.
- Modelos (`models/`) son POJOs limpios, sin lógica de negocio filtrada.

## 4. Debilidades

- Brecha crítica entre el esquema versionado (`database.sql`) y el esquema real que el código necesita (BD1, BD2): una instalación desde cero, siguiendo únicamente el repo, no funciona.
- Deuda de "dos frontends" (F1) que duplica superficie de mantenimiento y de bugs sin aportar valor.
- Ausencia total de pruebas automatizadas hace que cualquier cambio futuro (incluyendo los de este roadmap) sea riesgoso de verificar.
- Sin pool de conexiones ni índices en columnas de filtro: el sistema no está preparado para crecer más allá de un uso departamental pequeño.

## 5. Oportunidades de mejora

- Centralizar roles/estados en un único enum Java + módulo de constantes JS, eliminando ~10 puntos de duplicación de *magic strings*.
- Extraer una capa de utilidades JS compartidas (`api.js`, `fechas.js`, `validaciones.js`, `paginacion.js`) para eliminar la duplicación de F5, F13 y B5.
- Adoptar un logger simple (`java.util.logging` ya viene con el JDK, sin agregar dependencias) en vez de `System.out`/`System.err`.
- Introducir un pool de conexiones ligero (Tomcat JDBC Pool, ya disponible en el Tomcat objetivo, sin nueva dependencia Maven) en `GestorJDBC`.

## 6. Riesgos

- **Riesgo de instalación rota** (BD1/BD2): cualquier despliegue nuevo desde el repo tal como está falla en el primer login o en el primer "olvidé mi contraseña".
- **Riesgo de explotación XSS** por un usuario con rol bajo (Docente/Administrativo) contra un Administrador, vía cualquier campo de texto libre (nombre, descripción) que termine en una tabla de `admin.js`.
- **Riesgo de exposición de credenciales** si el repositorio se sube a un remoto compartido o público (S1).
- **Riesgo operativo**: sin pool de conexiones, un pico de uso concurrente puede agotar las conexiones disponibles de MySQL.

## 7. Recomendaciones

Priorizar en este orden: (1) corregir la brecha de esquema BD1/BD2 — bloquea cualquier instalación nueva; (2) eliminar XSS almacenado (F3/S4) y los artefactos de debug en producción (S2/S3) — riesgo de seguridad activo; (3) eliminar el frontend muerto (F1) — reduce superficie de mantenimiento antes de tocar el resto del frontend; (4) el resto de refactors de calidad, en el orden del roadmap.

---

## 8. Fase 3 — Rediseño propuesto (mismo stack)

Sin migrar de Java/Servlets/JDBC/MySQL/Tomcat/HTML/CSS/JS Vanilla:

- **Backend**: introducir una capa `service/` delgada entre Controller y DAO solo donde hay lógica compartida real (validación de rol, orquestación de auditoría) — sin over-engineering, sin frameworks nuevos. Mover roles/estados a `enum` Java (`Rol`, `EstadoDispositivo`, `EstadoPrestamo`, `EstadoMantenimiento`) usados tanto en validación como en `switch`/comparaciones, eliminando *magic strings*. Sustituir `System.out`/`System.err` por `java.util.logging` (parte del JDK, cero dependencias nuevas). Sustituir `DriverManager` por `Tomcat JDBC Pool` en `GestorJDBC` sin cambiar la interfaz pública de la clase, de modo que ningún DAO necesite cambios.
- **Base de datos**: regenerar `database.sql` para que sea la fuente de verdad real (agregar `usuario.cambio_obligatorio` y la tabla `solicitud_password` completas), agregar índices en columnas `estado` de `prestamo`, `mantenimiento`, `dispositivo`. Introducir `LIMIT`/`OFFSET` en los métodos `listar()` más usados (dispositivos, préstamos, auditoría) y mover la paginación del cliente al servidor progresivamente.
- **Frontend**: eliminar por completo `index.html` (vista dashboard) + `app.js`, dejando `index.html` únicamente como pantalla de login (ya es su rol real hoy) y `admin.html`/`admin.js` como única aplicación. Dividir `admin.js` en módulos por entidad (`dispositivos.js`, `prestamos.js`, `mantenimientos.js`, `usuarios.js`, `auditoria.js`, `reportes.js`, `core.js` con `fetch`/toast/paginación compartidos) cargados como `<script type="module">` — sigue siendo JS Vanilla, sin build step ni npm. Sanitizar toda inserción de datos de usuario en el DOM (usar `textContent` en vez de `innerHTML` allí donde no hay HTML real que insertar, o una función `escapeHtml()` centralizada donde sí lo hay). Mover los estilos inline de `admin.html` a clases CSS reutilizables en `styles.css`, y separar claramente (o eliminar tras borrar F1) el bloque de estilos legado.
- **Seguridad**: eliminar `test-db.jsp` y mover `LoginTester.java` fuera de `src/main` (a un script de desarrollo o eliminarlo). Agregar un `Filter` simple de cabeceras de seguridad (CSP básica, `X-Content-Type-Options`) y limitar intentos de login con un contador en memoria o en `login_log`. Purgar `password_temporal` tras su primer uso o tras expirar.

## 9. Fase 4 — Roadmap

| Fase | Tarea | Prioridad | Impacto | Dificultad | Archivos afectados | Riesgo | Dependencias |
|---|---|---|---|---|---|---|---|
| **1. Correcciones críticas** | Sincronizar `database.sql` con el esquema real (BD1, BD2) | Crítica | Alto | Baja | `src/main/resources/database.sql` | Bajo (script aditivo) | Ninguna — bloquea instalaciones nuevas |
| | Eliminar `test-db.jsp` y mover `LoginTester.java` fuera de `src/main` | Crítica | Alto | Baja | `test-db.jsp`, `LoginTester.java` | Bajo | Ninguna |
| | Sanitizar `innerHTML` en `admin.js` (XSS almacenado) | Crítica | Alto | Media | `admin.js` (~20 puntos) | Medio (tocar mucho render) | Ninguna |
| **2. Refactor backend** | Centralizar roles/estados en `enum` Java | Alta | Medio | Media | Todos los controllers + DAO | Medio | Fase 1 completa |
| | Introducir pool de conexiones en `GestorJDBC` | Alta | Alto | Baja | `GestorJDBC.java` | Bajo (interfaz no cambia) | Ninguna |
| | Reemplazar `System.out`/`err` por `java.util.logging` | Media | Medio | Baja | Todos los DAO + `GestorJDBC` | Bajo | Ninguna |
| | Unificar `AdministrativoController`/`DocenteController` y validaciones duplicadas (B5, B6) | Media | Medio | Media | Ambos controllers, `UsuarioController`, `DispositivoController` | Medio | Fase 2 (enums) |
| **3. Refactor frontend** | Eliminar `index.html` (dashboard) + `app.js` (F1) | Alta | Alto | Baja | `index.html`, `app.js`, `styles.css` (bloque legado) | Bajo (código inalcanzable) | Ninguna |
| | Dividir `admin.js` en módulos por entidad | Media | Alto | Alta | `admin.js` → nuevos archivos | Alto (refactor grande) | Eliminación de F1 primero |
| | Unificar los dos sistemas de permisos de sidebar (F5) | Media | Medio | Baja | `auth.js`, `admin.js` | Bajo | Ninguna |
| | Mover estilos inline a clases CSS (F7) | Baja | Bajo | Alta (volumen) | `admin.html`, `styles.css` | Bajo | Puede ir en paralelo |
| **4. Optimización SQL** | Agregar índices en columnas `estado` | Media | Medio | Baja | `database.sql`, migración | Bajo | Fase 1 |
| | Mover paginación a la base de datos (`LIMIT`/`OFFSET`) | Baja | Medio | Media | DAO `listar()`, `admin.js` | Medio | Módulos de Fase 3 |
| **5. Mejoras UX** | Reemplazar `alert()`/`confirm()` por `showToast()`/modal de confirmación consistente | Baja | Medio | Baja | `admin.js` | Bajo | Ninguna |
| | Accesibilidad: `aria-label`, texto además de color en badges | Baja | Bajo | Media | `admin.html`, `admin.js` | Bajo | Ninguna |
| **6. Mejoras de seguridad** | Rate limiting de login | Media | Alto | Media | `AuthController.java` | Bajo | Ninguna |
| | Purga de `password_temporal` tras uso/expiración | Media | Medio | Baja | `SolicitudPasswordDAO.java`, `AdminController.java` | Bajo | Ninguna |
| | Cabeceras de seguridad (CSP, `X-Content-Type-Options`) + CSRF básico | Media | Medio | Media | Nuevo `Filter`, `web.xml` | Medio | Ninguna |
| | Pruebas automatizadas (JUnit) para DAO y validaciones críticas | Media | Alto | Alta | Nuevo `src/test` | Bajo | Debe ir después de estabilizar Fases 1-2 |

## 10. Estimación del esfuerzo

Estimación orientativa para un desarrollador que ya conoce el proyecto, trabajando de forma incremental (una fase del roadmap a la vez, sin paralelizar):

| Fase | Estimación |
|---|---|
| 1. Correcciones críticas | 1–2 días |
| 2. Refactor backend | 3–4 días |
| 3. Refactor frontend | 5–7 días (la división de `admin.js` es lo más costoso) |
| 4. Optimización SQL | 1–2 días |
| 5. Mejoras UX | 1–2 días |
| 6. Mejoras de seguridad (incluye base de tests) | 3–5 días |
| **Total** | **~14–22 días-persona** |

No se requiere downtime prolongado en ningún punto: todas las tareas son compatibles con despliegues incrementales sobre el mismo Tomcat/MySQL, salvo la migración de esquema (Fase 1), que debe aplicarse con la aplicación detenida o en una ventana corta de mantenimiento.

---

## 11. Estado de implementación

### Fase 1 — Correcciones críticas (implementada)

- `database.sql` sincronizado: agregada `usuario.cambio_obligatorio` y la tabla `solicitud_password`.
- Eliminados `test-db.jsp` y `LoginTester.java`.
- XSS almacenado saneado en `admin.js` (~20 puntos, helper `escapeHtml()`).

### Fase 2 — Refactor backend (implementada)

- Pool de conexiones JDBC: `GestorJDBC` ahora usa `org.apache.tomcat.jdbc.pool.DataSource` (dependencia `provided`, sin cambiar la interfaz pública de la clase). `pom.xml` actualizado.
- Enums de dominio creados en `com.systechpro.models`: `Rol`, `EstadoDispositivo`, `EstadoPrestamo`, `EstadoMantenimiento`, cada uno con `esValido(String)`. Todos los *magic strings* de rol/estado en los 13 controllers fueron reemplazados por referencias a estos enums.
- `System.out`/`System.err` reemplazados por `java.util.logging` en los 9 DAO, `GestorJDBC` y `Encriptador`.
- B5 cerrado: `UsuarioController.validarTextoLibre` y `DispositivoController.validarNombreDispositivo` (idénticos) ahora delegan en una nueva utilidad compartida `com.systechpro.utils.ValidadorTexto`; se incluyó también `esDescripcionValida` en la misma utilidad.
- B6 cerrado: `AdministrativoController` y `DocenteController` (casi idénticos) ahora extienden una base común `SolicitanteBaseController` que contiene toda la lógica compartida; cada subclase solo declara su `@WebServlet` y su rol permitido.
- B7 cerrado: eliminado el método muerto `parseTimestamp` en `MantenimientoController`.
- B8 cerrado: `MantenimientoController.doPut` ahora valida el `estado` recibido contra `EstadoMantenimiento` antes de aplicarlo (antes aceptaba cualquier valor).
- Compilación verificada con `mvn compile` después de cada cambio.

### Hallazgos nuevos descubiertos durante la implementación de Fase 2

Estos no estaban en la Fase 2 y Fase 5 originales; se documentan aquí para una decisión futura, sin haber sido modificados salvo que se indique:

- **B1 corregido** (ver tabla de hallazgos de Backend): la transcripción original de `web.xml` tenía un error — en realidad registra 4 controllers, no 3, y `AdministrativoController` tiene un `url-pattern` distinto entre `web.xml` y su propia anotación `@WebServlet` (`/api/administrativos/*` vs. `/api/administrativo/*`), quedando accesible por ambas rutas.
- **Posible código muerto adicional**: ninguna vista del frontend (`admin.js`/`app.js`) llama a `/api/administrativo/*` ni a `/api/docente/*` — toda la funcionalidad de préstamos del frontend pasa por `/api/prestamos`. Ambos endpoints (ya refactorizados en Fase 2 para eliminar su duplicación) podrían ser candidatos a eliminación en una fase futura, pendiente de confirmación de que ningún cliente externo los use.
- **Schema drift adicional**: `prestamo.estado` nunca incluyó `'DEVUELTO'` en su `ENUM` de MySQL, pese a que `PrestamoController` ya lo acepta y usa como estado válido desde antes de esta auditoría. Corregido en `database.sql` (Fase 2) y agregado `update_db_prestamo_devuelto.sql` para instalaciones existentes.
- **Comparación imposible detectada en `PrestamoController.doPut`**: una condición compara `prestamo.getEstado()` contra `EstadoDispositivo.EN_USO`, un valor que el campo `prestamo.estado` nunca toma (solo `dispositivo.estado` lo hace). Es una condición preexistente que nunca se cumple; se preservó el comportamiento original y se dejó un comentario explicativo en el código — requiere que alguien con contexto de negocio confirme cuál era la intención original antes de corregirla.

### Pendiente (no implementado en esta sesión)

Fases 3 (frontend), 4 (SQL), 5 (UX) y 6 (seguridad avanzada) del roadmap original siguen pendientes.
