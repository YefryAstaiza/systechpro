const API_BASE = window.location.origin + '/systechpro/api';
let usuarioActual = null;

document.addEventListener('DOMContentLoaded', () => {
    checkSession();
    setupEventListeners();
});

function checkSession() {
    fetch(`${API_BASE}/auth/sesion`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            if (data.authenticated && data.usuario) {
                usuarioActual = data.usuario;
                const rol = data.usuario.rol;
                if (rol !== 'TECNICO') {
                    if (rol === 'ADMINISTRADOR') {
                        window.location.href = 'admin.html';
                    } else {
                        window.location.href = 'index.html';
                    }
                } else {
                    document.getElementById('tecnico-user-name').textContent = data.usuario.nombre;
                    loadDashboard();
                }
            } else {
                window.location.href = 'index.html';
            }
        })
        .catch(() => window.location.href = 'index.html');
}

function setupEventListeners() {
    document.getElementById('btn-logout').addEventListener('click', handleLogout);
    document.getElementById('nav-inicio-btn').addEventListener('click', (e) => { e.preventDefault(); showSection('inicio'); });
    document.getElementById('nav-mantenimiento-btn').addEventListener('click', (e) => { e.preventDefault(); showSection('mantenimiento'); });
    document.getElementById('nav-historial-btn').addEventListener('click', (e) => { e.preventDefault(); showSection('historial'); });
    document.getElementById('btn-registrar-mantenimiento').addEventListener('click', () => showSection('mantenimiento'));
    document.getElementById('btn-ver-historial').addEventListener('click', () => showSection('historial'));
    document.getElementById('btn-cancelar-mantenimiento').addEventListener('click', () => showSection('inicio'));

    const form = document.getElementById('form-mantenimiento');
    form.addEventListener('submit', (e) => {
        e.preventDefault();
        registrarMantenimiento();
    });

    document.querySelector('.close-modal').addEventListener('click', closeModal);
    document.getElementById('modal-detail').addEventListener('click', (e) => {
        if (e.target === document.getElementById('modal-detail')) closeModal();
    });
}

function handleLogout() {
    fetch(`${API_BASE}/auth/logout`, { method: 'GET', credentials: 'include' })
        .then(() => window.location.href = 'index.html')
        .catch(() => window.location.href = 'index.html');
}

function showSection(section) {
    document.querySelectorAll('.panel').forEach(panel => panel.classList.remove('active'));
    document.querySelectorAll('.sidebar-nav .nav-link').forEach(link => link.classList.remove('active'));

    if (section === 'inicio') {
        document.getElementById('panel-inicio').classList.add('active');
        document.getElementById('nav-inicio-btn').classList.add('active');
        loadDashboard();
    } else if (section === 'mantenimiento') {
        document.getElementById('panel-mantenimiento').classList.add('active');
        document.getElementById('nav-mantenimiento-btn').classList.add('active');
        loadMantenimientoPanel();
    } else if (section === 'historial') {
        document.getElementById('panel-historial').classList.add('active');
        document.getElementById('nav-historial-btn').classList.add('active');
        loadHistorial();
    }
}

function loadDashboard() {
    fetch(`${API_BASE}/mantenimientos`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            const mantenimientos = Array.isArray(data) ? data : [];
            
            const dispositivosEnMantenimiento = mantenimientos.filter(m => m.estado === 'EN_PROCESO').length;
            const mantenimientosEnProceso = mantenimientos.filter(m => m.estado === 'EN_PROCESO').length;
            const mantenimientosFinalizados = mantenimientos.filter(m => m.estado === 'FINALIZADO').length;
            
            document.getElementById('card-dispositivos-mantenimiento').textContent = dispositivosEnMantenimiento;
            document.getElementById('card-mantenimientos-proceso').textContent = mantenimientosEnProceso;
            document.getElementById('card-mantenimientos-finalizados').textContent = mantenimientosFinalizados;
            
            const recientes = [...mantenimientos].sort((a, b) => new Date(b.fechaInicio) - new Date(a.fechaInicio)).slice(0, 5);
            renderRecientes(recientes);
        })
        .catch(err => {
            console.error('Error cargando dashboard:', err);
        });
}

