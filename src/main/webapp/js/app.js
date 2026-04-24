// SysTechPro - Aplicación Frontend
const API_BASE = 'http://localhost:8080/systechpro/api';
let session = null;

// Elementos del DOM
const loginView = document.getElementById('login-view');
const dashboardView = document.getElementById('dashboard-view');
const loginForm = document.getElementById('login-form');
const loginError = document.getElementById('login-error');
const modal = document.getElementById('modal');
const modalForm = document.getElementById('modal-form');

// Inicialización
document.addEventListener('DOMContentLoaded', () => {
    checkSession();
    setupEventListeners();
});

// Verificar sesión
function checkSession() {
    fetch(`${API_BASE}/auth/sesion`)
        .then(res => res.json())
        .then(data => {
            if (data.authenticated) {
                if (data.usuario && data.usuario.rol === 'ADMIN') {
                    window.location.href = 'admin.html';
                } else {
                    showDashboard(data.usuario);
                }
            } else {
                showLogin();
            }
        })
        .catch(() => showLogin());
}

// Configurar eventos
function setupEventListeners() {
    // Login
    loginForm.addEventListener('submit', handleLogin);
    
    // Logout
    document.getElementById('logout-btn').addEventListener('click', handleLogout);
    
    // Navegación
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.addEventListener('click', () => switchView(btn.dataset.view));
    });
    
    // Modal
    document.querySelector('.close-modal').addEventListener('click', closeModal);
    modal.addEventListener('click', (e) => {
        if (e.target === modal) closeModal();
    });
    
    // Botones agregar
    document.getElementById('add-dispositivo-btn').addEventListener('click', () => showDispositivoForm());
    document.getElementById('add-prestamo-btn').addEventListener('click', () => showPrestamoForm());
    document.getElementById('add-mantenimiento-btn').addEventListener('click', () => showMantenimientoForm());
    document.getElementById('add-usuario-btn').addEventListener('click', () => showUsuarioForm());
}

// Login
function handleLogin(e) {
    e.preventDefault();
    const correo = document.getElementById('correo').value;
    const password = document.getElementById('password').value;
    
    fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ correo, password }),
        credentials: 'include'
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            // Redirección a la interfaz de admin si el usuario es ADMIN
            if (data.usuario && data.usuario.rol === 'ADMIN') {
                // TODO: Pruebas: Verificar que el admin es redirigido correctamente tras login
                window.location.href = 'admin.html';
            } else {
                showDashboard(data.usuario);
            }
        } else {
            loginError.textContent = data.error || 'Error en el login';
        }
    })
    .catch(err => {
        loginError.textContent = 'Error de conexión';
    });
}

// Logout
function handleLogout() {
    fetch(`${API_BASE}/auth/logout`, {
        method: 'GET',
        credentials: 'include'
    })
    .then(() => showLogin())
    .catch(() => showLogin());
}

// Mostrar vistas
function showLogin() {
    loginView.classList.add('active');
    dashboardView.classList.remove('active');
}

function showDashboard(usuario) {
    loginView.classList.remove('active');
    dashboardView.classList.add('active');
    
    document.getElementById('user-name').textContent = usuario.nombre;
    document.getElementById('user-role').textContent = usuario.rol;
    
    // Ocultar usuarios si no es admin
    if (usuario.rol !== 'ADMIN') {
        document.getElementById('nav-usuarios').style.display = 'none';
    }
    
    loadDispositivos();
}

function switchView(view) {
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.toggle('active', btn.dataset.view === view);
    });
    
    document.querySelectorAll('.panel').forEach(panel => {
        panel.classList.remove('active');
    });
    
    document.getElementById(`${view}-panel`).classList.add('active');
    
    // Cargar datos según la vista
    if (view === 'dispositivos') loadDispositivos();
    else if (view === 'prestamos') loadPrestamos();
    else if (view === 'mantenimientos') loadMantenimientos();
    else if (view === 'usuarios') loadUsuarios();
}

// Modal
function showModal(content) {
    modalForm.innerHTML = content;
    modal.classList.add('active');
}

function closeModal() {
    modal.classList.remove('active');
}

