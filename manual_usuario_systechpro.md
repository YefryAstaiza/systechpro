# Manual de Usuario — SysTechPro

Sistema de Gestión de Dispositivos Tecnológicos de la Fundación Universitaria de Popayán.

Este manual explica, paso a paso y sin lenguaje técnico, qué puede hacer cada tipo de usuario dentro del sistema. SysTechPro tiene **cinco roles**: Administrador, Técnico, Docente, Administrativo y Monitor. Cada uno ve un menú distinto, adaptado a lo que necesita hacer.

---

## Cómo iniciar sesión (todos los roles)

1. Abrí la página principal del sistema en tu navegador.
2. Ingresá tu **correo electrónico** y tu **contraseña**.
3. Hacé clic en **Ingresar**.
4. El sistema te lleva automáticamente al panel correspondiente a tu rol — no hay que elegirlo, lo determina tu cuenta.

**Si olvidaste tu contraseña:**
1. En la pantalla de inicio de sesión, hacé clic en **"¿Olvidó su contraseña?"**.
2. Escribí tu correo electrónico y hacé clic en **Enviar Solicitud**.
3. Un administrador va a revisar tu solicitud y, si la aprueba, vas a recibir una contraseña temporal por correo electrónico.
4. La próxima vez que intentes iniciar sesión con esa contraseña temporal, el sistema te va a pedir automáticamente que definas una contraseña nueva antes de dejarte entrar. Una vez definida, iniciá sesión de nuevo con la contraseña nueva.

**Para cerrar sesión:** hacé clic en el botón **"Cerrar sesión"**, arriba a la derecha, en cualquier pantalla del sistema.

---

## 1. Rol Administrador

### Qué puede hacer
El Administrador tiene control total del sistema. Ve en su menú lateral: **Inicio, Usuarios, Dispositivos, Préstamos, Mantenimiento, Auditoría, Reportes, Solicitudes Clave, Corte Diario.**

- Gestionar el inventario completo de dispositivos (crear, editar, eliminar).
- Aprobar o rechazar cualquier solicitud de préstamo.
- Crear, editar y eliminar cuentas de usuario, y asignarles un rol.
- Consultar la auditoría (registro de inicios de sesión y de altas/ediciones/bajas de usuarios y dispositivos).
- Ver reportes y estadísticas del uso del sistema.
- Aprobar o rechazar solicitudes de restablecimiento de contraseña de otros usuarios.
- Generar y consultar el historial de "Corte Diario" (una foto del estado del inventario en un momento dado).

### Aprobar o rechazar una solicitud de préstamo
1. En el menú, entrá a **Préstamos**.
2. Vas a ver la lista de todas las solicitudes. Las que están **Pendientes** muestran los botones **Aprobar** y **Rechazar**.
3. Podés usar el buscador (por dispositivo o usuario) y el filtro por estado o por rango de fechas para encontrar una solicitud puntual.
4. Hacé clic en **Aprobar** para autorizar el préstamo, o en **Rechazar** para denegarlo. El sistema valida automáticamente que el dispositivo no tenga otro préstamo aprobado que se cruce en el mismo horario.

### Registrar un dispositivo nuevo
1. Entrá a **Dispositivos**.
2. Hacé clic en **"+ Agregar dispositivo"**.
3. Completá nombre, tipo y descripción (opcional). El estado se asigna automáticamente en "Disponible" — no se elige a mano.
4. Guardá. El dispositivo aparece en la lista, listo para ser solicitado en un préstamo.

### Ver quién tiene un dispositivo en este momento
1. En **Dispositivos**, buscá el equipo que te interesa.
2. Hacé clic en el ícono de información (junto a los de editar/eliminar).
3. El sistema muestra si el dispositivo está prestado ahora mismo, a quién, quién aprobó ese préstamo, y las fechas del préstamo. Si no tiene ningún préstamo activo, te lo indica también.

### Crear un usuario nuevo
1. Entrá a **Usuarios**.
2. Hacé clic en **"+ Nuevo Usuario"**.
3. Completá nombre, correo, contraseña inicial y rol (Administrador, Técnico, Docente, Administrativo o Monitor).
4. Guardá. La persona ya puede iniciar sesión con esos datos.

### Resolver una solicitud de contraseña
1. Entrá a **Solicitudes Clave**.
2. Las solicitudes pendientes muestran los botones **Aprobar** y **Rechazar**.
3. Al aprobar, el sistema genera una contraseña temporal y se la envía por correo al usuario (además de mostrártela a vos en la pantalla, mientras esté vigente).

### Generar un Corte Diario
1. Entrá a **Corte Diario**.
2. Hacé clic en **"Generar corte ahora"**.
3. El sistema registra, en ese instante, cuántos dispositivos hay disponibles, en préstamo y en mantenimiento, y guarda el detalle de quién tiene cada uno. Queda guardado en el historial de la misma pantalla para consultarlo después.

---

## 2. Rol Técnico

### Qué puede hacer
El Técnico ve en su menú: **Inicio, Mantenimiento, Corte Diario.**

- Registrar un mantenimiento (preventivo o correctivo) sobre un dispositivo.
- Finalizar un mantenimiento en curso, devolviendo el dispositivo a estado disponible.
- Generar y consultar el historial de Corte Diario.