function renderRecientes(items) {
    const tbody = document.querySelector('#tabla-recientes tbody');
    tbody.innerHTML = '';

    if (!items.length) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:#64748b;">No hay registros recientes.</td></tr>`;
        return;
    }

    items.forEach(item => {
        const estadoTexto = item.estado === 'EN_PROCESO' ? 'En proceso' : 'Finalizado';
        const estadoClass = item.estado === 'EN_PROCESO' ? 'status-en-proceso' : 'status-finalizado';
        
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${item.nombreDispositivo || 'ID: ' + item.idDispositivo}</td>
            <td>${item.tipo || 'N/A'}</td>
            <td>${item.ubicacion || 'N/A'}</td>
            <td>${formatFecha(item.fechaInicio)}</td>
            <td>${item.fechaFin ? formatFecha(item.fechaFin) : 'En curso'}</td>
            <td><span class="status-pill ${estadoClass}">${estadoTexto}</span></td>
            <td><button class="btn-secondary" onclick="verDetalle(${item.idMantenimiento})">Ver detalle</button></td>
        `;
        tbody.appendChild(tr);
    });
}

function loadMantenimientoPanel() {
    cargarDispositivos();
    cargarMantenimientosEnProceso();
}

function cargarDispositivos() {
    const select = document.getElementById('mantenimiento-dispositivo');
    select.innerHTML = '<option value="">Cargando dispositivos...</option>';

    fetch(`${API_BASE}/dispositivos`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            const disponibles = Array.isArray(data) ? data.filter(d => d.estado !== 'MANTENIMIENTO') : [];
            if (!disponibles.length) {
                select.innerHTML = '<option value="">No hay dispositivos disponibles</option>';
                return;
            }
            select.innerHTML = '<option value="">Selecciona un dispositivo</option>';
            disponibles.forEach(d => {
                const option = document.createElement('option');
                option.value = d.idDispositivo;
                option.textContent = `${d.nombre} (${d.tipo}) - ${d.estado}`;
                select.appendChild(option);
            });
        })
        .catch(err => {
            console.error('Error cargando dispositivos:', err);
            select.innerHTML = '<option value="">Error cargando dispositivos</option>';
        });
}

function cargarMantenimientosEnProceso() {
    fetch(`${API_BASE}/mantenimientos`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            const items = Array.isArray(data) ? data.filter(m => m.estado === 'EN_PROCESO') : [];
            renderMantenimientoProceso(items);
        })
        .catch(err => {
            console.error('Error cargando mantenimientos:', err);
        });
}

function renderMantenimientoProceso(items) {
    const tbody = document.querySelector('#tabla-proceso tbody');
    tbody.innerHTML = '';

    if (!items.length) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:#64748b;">No hay mantenimientos en proceso.</td></tr>`;
        return;
    }

    items.forEach(item => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${item.nombreDispositivo || 'ID: ' + item.idDispositivo}</td>
            <td>${item.tipo || 'N/A'}</td>
            <td>${item.ubicacion || 'N/A'}</td>
            <td>${formatFecha(item.fechaInicio)}</td>
            <td>${item.fechaFin ? formatFecha(item.fechaFin) : 'En curso'}</td>
            <td><span class="status-pill status-en-proceso">En proceso</span></td>
            <td>
                <button class="btn-secondary" onclick="verDetalle(${item.idMantenimiento})">Ver detalle</button>
                <button class="btn-primary" onclick="finalizarMantenimiento(${item.idMantenimiento})" style="background:#10b981; margin-left:8px;">Finalizar</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function loadHistorial() {
    fetch(`${API_BASE}/mantenimientos`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            const items = Array.isArray(data) ? data.filter(m => m.estado === 'FINALIZADO') : [];
            renderHistorial(items);
        })
        .catch(err => {
            console.error('Error cargando historial:', err);
        });
}

function renderHistorial(items) {
    const tbody = document.querySelector('#tabla-historial tbody');
    tbody.innerHTML = '';

    if (!items.length) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:#64748b;">No hay historial disponible.</td></tr>`;
        return;
    }

    items.forEach(item => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${item.nombreDispositivo || 'ID: ' + item.idDispositivo}</td>
            <td>${item.tipo || 'N/A'}</td>
            <td>${item.ubicacion || 'N/A'}</td>
            <td>${formatFecha(item.fechaInicio)}</td>
            <td>${item.fechaFin ? formatFecha(item.fechaFin) : 'N/A'}</td>
            <td><span class="status-pill status-finalizado">Finalizado</span></td>
            <td><button class="btn-secondary" onclick="verDetalle(${item.idMantenimiento})">Ver detalle</button></td>
        `;
        tbody.appendChild(tr);
    });
}

function registrarMantenimiento() {
    const idDispositivo = Number(document.getElementById('mantenimiento-dispositivo').value);
    const tipo = document.getElementById('mantenimiento-tipo').value;
    const descripcion = document.getElementById('mantenimiento-descripcion').value.trim();

    if (!idDispositivo || !tipo || !descripcion) {
        alert('Completa todos los campos antes de guardar.');
        return;
    }

    if (!usuarioActual || !usuarioActual.idUsuario) {
        alert('Error: No se pudo identificar al técnico.');
        return;
    }

    const ahora = new Date();
    const fechaInicio = ahora.getFullYear() + '-' + 
                       String(ahora.getMonth() + 1).padStart(2, '0') + '-' + 
                       String(ahora.getDate()).padStart(2, '0') + ' ' +
                       String(ahora.getHours()).padStart(2, '0') + ':' +
                       String(ahora.getMinutes()).padStart(2, '0') + ':' +
                       String(ahora.getSeconds()).padStart(2, '0');

    const data = {
        idDispositivo: idDispositivo,
        idUsuario: usuarioActual.idUsuario,
        tipo: tipo,
        descripcion: descripcion,
        fechaInicio: fechaInicio,
        estado: 'EN_PROCESO'
    };

    fetch(`${API_BASE}/mantenimientos`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(res => res.json())
    .then(data => {
        if (data.idMantenimiento || data.success) {
            alert('Mantenimiento registrado correctamente.');
            document.getElementById('form-mantenimiento').reset();
            loadDashboard();
            cargarMantenimientosEnProceso();
            showSection('inicio');
        } else {
            alert(data.error || 'Error al registrar mantenimiento.');
        }
    })
    .catch(err => {
        console.error('Error registrando mantenimiento:', err);
        alert('Error de conexión al registrar mantenimiento.');
    });
}

function finalizarMantenimiento(id) {
    if (!confirm('¿Estás seguro de finalizar este mantenimiento?')) {
        return;
    }

    const ahora = new Date();
    const fechaFin = ahora.getFullYear() + '-' + 
                    String(ahora.getMonth() + 1).padStart(2, '0') + '-' + 
                    String(ahora.getDate()).padStart(2, '0') + ' ' +
                    String(ahora.getHours()).padStart(2, '0') + ':' +
                    String(ahora.getMinutes()).padStart(2, '0') + ':' +
                    String(ahora.getSeconds()).padStart(2, '0');

    const data = {
        estado: 'FINALIZADO',
        fechaFin: fechaFin
    };

    fetch(`${API_BASE}/mantenimientos/${id}`, {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(res => res.json())
    .then(data => {
        if (data.success || data.idMantenimiento) {
            alert('Mantenimiento finalizado correctamente.');
            loadDashboard();
            cargarMantenimientosEnProceso();
            loadHistorial();
            showSection('inicio');
        } else {
            alert(data.error || 'Error al finalizar mantenimiento.');
        }
    })
    .catch(err => {
        console.error('Error finalizando mantenimiento:', err);
        alert('Error de conexión al finalizar mantenimiento.');
    });
}

function verDetalle(id) {
    fetch(`${API_BASE}/mantenimientos/${id}`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            if (data.error) {
                alert(data.error);
                return;
            }
            showModalDetalle(data);
        })
        .catch(err => {
            console.error('Error cargando detalle:', err);
        });
}

function showModalDetalle(data) {
    const body = document.getElementById('modal-detail-body');
    const estadoTexto = data.estado === 'FINALIZADO' ? 'Finalizado' : 'En proceso';
    const tipoTexto = data.tipo === 'CORRECTIVO' ? 'Correctivo' : 'Preventivo';

    body.innerHTML = `
        <h3>Detalle de mantenimiento</h3>
        <dl>
            <dt>Dispositivo</dt><dd>${data.nombreDispositivo || 'ID: ' + data.idDispositivo}</dd>
            <dt>Tipo</dt><dd>${tipoTexto}</dd>
            <dt>Ubicación</dt><dd>${data.ubicacion || 'N/A'}</dd>
            <dt>Fecha de inicio</dt><dd>${formatFecha(data.fechaInicio)}</dd>
            <dt>Fecha de finalización</dt><dd>${data.fechaFin ? formatFecha(data.fechaFin) : 'En curso'}</dd>
            <dt>Estado</dt><dd>${estadoTexto}</dd>
            <dt>Descripción</dt><dd>${data.descripcion || 'Sin descripción'}</dd>
        </dl>
    `;
    document.getElementById('modal-detail').classList.add('active');
}

function closeModal() {
    document.getElementById('modal-detail').classList.remove('active');
}

function formatFecha(value) {
    if (!value) return 'N/A';
    try {
        if (value.includes(' ')) {
            const [fecha, hora] = value.split(' ');
            const partes = fecha.split('-');
            if (partes.length === 3) {
                return `${partes[2]}/${partes[1]}/${partes[0]} ${hora.substring(0, 5)}`;
            }
        }
        const date = new Date(value);
        if (isNaN(date.getTime())) return value;
        return date.toLocaleDateString('es-CO', { year: 'numeric', month: '2-digit', day: '2-digit' }) +
               ' ' + date.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' });
    } catch(e) {
        return value;
    }
}