// ==================== DISPOSITIVOS ====================
function loadDispositivos() {
    fetch(`${API_BASE}/dispositivos`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            if (Array.isArray(data)) {
                renderDispositivos(data);
            }
        })
        .catch(err => console.error(err));
}

function renderDispositivos(dispositivos) {
    const container = document.getElementById('dispositivos-list');
    if (dispositivos.length === 0) {
        container.innerHTML = '<p>No hay dispositivos registrados</p>';
        return;
    }
    
    let html = '<table><thead><tr><th>ID</th><th>Nombre</th><th>Tipo</th><th>Estado</th><th>Descripción</th><th>Acciones</th></tr></thead><tbody>';
    
    dispositivos.forEach(d => {
        html += `<tr>
            <td>${d.idDispositivo}</td>
            <td>${d.nombre}</td>
            <td>${d.tipo}</td>
            <td><span class="status status-${d.estado.toLowerCase()}">${d.estado}</span></td>
            <td>${d.descripcion || ''}</td>
            <td>
                <button class="action-btn edit-btn" onclick="editDispositivo(${d.idDispositivo})">Editar</button>
                <button class="action-btn delete-btn" onclick="deleteDispositivo(${d.idDispositivo})">Eliminar</button>
            </td>
        </tr>`;
    });
    
    html += '</tbody></table>';
    container.innerHTML = html;
}

function showDispositivoForm(dispositivo = null) {
    const isEdit = dispositivo !== null;
    let html = `<h3>${isEdit ? 'Editar' : 'Agregar'} Dispositivo</h3>
        <form id="dispositivo-form">
            <div class="form-group">
                <label>Nombre:</label>
                <input type="text" name="nombre" value="${dispositivo?.nombre || ''}" required>
            </div>
            <div class="form-group">
                <label>Tipo:</label>
                <select name="tipo" required>
                    <option value="Computador" ${dispositivo?.tipo === 'Computador' ? 'selected' : ''}>Computador</option>
                    <option value="Proyector" ${dispositivo?.tipo === 'Proyector' ? 'selected' : ''}>Proyector</option>
                    <option value="Impresora" ${dispositivo?.tipo === 'Impresora' ? 'selected' : ''}>Impresora</option>
                    <option value="Tablet" ${dispositivo?.tipo === 'Tablet' ? 'selected' : ''}>Tablet</option>
                    <option value="Otro" ${dispositivo?.tipo === 'Otro' ? 'selected' : ''}>Otro</option>
                </select>
            </div>
            <div class="form-group">
                <label>Estado:</label>
                <select name="estado" required>
                    <option value="DISPONIBLE" ${dispositivo?.estado === 'DISPONIBLE' ? 'selected' : ''}>Disponible</option>
                    <option value="PRESTADO" ${dispositivo?.estado === 'PRESTADO' ? 'selected' : ''}>Prestado</option>
                    <option value="MANTENIMIENTO" ${dispositivo?.estado === 'MANTENIMIENTO' ? 'selected' : ''}>Mantenimiento</option>
                </select>
            </div>
            <div class="form-group">
                <label>Descripción:</label>
                <textarea name="descripcion">${dispositivo?.descripcion || ''}</textarea>
            </div>
            <button type="submit">${isEdit ? 'Actualizar' : 'Guardar'}</button>
        </form>`;
    
    showModal(html);
    document.getElementById('dispositivo-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        const data = Object.fromEntries(formData);
        
        const url = isEdit ? `${API_BASE}/dispositivos/${dispositivo.idDispositivo}` : `${API_BASE}/dispositivos`;
        const method = isEdit ? 'PUT' : 'POST';
        
        fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
            credentials: 'include'
        })
        .then(res => res.json())
        .then(result => {
            closeModal();
            loadDispositivos();
        });
    });
}

function editDispositivo(id) {
    fetch(`${API_BASE}/dispositivos/${id}`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => showDispositivoForm(data));
}

function deleteDispositivo(id) {
    if (confirm('¿Está seguro de eliminar este dispositivo?')) {
        fetch(`${API_BASE}/dispositivos/${id}`, {
            method: 'DELETE',
            credentials: 'include'
        })
        .then(() => loadDispositivos());
    }
}