### Registrar un mantenimiento
1. En **Inicio** (tu panel principal) o en **Mantenimiento**, hacé clic en **"Registrar mantenimiento"**.
2. Elegí el dispositivo (solo aparecen los que están disponibles), el tipo (Preventivo o Correctivo), la sede/salón y una descripción.
3. Guardá. El dispositivo pasa automáticamente a estado "En mantenimiento" y no puede ser solicitado en préstamo hasta que finalices el proceso.

### Finalizar un mantenimiento
1. Entrá a **Mantenimiento**.
2. Buscá el registro correspondiente (podés filtrar por estado "En Proceso") y hacé clic en **"Ver"** en esa fila.
3. En la ventana de detalle, hacé clic en **"Finalizar Mantenimiento"** (solo aparece si el mantenimiento sigue en proceso). El dispositivo vuelve automáticamente a estado "Disponible".

### Generar un Corte Diario
Igual que para el Administrador: **Corte Diario → "Generar corte ahora"**.

---

## 3. Rol Docente

### Qué puede hacer
El Docente ve en su menú: **Inicio, Mis Solicitudes.**

- Solicitar el préstamo de un dispositivo, incluida una reserva para una fecha/hora futura.
- Ver el estado de sus propias solicitudes (nunca las de otros usuarios).
- Cancelar una solicitud propia mientras esté pendiente de aprobación.
- Devolver (o cancelar, si todavía no empezó) un préstamo propio ya aprobado.

### Solicitar un préstamo
1. En **Inicio**, hacé clic en **"Solicitar préstamo"**.
2. Elegí el **dispositivo** (solo aparecen los disponibles en ese momento).
3. Elegí la **sede** y el **número de salón** donde lo vas a usar — el sistema arma la ubicación automáticamente (ej. "SJ-217").
4. Definí la **fecha de inicio** y la **fecha de fin estimada**. Podés pedirlo para ahora mismo o reservarlo para más adelante.
5. Enviá la solicitud. Queda en estado **Pendiente** hasta que un Administrador o Monitor la revise.

### Consultar tus solicitudes
1. Hacé clic en **"Mis Solicitudes"** en el menú (o **"Ver mis solicitudes"** desde Inicio).
2. Vas a ver únicamente tus propias solicitudes, con su estado:
   - **Pendiente:** todavía no fue revisada.
   - **Reservado:** ya fue aprobada, pero la fecha de inicio todavía no llegó.
   - **En curso:** aprobada y dentro de la ventana de fecha (la tenés en tu poder ahora).
   - **Vencido:** la fecha de fin ya pasó y todavía no la devolviste — conviene devolverla cuanto antes.
   - **Rechazado** / **Devuelto.**

### Cancelar una solicitud pendiente
Mientras la solicitud esté en estado **Pendiente**, vas a ver un botón **Cancelar** en esa fila — te permite retirarla vos mismo sin esperar a que la revise un administrador.

### Devolver un dispositivo
Cuando tu solicitud está **Reservada** o **En curso**, vas a ver un botón:
- **"Cancelar reserva"** si todavía no llegó la fecha de inicio.
- **"Devolver"** si ya la tenés en uso.

Hacé clic ahí en cuanto termines de usar el dispositivo (o si decidís no usarlo). El dispositivo vuelve a quedar disponible para otros.

---

## 4. Rol Administrativo

Funcionalmente **idéntico al rol Docente** — mismo menú (**Inicio, Mis Solicitudes**), mismas acciones: solicitar préstamo, consultar y cancelar/devolver sus propias solicitudes. Seguí exactamente los mismos pasos descritos en la sección "3. Rol Docente".

---

## 5. Rol Monitor

### Qué puede hacer
El Monitor ve en su menú: **Inicio, Dispositivos, Préstamos, Corte Diario.**

- Aprobar o rechazar solicitudes de préstamo de cualquier usuario (igual que un Administrador en esta función puntual).
- Consultar el inventario de dispositivos y ver quién tiene cada uno en este momento — **no puede crear, editar ni eliminar dispositivos**, solo consultarlos.
- Generar y consultar el historial de Corte Diario.
- No tiene acceso a Usuarios, Auditoría, Reportes ni Solicitudes Clave.

### Aprobar o rechazar una solicitud de préstamo
Mismos pasos que el Administrador: entrá a **Préstamos**, buscá o filtrá la solicitud, y hacé clic en **Aprobar** o **Rechazar**. También podés usar el atajo **"Ver préstamos pendientes"** desde tu panel de Inicio, que te lleva directo a la lista filtrada por pendientes.

### Ver quién tiene un dispositivo
Igual que el Administrador: en **Dispositivos**, hacé clic en el ícono de información junto a un equipo para ver si está prestado, a quién, y quién aprobó ese préstamo.

### Generar un Corte Diario
Igual que para el Administrador y el Técnico: **Corte Diario → "Generar corte ahora"**.

---

## Resumen rápido — qué ve cada rol

| Rol | Secciones en el menú |
|---|---|
| Administrador | Inicio, Usuarios, Dispositivos, Préstamos, Mantenimiento, Auditoría, Reportes, Solicitudes Clave, Corte Diario |
| Técnico | Inicio, Mantenimiento, Corte Diario |
| Docente | Inicio, Mis Solicitudes |
| Administrativo | Inicio, Mis Solicitudes |
| Monitor | Inicio, Dispositivos, Préstamos, Corte Diario |