// ==================== PRÉSTAMOS ====================
function loadPrestamos() {
    fetch(`${API_BASE}/prestamos`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            if (Array.isArray(data)) {
                renderPrestamos(data);
            }
        });
}

function renderPrestamos(prestamos) {
    const container = document.getElementById('prestamos-list');
    if (prestamos.length === 0) {
        container.innerHTML = '<p>No hay préstamos registrados</p>';
        return;
    }
    
    let html = '<table><thead><tr><th>ID</th><th>Usuario</th><th>Dispositivo</th><th>Salón</th><th>Fecha Inicio</th><th>Fecha Fin</th><th>Estado</th><th>Acciones</th></tr></thead><tbody>';
    
    prestamos.forEach(p => {
        html += `<tr>
            <td>${p.idPrestamo}</td>
            <td>${p.idUsuario}</td>
            <td>${p.idDispositivo}</td>
            <td>${p.idSalon || '-'}</td>
            <td>${p.fechaInicio}</td>
            <td>${p.fechaFin || '-'}</td>
            <td><span class="status status-${p.estado.toLowerCase()}">${p.estado}</span></td>
            <td>
                <button class="action-btn edit-btn" onclick="editPrestamo(${p.idPrestamo})">Editar</button>
                <button class="action-btn delete-btn" onclick="deletePrestamo(${p.idPrestamo})">Eliminar</button>
            </td>
        </tr>`;
    });
    
    html += '</tbody></table>';
    container.innerHTML = html;
}

function showPrestamoForm(prestamo = null) {
    const isEdit = prestamo !== null;
    let html = `<h3>${isEdit ? 'Editar' : 'Nuevo'} Préstamo</h3>
        <form id="prestamo-form">
            <div class="form-group">
                <label>Usuario ID:</label>
                <input type="number" name="idUsuario" value="${prestamo?.idUsuario || ''}" required>
            </div>
            <div class="form-group">
                <label>Dispositivo ID:</label>
                <input type="number" name="idDispositivo" value="${prestamo?.idDispositivo || ''}" required>
            </div>
            <div class="form-group">
                <label>Salón ID:</label>
                <input type="number" name="idSalon" value="${prestamo?.idSalon || ''}">
            </div>
            <div class="form-group">
                <label>Fecha Inicio:</label>
                <input type="date" name="fechaInicio" value="${prestamo?.fechaInicio || ''}" required>
            </div>
            <div class="form-group">
                <label>Fecha Fin:</label>
                <input type="date" name="fechaFin" value="${prestamo?.fechaFin || ''}">
            </div>
            <div class="form-group">
                <label>Estado:</label>
                <select name="estado" required>
                    <option value="PENDIENTE" ${prestamo?.estado === 'PENDIENTE' ? 'selected' : ''}>Pendiente</option>
                    <option value="APROBADO" ${prestamo?.estado === 'APROBADO' ? 'selected' : ''}>Aprobado</option>
                    <option value="DEVUELTO" ${prestamo?.estado === 'DEVUELTO' ? 'selected' : ''}>Devuelto</option>
                    <option value="CANCELADO" ${prestamo?.estado === 'CANCELADO' ? 'selected' : ''}>Cancelado</option>
                </select>
            </div>
            <button type="submit">${isEdit ? 'Actualizar' : 'Guardar'}</button>
        </form>`;
    
    showModal(html);
    document.getElementById('prestamo-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        const data = Object.fromEntries(formData);
        
        const url = isEdit ? `${API_BASE}/prestamos/${prestamo.idPrestamo}` : `${API_BASE}/prestamos`;
        const method = isEdit ? 'PUT' : 'POST';
        
        fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
            credentials: 'include'
        })
        .then(res => res.json())
        .then(result => {
            closeModal();
            loadPrestamos();
        });
    });
}

function editPrestamo(id) {
    fetch(`${API_BASE}/prestamos/${id}`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => showPrestamoForm(data));
}

function deletePrestamo(id) {
    if (confirm('¿Está seguro de eliminar este préstamo?')) {
        fetch(`${API_BASE}/prestamos/${id}`, {
            method: 'DELETE',
            credentials: 'include'
        })
        .then(() => loadPrestamos());
    }
}

// ==================== MANTENIMIENTOS ====================
function loadMantenimientos() {
    fetch(`${API_BASE}/mantenimientos`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            if (Array.isArray(data)) {
                renderMantenimientos(data);
            }
        });
}

function renderMantenimientos(mantenimientos) {
    const container = document.getElementById('mantenimientos-list');
    if (mantenimientos.length === 0) {
        container.innerHTML = '<p>No hay mantenimientos registrados</p>';
        return;
    }
    
    let html = '<table><thead><tr><th>ID</th><th>Dispositivo</th><th>Usuario</th><th>Tipo</th><th>Fecha Inicio</th><th>Fecha Fin</th><th>Estado</th><th>Acciones</th></tr></thead><tbody>';
    
    mantenimientos.forEach(m => {
        html += `<tr>
            <td>${m.idMantenimiento}</td>
            <td>${m.idDispositivo}</td>
            <td>${m.idUsuario}</td>
            <td>${m.tipo}</td>
            <td>${m.fechaInicio}</td>
            <td>${m.fechaFin || '-'}</td>
            <td><span class="status status-${m.estado.toLowerCase()}">${m.estado}</span></td>
            <td>
                <button class="action-btn edit-btn" onclick="editMantenimiento(${m.idMantenimiento})">Editar</button>
                <button class="action-btn delete-btn" onclick="deleteMantenimiento(${m.idMantenimiento})">Eliminar</button>
            </td>
        </tr>`;
    });
    
    html += '</tbody></table>';
    container.innerHTML = html;
}

function showMantenimientoForm(mantenimiento = null) {
    const isEdit = mantenimiento !== null;
    let html = `<h3>${isEdit ? 'Editar' : 'Nuevo'} Mantenimiento</h3>
        <form id="mantenimiento-form">
            <div class="form-group">
                <label>Dispositivo ID:</label>
                <input type="number" name="idDispositivo" value="${mantenimiento?.idDispositivo || ''}" required>
            </div>
            <div class="form-group">
                <label>Usuario ID:</label>
                <input type="number" name="idUsuario" value="${mantenimiento?.idUsuario || ''}" required>
            </div>
            <div class="form-group">
                <label>Tipo:</label>
                <select name="tipo" required>
                    <option value="PREVENTIVO" ${mantenimiento?.tipo === 'PREVENTIVO' ? 'selected' : ''}>Preventivo</option>
                    <option value="CORRECTIVO" ${mantenimiento?.tipo === 'CORRECTIVO' ? 'selected' : ''}>Correctivo</option>
                </select>
            </div>
            <div class="form-group">
                <label>Fecha Inicio:</label>
                <input type="date" name="fechaInicio" value="${mantenimiento?.fechaInicio || ''}" required>
            </div>
            <div class="form-group">
                <label>Fecha Fin:</label>
                <input type="date" name="fechaFin" value="${mantenimiento?.fechaFin || ''}">
            </div>
            <div class="form-group">
                <label>Descripción:</label>
                <textarea name="descripcion">${mantenimiento?.descripcion || ''}</textarea>
            </div>
            <div class="form-group">
                <label>Estado:</label>
                <select name="estado" required>
                    <option value="PENDIENTE" ${mantenimiento?.estado === 'PENDIENTE' ? 'selected' : ''}>Pendiente</option>
                    <option value="EN_PROCESO" ${mantenimiento?.estado === 'EN_PROCESO' ? 'selected' : ''}>En Proceso</option>
                    <option value="COMPLETADO" ${mantenimiento?.estado === 'COMPLETADO' ? 'selected' : ''}>Completado</option>
                </select>
            </div>
            <button type="submit">${isEdit ? 'Actualizar' : 'Guardar'}</button>
        </form>`;
    
    showModal(html);
    document.getElementById('mantenimiento-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        const data = Object.fromEntries(formData);
        
        const url = isEdit ? `${API_BASE}/mantenimientos/${mantenimiento.idMantenimiento}` : `${API_BASE}/mantenimientos`;
        const method = isEdit ? 'PUT' : 'POST';
        
        fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
            credentials: 'include'
        })
        .then(res => res.json())
        .then(result => {
            closeModal();
            loadMantenimientos();
        });
    });
}

function editMantenimiento(id) {
    fetch(`${API_BASE}/mantenimientos/${id}`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => showMantenimientoForm(data));
}

function deleteMantenimiento(id) {
    if (confirm('¿Está seguro de eliminar este mantenimiento?')) {
        fetch(`${API_BASE}/mantenimientos/${id}`, {
            method: 'DELETE',
            credentials: 'include'
        })
        .then(() => loadMantenimientos());
    }
}

// ==================== USUARIOS ====================
function loadUsuarios() {
    fetch(`${API_BASE}/usuarios`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            if (Array.isArray(data)) {
                renderUsuarios(data);
            }
        });
}

function renderUsuarios(usuarios) {
    const container = document.getElementById('usuarios-list');
    if (usuarios.length === 0) {
        container.innerHTML = '<p>No hay usuarios registrados</p>';
        return;
    }
    
    let html = '<table><thead><tr><th>ID</th><th>Nombre</th><th>Correo</th><th>Rol</th><th>Acciones</th></tr></thead><tbody>';
    
    usuarios.forEach(u => {
        html += `<tr>
            <td>${u.idUsuario}</td>
            <td>${u.nombre}</td>
            <td>${u.correo}</td>
            <td>${u.rol}</td>
            <td>
                <button class="action-btn edit-btn" onclick="editUsuario(${u.idUsuario})">Editar</button>
                <button class="action-btn delete-btn" onclick="deleteUsuario(${u.idUsuario})">Eliminar</button>
            </td>
        </tr>`;
    });
    
    html += '</tbody></table>';
    container.innerHTML = html;
}

function showUsuarioForm(usuario = null) {
    const isEdit = usuario !== null;
    let html = `<h3>${isEdit ? 'Editar' : 'Agregar'} Usuario</h3>
        <form id="usuario-form">
            <div class="form-group">
                <label>Nombre:</label>
                <input type="text" name="nombre" value="${usuario?.nombre || ''}" required>
            </div>
            <div class="form-group">
                <label>Correo:</label>
                <input type="email" name="correo" value="${usuario?.correo || ''}" required>
            </div>
            ${!isEdit ? `<div class="form-group">
                <label>Contraseña:</label>
                <input type="password" name="contrasena" required>
            </div>` : ''}
            <div class="form-group">
                <label>Rol:</label>
                <select name="rol" required>
                    <option value="ADMIN" ${usuario?.rol === 'ADMIN' ? 'selected' : ''}>Administrador</option>
                    <option value="DOCENTE" ${usuario?.rol === 'DOCENTE' ? 'selected' : ''}>Docente</option>
                    <option value="ADMINISTRATIVO" ${usuario?.rol === 'ADMINISTRATIVO' ? 'selected' : ''}>Administrativo</option>
                    <option value="TECNICO" ${usuario?.rol === 'TECNICO' ? 'selected' : ''}>Técnico</option>
                </select>
            </div>
            <button type="submit">${isEdit ? 'Actualizar' : 'Guardar'}</button>
        </form>`;
    
    showModal(html);
    document.getElementById('usuario-form').addEventListener('submit', (e) => {
        e.preventDefault();
        const formData = new FormData(e.target);
        const data = Object.fromEntries(formData);
        
        const url = isEdit ? `${API_BASE}/usuarios/${usuario.idUsuario}` : `${API_BASE}/usuarios`;
        const method = isEdit ? 'PUT' : 'POST';
        
        fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
            credentials: 'include'
        })
        .then(res => res.json())
        .then(result => {
            closeModal();
            loadUsuarios();
        });
    });
}

function editUsuario(id) {
    fetch(`${API_BASE}/usuarios/${id}`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => showUsuarioForm(data));
}

function deleteUsuario(id) {
    if (confirm('¿Está seguro de eliminar este usuario?')) {
        fetch(`${API_BASE}/usuarios/${id}`, {
            method: 'DELETE',
            credentials: 'include'
        })
        .then(() => loadUsuarios());
    }
}