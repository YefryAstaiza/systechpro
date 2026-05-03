// admin.js - Lógica para el panel de administración

const apiBase = window.location.origin + '/systechpro/api';
let rolGlobal = '';
let idUsuarioGlobal = '';
let prestamosDashboard = [];
let dashSolicitudesPage = 1;
const DASH_SOLICITUDES_PER_PAGE = 5;
let tecnicoRecientesPage = 1;
const TECNICO_RECIENTES_PER_PAGE = 5;
let usuariosActuales = [];
let usuariosPage = 1;
let auditoriaActuales = [];
let auditoriaPage = 1;
let prestamosPanelPage = 1;
let mantenimientosPage = 1;
const PANEL_PAGE_SIZE = 10;

const permisos = {
    ADMINISTRADOR: ['inicio','usuarios','dispositivos','prestamos','mantenimientos','auditoria','reportes'],
    TECNICO: ['inicio','mantenimientos'],
    DOCENTE: ['inicio','mis-solicitudes'],
    ADMINISTRATIVO: ['inicio','mis-solicitudes']
};

const modulos = {
    inicio: 'nav-inicio-btn',
    usuarios: 'nav-usuarios-btn',
    dispositivos: 'nav-dispositivos-btn',
    prestamos: 'nav-prestamos-btn',
    mantenimientos: 'nav-mantenimientos-btn',
    auditoria: 'nav-auditoria-btn',
    reportes: 'nav-reportes-btn',
    historial: 'nav-historial-btn',
    'mis-solicitudes': 'nav-mis-solicitudes-btn'
};

const panelInicioByRol = {
    ADMINISTRADOR: 'panel-dashboard',
    TECNICO: 'panel-inicio-tecnico',
    DOCENTE: 'panel-inicio-docente',
    ADMINISTRATIVO: 'panel-inicio-docente'
};

function aplicarPermisosPorRol(rol) {
    const permitidos = permisos[rol] || [];

    Object.values(modulos).forEach(id => {
        const el = document.getElementById(id);
        if (el) el.style.display = 'none';
    });

    permitidos.forEach(mod => {
        const id = modulos[mod];
        const el = document.getElementById(id);
        if (el) el.style.display = 'flex';
    });
}

function ocultarTodosLosPaneles() {
    document.querySelectorAll('.dashboard-panel').forEach(panel => panel.style.display = 'none');
    document.querySelectorAll('.sidebar-nav .nav-link').forEach(link => link.classList.remove('active'));
}

function getNavIdForSection(sectionId) {
    const map = {
        'panel-dashboard': 'nav-inicio-btn',
        'panel-inicio-tecnico': 'nav-inicio-btn',
        'panel-inicio-docente': 'nav-inicio-btn',
        'panel-usuarios': 'nav-usuarios-btn',
        'panel-dispositivos': 'nav-dispositivos-btn',
        'panel-prestamos': 'nav-prestamos-btn',
        'panel-mantenimientos': 'nav-mantenimientos-btn',
        'panel-auditoria': 'nav-auditoria-btn',
        'panel-reportes': 'nav-reportes-btn'
    };
    return map[sectionId] || null;
}

function mostrarPanel(sectionId, activeNavId = null) {
    ocultarTodosLosPaneles();
    const section = document.getElementById(sectionId);
    if (section) section.style.display = 'block';

    const navId = activeNavId || getNavIdForSection(sectionId);
    if (navId) {
        const nav = document.getElementById(navId);
        if (nav) nav.classList.add('active');
    }
}

function cargarPanelInicial(rol) {
    if (rol === 'ADMINISTRADOR') {
        mostrarPanel('panel-dashboard');
    } else if (rol === 'TECNICO') {
        mostrarPanel('panel-inicio-tecnico');
    } else {
        mostrarPanel('panel-inicio-docente');
    }
}

function actualizarTituloInicio(rol) {
    const titulo = document.getElementById('titulo-inicio');
    if (!titulo) return;

    if (rol === 'TECNICO') {
        titulo.textContent = 'Inicio Técnico';
    } else {
        titulo.textContent = 'Inicio';
    }
}

function renderLayoutByRole() {
    if (!rolGlobal) return;
    aplicarPermisosPorRol(rolGlobal);
    actualizarTituloInicio(rolGlobal);
    cargarPanelInicial(rolGlobal);
    actualizarEncabezadosTablasPorRol();
    if (rolGlobal !== 'ADMINISTRADOR') {
        const panelUsuarios = document.getElementById('panel-usuarios');
        if (panelUsuarios) panelUsuarios.style.display = 'none';
        const panelAuditoria = document.getElementById('panel-auditoria');
        if (panelAuditoria) panelAuditoria.style.display = 'none';
    }
}

function obtenerRolActual() {
    if (rolGlobal) return rolGlobal;
    const usuarioJSON = localStorage.getItem('usuario');
    if (!usuarioJSON) return '';
    try {
        const usuario = JSON.parse(usuarioJSON);
        return usuario.rol || '';
    } catch (e) {
        return '';
    }
}

function actualizarEncabezadosTablasPorRol() {
    actualizarEncabezadoDispositivos();
    actualizarEncabezadoPrestamosPanel();
    actualizarEncabezadoMantenimientos();
}

function actualizarEncabezadoDispositivos() {
    const rol = obtenerRolActual();
    const table = document.getElementById('tabla-dispositivos');
    if (!table) return;
    const headerRow = table.querySelector('thead tr');
    if (!headerRow) return;

    if (rol === 'TECNICO') {
        headerRow.innerHTML = [
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;white-space:nowrap;">Código</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Nombre</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Tipo</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Estado</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Descripción</th>',
            '<th style="padding:12px 14px;text-align:center;font-weight:600;color:#475569;">Acciones</th>'
        ].join('');
    } else {
        headerRow.innerHTML = [
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;white-space:nowrap;">Código</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Nombre</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Tipo</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Estado</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Ubicación</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Fecha Creación</th>',
            '<th style="padding:12px 14px;text-align:center;font-weight:600;color:#475569;">Acciones</th>'
        ].join('');
    }
}

function actualizarEncabezadoPrestamosPanel() {
    const rol = obtenerRolActual();
    const table = document.getElementById('tabla-prestamos-panel');
    if (!table) return;
    const headerRow = table.querySelector('thead tr');
    if (!headerRow) return;

    if (rol === 'DOCENTE' || rol === 'ADMINISTRATIVO' || rol === 'TECNICO') {
        headerRow.innerHTML = [
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;"># ID</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Dispositivo</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Salón</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Fecha Inicio</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Fecha Fin</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Estado</th>',
            '<th style="padding:12px 14px;text-align:center;font-weight:600;color:#475569;">Acciones</th>'
        ].join('');
    } else {
        headerRow.innerHTML = [
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;"># ID</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Usuario</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Dispositivo</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Salón</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Fecha Inicio</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Fecha Fin</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Estado</th>',
            '<th style="padding:12px 14px;text-align:center;font-weight:600;color:#475569;">Acciones</th>'
        ].join('');
    }
}

function actualizarEncabezadoMantenimientos() {
    const rol = obtenerRolActual();
    const table = document.querySelector('#panel-mantenimientos table');
    if (!table) return;
    const headerRow = table.querySelector('thead tr');
    if (!headerRow) return;

    if (rol === 'TECNICO') {
        headerRow.innerHTML = [
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;"># ID</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Dispositivo</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Tipo</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Inicio</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Fecha Fin</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Estado</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Descripción</th>',
            '<th style="padding:12px 14px;text-align:center;font-weight:600;color:#475569;">Acción</th>'
        ].join('');
    } else if (rol === 'ADMINISTRADOR') {
        headerRow.innerHTML = [
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;"># ID</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Dispositivo</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Técnico</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Tipo</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Inicio</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Fecha Fin</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Estado</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Descripción</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Fecha Creación</th>',
            '<th style="padding:12px 14px;text-align:center;font-weight:600;color:#475569;">Acción</th>'
        ].join('');
    } else {
        headerRow.innerHTML = [
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Dispositivo</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Técnico</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Tipo</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Inicio</th>',
            '<th style="padding:12px 14px;text-align:left;font-weight:600;color:#475569;">Estado</th>',
            '<th style="padding:12px 14px;text-align:center;font-weight:600;color:#475569;">Acción</th>'
        ].join('');
    }
}

document.addEventListener('DOMContentLoaded', function() {
    
    // Verificar sesión (Opcional, pero recomendado para obtener nombre del admin)
    fetch(`${apiBase}/auth/sesion`)
        .then(res => res.json())
        .then(data => {
            if (data.authenticated && data.usuario) {
                // Guardar usuario en localStorage para que auth.js pueda accederlo
                localStorage.setItem('usuario', JSON.stringify(data.usuario));
                
                const userNameEl = document.getElementById('admin-user-name');
                if (userNameEl) userNameEl.textContent = data.usuario.nombre;
                rolGlobal = data.usuario.rol;
                idUsuarioGlobal = data.usuario.idUsuario;
                renderLayoutByRole();

                if (rolGlobal === 'ADMINISTRADOR') {
                    cargarDispositivos();
                    cargarPrestamos();
                } else if (rolGlobal === 'TECNICO') {
                    cargarMantenimientos();
                } else if (rolGlobal === 'DOCENTE' || rolGlobal === 'ADMINISTRATIVO') {
                    cargarMisSolicitudes();
                }
            } else {
                window.location.href = 'index.html';
            }
        });

    // Lógica para cerrar sesión
    const logoutBtn = document.getElementById('btn-logout');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function() {
            fetch(`${apiBase}/auth/logout`, { method: 'GET' })
            .then(() => window.location.href = 'index.html')
            .catch(() => window.location.href = 'index.html');
        });
    }

    // Navegación de vistas
    const navInicioBtn = document.getElementById('nav-inicio-btn');
    const navUsuariosBtn = document.getElementById('nav-usuarios-btn');
    const navDispositivosBtn = document.getElementById('nav-dispositivos-btn');
    
    const panelDashboard = document.getElementById('panel-dashboard');
    const panelUsuarios = document.getElementById('panel-usuarios');
    const panelDispositivos = document.getElementById('panel-dispositivos');
    const panelPrestamos = document.getElementById('panel-prestamos');
    const panelMantenimientos = document.getElementById('panel-mantenimientos');
    const panelAuditoria = document.getElementById('panel-auditoria');
    const panelReportes = document.getElementById('panel-reportes');

    const navPrestamosBtn = document.getElementById('nav-prestamos-btn');
    const navMisSolicitudesBtn = document.getElementById('nav-mis-solicitudes-btn');
    const navHistorialBtn = document.getElementById('nav-historial-btn');
    const navMantenimientosBtn = document.getElementById('nav-mantenimientos-btn');
    const navAuditoriaBtn = document.getElementById('nav-auditoria-btn');
    const navReportesBtn = document.getElementById('nav-reportes-btn');

    function ocultarPaneles() {
        ocultarTodosLosPaneles();
    }

    navInicioBtn.addEventListener('click', (e) => {
        e.preventDefault();
        renderLayoutByRole();
    });

    navUsuariosBtn.addEventListener('click', (e) => {
        e.preventDefault();
        mostrarPanel('panel-usuarios');
        cargarUsuarios();
    });

    document.getElementById('quick-add-user-btn').addEventListener('click', () => {
        mostrarPanel('panel-usuarios');
        cargarUsuarios();
        abrirModalUsuario();
    });

    navDispositivosBtn.addEventListener('click', (e) => {
        e.preventDefault();
        mostrarPanel('panel-dispositivos');
        cargarDispositivos();
    });

    if (navPrestamosBtn) {
        navPrestamosBtn.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-prestamos');
            cargarPrestamosPanelDedicado();
        });
    }

    if (navMisSolicitudesBtn) {
        navMisSolicitudesBtn.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-prestamos', 'nav-mis-solicitudes-btn');
            cargarPrestamosPanelDedicado();
        });
    }

    if (navHistorialBtn) {
        navHistorialBtn.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-mantenimientos', 'nav-historial-btn');
            cargarMantenimientos();
        });
    }

    document.getElementById('quick-add-dispositivo-btn').addEventListener('click', () => {
        mostrarPanel('panel-dispositivos');
        cargarDispositivos();
        setTimeout(() => abrirFormDispositivo(null), 100);
    });

    const btnRegistrarMantenimiento = document.getElementById('btn-registrar-mantenimiento');
    const btnVerHistorial = document.getElementById('btn-ver-historial');
    const btnCrearPrestamoDocente = document.getElementById('btn-crear-prestamo-docente');
    const btnVerSolicitudesDocente = document.getElementById('btn-ver-solicitudes-docente');

    if (btnRegistrarMantenimiento) {
        btnRegistrarMantenimiento.addEventListener('click', abrirModalCrearMantenimiento);
    }

    if (btnVerHistorial) {
        btnVerHistorial.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-mantenimientos', 'nav-historial-btn');
            cargarMantenimientos();
        });
    }

    if (btnCrearPrestamoDocente) {
        btnCrearPrestamoDocente.addEventListener('click', abrirModalCrearPrestamo);
    }

    if (btnVerSolicitudesDocente) {
        btnVerSolicitudesDocente.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-prestamos', 'nav-mis-solicitudes-btn');
            cargarPrestamosPanelDedicado();
        });
    }

    const verSolicitudesBtn = document.getElementById('ver-solicitudes-btn');
    if (verSolicitudesBtn) {
        verSolicitudesBtn.addEventListener('click', () => {
            mostrarPanel('panel-prestamos');
            if (navPrestamosBtn) navPrestamosBtn.classList.add('active');
            cargarPrestamosPanelDedicado();
        });
    }

    if (navMantenimientosBtn) {
        navMantenimientosBtn.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-mantenimientos');
            cargarMantenimientos();
        });
    }

    if (navAuditoriaBtn) {
        navAuditoriaBtn.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-auditoria');
            cargarAuditoria();
        });
    }

    if (navReportesBtn) {
        navReportesBtn.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-reportes');
            cargarReportes();
        });
    }

    // ---------------------------------------------
    // LOGICA CRUD USUARIOS
    // ---------------------------------------------
    const btnNuevoUsuario = document.getElementById('btn-nuevo-usuario');
    const modalUsuario = document.getElementById('modal-usuario');
    const formUsuario = document.getElementById('form-usuario');
    const btnCancelarUsuario = document.getElementById('btn-cancelar-usuario');
    const tablaUsuariosBody = document.getElementById('tabla-usuarios-body');

    btnNuevoUsuario.addEventListener('click', abrirModalUsuario);
    btnCancelarUsuario.addEventListener('click', () => modalUsuario.style.display = 'none');

    function abrirModalUsuario(usuario = null) {
        formUsuario.reset();
        document.getElementById('usuario-id').value = '';
        
        if (usuario) {
            document.getElementById('modal-usuario-title').textContent = 'Editar Usuario';
            document.getElementById('usuario-id').value = usuario.idUsuario;
            document.getElementById('usuario-nombre').value = usuario.nombre;
            document.getElementById('usuario-correo').value = usuario.correo;
            document.getElementById('usuario-rol').value = usuario.rol;
            document.getElementById('usuario-password').required = false;
            document.getElementById('hint-password').textContent = '(Dejar en blanco para no cambiar)';
        } else {
            document.getElementById('modal-usuario-title').textContent = 'Crear Usuario';
            document.getElementById('usuario-password').required = true;
            document.getElementById('hint-password').textContent = '';
        }
        modalUsuario.style.display = 'flex';
    }

    function cargarUsuarios() {
        fetch(`${apiBase}/usuarios`)
            .then(res => res.json())
            .then(usuarios => {
                if(Array.isArray(usuarios)){
                    tablaUsuariosBody.innerHTML = '';
                    usuarios.forEach(u => {
                        const tr = document.createElement('tr');
                        tr.innerHTML = `
                            <td>${u.nombre}</td>
                            <td>${u.correo}</td>
                            <td><span class="badge" style="background:#2c3e50; color:white; padding:4px 8px; border-radius:12px; font-size:12px;">${u.rol}</span></td>
                            <td>
                                <button class="btn-editar" data-id="${u.idUsuario}" style="background:#f39c12; color:white; border:none; padding:5px 10px; cursor:pointer; border-radius:4px; margin-right:5px;">Editar</button>
                                <button class="btn-eliminar" data-id="${u.idUsuario}" style="background:#e74c3c; color:white; border:none; padding:5px 10px; cursor:pointer; border-radius:4px;">Eliminar</button>
                            </td>
                        `;
                        tablaUsuariosBody.appendChild(tr);
                    });

                    document.querySelectorAll('.btn-editar').forEach(btn => {
                        btn.addEventListener('click', (e) => {
                            const id = e.target.getAttribute('data-id');
                            const usuario = usuarios.find(user => user.idUsuario == id);
                            if(usuario) abrirModalUsuario(usuario);
                        });
                    });

                    document.querySelectorAll('.btn-eliminar').forEach(btn => {
                        btn.addEventListener('click', (e) => {
                            const id = e.target.getAttribute('data-id');
                            eliminarUsuario(id);
                        });
                    });
                }
            })
            .catch(error => console.error('Error cargando usuarios:', error));
    }

    formUsuario.addEventListener('submit', function(e) {
        e.preventDefault();
        const id = document.getElementById('usuario-id').value;
        const password = document.getElementById('usuario-password').value;
        
        const payload = {
            nombre: document.getElementById('usuario-nombre').value,
            correo: document.getElementById('usuario-correo').value,
            rol: document.getElementById('usuario-rol').value
        };

        if (password) {
            payload.contrasena = password;
        }

        const method = id ? 'PUT' : 'POST';
        const url = id ? `${apiBase}/usuarios/${id}` : `${apiBase}/usuarios`;

        fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(res => res.json().then(data => ({status: res.status, body: data})))
        .then(res => {
            if (res.status >= 200 && res.status < 300) {
                alert(res.body.mensaje);
                modalUsuario.style.display = 'none';
                cargarUsuarios();
            } else {
                alert(res.body.error || 'Ocurrió un error al guardar');
            }
        })
        .catch(err => {
            alert('Error de conexión con el servidor');
        });
    });

    function eliminarUsuario(id) {
        if (confirm('¿Estás seguro de que deseas eliminar este usuario? Esta acción no se puede deshacer.')) {
            fetch(`${apiBase}/usuarios/${id}`, {
                method: 'DELETE'
            })
            .then(res => res.json().then(data => ({status: res.status, body: data})))
            .then(res => {
                if (res.status >= 200 && res.status < 300) {
                    alert(res.body.mensaje);
                    cargarUsuarios();
                } else {
                    alert(res.body.error || 'Ocurrió un error al eliminar');
                }
            })
            .catch(err => alert('Error de conexión con el servidor'));
        }
    }

    // ---------------------------------------------
    // TOAST NOTIFICATIONS
    // ---------------------------------------------
    function showToast(message, type) {
        type = type || 'success';
        const container = document.getElementById('toast-container');
        const toast = document.createElement('div');
        const colors = { success: '#22c55e', error: '#ef4444', info: '#3b82f6', warning: '#f59e0b' };
        const icons = { success: '\u2713', error: '\u2715', info: '\u2139', warning: '\u26a0' };
        const c = colors[type] || colors.info;
        const ic = icons[type] || icons.info;
        toast.style.cssText = 'background:white;border-left:4px solid ' + c + ';border-radius:8px;padding:12px 18px;box-shadow:0 4px 20px rgba(0,0,0,0.15);display:flex;align-items:center;gap:10px;font-size:14px;font-weight:500;color:#1e293b;pointer-events:all;min-width:260px;max-width:380px;animation:slideIn .3s ease;';
        toast.innerHTML = '<span style="color:' + c + ';font-size:18px;font-weight:700;">' + ic + '</span><span>' + message + '</span>';
        container.appendChild(toast);
        setTimeout(function() {
            toast.style.animation = 'fadeOut .3s ease';
            setTimeout(function() { toast.remove(); }, 300);
        }, 3500);
    }
    var toastStyle = document.createElement('style');
    toastStyle.textContent = '@keyframes slideIn{from{transform:translateX(120%);opacity:0}to{transform:translateX(0);opacity:1}}@keyframes fadeOut{from{opacity:1}to{opacity:0;transform:translateX(120%)}}';
    document.head.appendChild(toastStyle);

    // ---------------------------------------------
    // LOGICA CRUD DISPOSITIVOS Y DASHBOARD
    // ---------------------------------------------
    const formDispositivo = document.getElementById('form-dispositivo');
    const tablaDispositivosBody = document.getElementById('tabla-dispositivos-body');
    var todosLosDispositivos = [];
    var dispPaginaActual = 1;
    var DISP_POR_PAGINA = 10;

    document.getElementById('btn-nuevo-dispositivo').addEventListener('click', function() { abrirFormDispositivo(null); });
    document.getElementById('btn-cancelar-dispositivo').addEventListener('click', resetFormDispositivo);
    document.getElementById('disp-buscar').addEventListener('input', aplicarFiltrosDispositivos);
    document.getElementById('disp-filtro-estado').addEventListener('change', aplicarFiltrosDispositivos);
    document.getElementById('btn-disp-prev').addEventListener('click', function() {
        if (dispPaginaActual > 1) { dispPaginaActual--; renderTablaDispositivos(); }
    });
    document.getElementById('btn-disp-next').addEventListener('click', function() {
        var filtered = getDispositivosFiltrados();
        if (dispPaginaActual < Math.ceil(filtered.length / DISP_POR_PAGINA)) { dispPaginaActual++; renderTablaDispositivos(); }
    });

    var selectSede = document.getElementById('disp-sede');
    var selectSalon = document.getElementById('disp-salon');
    var inputUbicacion = document.getElementById('disp-ubicacion-generada');

    selectSede.addEventListener('change', function() {
        var idSede = selectSede.value;
        inputUbicacion.value = '';
        if (!idSede) { selectSalon.innerHTML = '<option value="">Seleccione una sede primero</option>'; return; }
        fetch(apiBase + '/sedes/' + idSede + '/salones')
            .then(function(res) { return res.json(); })
            .then(function(salones) {
                selectSalon.innerHTML = '<option value="">Seleccione un sal\u00f3n</option>';
                salones.forEach(function(s) {
                    selectSalon.innerHTML += '<option value="' + s.idSalon + '" data-numero="' + s.numero + '">' + s.numero + '</option>';
                });
            });
    });

    selectSalon.addEventListener('change', function() {
        var opt = selectSalon.options[selectSalon.selectedIndex];
        var sedeOpt = selectSede.options[selectSede.selectedIndex];
        var codigoSede = sedeOpt ? sedeOpt.getAttribute('data-codigo') : '';
        var numSalon = opt ? opt.getAttribute('data-numero') : '';
        inputUbicacion.value = (codigoSede && numSalon) ? (codigoSede + '-' + numSalon) : '';
    });

    function cargarSedes() {
        selectSede.innerHTML = '<option value="">Cargando sedes...</option>';
        fetch(apiBase + '/sedes')
            .then(function(res) {
                if (res.status === 401) { selectSede.innerHTML = '<option value="">Sin sesi\u00f3n activa</option>'; return null; }
                return res.json();
            })
            .then(function(sedes) {
                if (!sedes) return;
                if (!Array.isArray(sedes) || sedes.length === 0) {
                    selectSede.innerHTML = '<option value="">Sin sedes registradas</option>';
                    return;
                }
                selectSede.innerHTML = '<option value="">Seleccione una sede...</option>';
                sedes.forEach(function(s) {
                    selectSede.innerHTML += '<option value="' + s.idSede + '" data-codigo="' + s.codigo + '">' + s.nombre + ' (' + s.codigo + ')</option>';
                });
            })
            .catch(function(err) {
                console.error('Error cargando sedes:', err);
                selectSede.innerHTML = '<option value="">Error al cargar sedes</option>';
            });
    }

    function abrirFormDispositivo(dispositivo) {
        formDispositivo.reset();
        document.getElementById('dispositivo-id').value = '';
        document.getElementById('disp-form-titulo').textContent = dispositivo ? 'Editar Dispositivo' : 'Registrar Nuevo Dispositivo';
        inputUbicacion.value = '';
        selectSalon.innerHTML = '<option value="">Seleccione una sede primero</option>';
        cargarSedes();
        if (dispositivo) {
            document.getElementById('dispositivo-id').value = dispositivo.idDispositivo;
            document.getElementById('dispositivo-nombre').value = dispositivo.nombre;
            document.getElementById('dispositivo-tipo').value = dispositivo.tipo;
            document.getElementById('dispositivo-estado').value = dispositivo.estado;
            document.getElementById('dispositivo-descripcion').value = dispositivo.descripcion || '';
            if (dispositivo.ubicacion && dispositivo.ubicacion !== '\u2014') {
                inputUbicacion.value = dispositivo.ubicacion;
            }
        }
    }

    function resetFormDispositivo() {
        formDispositivo.reset();
        document.getElementById('dispositivo-id').value = '';
        document.getElementById('disp-form-titulo').textContent = 'Gesti\u00f3n Detallada del Dispositivo';
        if(inputUbicacion) inputUbicacion.value = '';
        if(selectSalon) selectSalon.innerHTML = '<option value="">Seleccione una sede primero</option>';
    }

    function actualizarDashboard(dispositivos) {
        document.getElementById('dash-total').textContent = dispositivos.length;
        document.getElementById('dash-available').textContent = dispositivos.filter(function(d) { return d.estado === 'DISPONIBLE'; }).length;
        document.getElementById('dash-inuse').textContent = dispositivos.filter(function(d) { return d.estado === 'EN_USO'; }).length;
        document.getElementById('dash-maintenance').textContent = dispositivos.filter(function(d) { return d.estado === 'MANTENIMIENTO'; }).length;
    }

    function getEstadoBadgeDisp(estado) {
        var cfg = {
            'DISPONIBLE':    { bg: '#dcfce7', color: '#16a34a', label: 'Disponible' },
            'EN_USO':        { bg: '#fef9c3', color: '#ca8a04', label: 'En uso' },
            'MANTENIMIENTO': { bg: '#f3f4f6', color: '#374151', label: 'Mantenimiento' }
        };
        var s = cfg[estado] || { bg: '#e2e8f0', color: '#475569', label: estado };
        return '<span style="background:' + s.bg + ';color:' + s.color + ';padding:3px 10px;border-radius:12px;font-size:12px;font-weight:600;display:inline-flex;align-items:center;gap:5px;"><span style="width:7px;height:7px;border-radius:50%;background:' + s.color + ';display:inline-block;"></span>' + s.label + '</span>';
    }

    function getEstadoBadge(estado) {
        if(estado === 'DISPONIBLE' || estado === 'APROBADO') return '<span class="badge approved"><span class="dot"></span>' + estado + '</span>';
        if(estado === 'EN_USO' || estado === 'PENDIENTE') return '<span class="badge pending"><span class="dot"></span>' + estado + '</span>';
        if(estado === 'MANTENIMIENTO' || estado === 'RECHAZADO') return '<span class="badge rejected"><span class="dot"></span>' + estado + '</span>';
        return estado;
    }

    function getDispositivosFiltrados() {
        var buscar = (document.getElementById('disp-buscar').value || '').toLowerCase();
        var estado = document.getElementById('disp-filtro-estado').value || '';
        return todosLosDispositivos.filter(function(d) {
            var codigo = 'D' + String(d.idDispositivo).padStart(3, '0');
            var matchBuscar = !buscar || d.nombre.toLowerCase().includes(buscar) || codigo.toLowerCase().includes(buscar);
            var matchEstado = !estado || d.estado === estado;
            return matchBuscar && matchEstado;
        });
    }

    function aplicarFiltrosDispositivos() {
        dispPaginaActual = 1;
        renderTablaDispositivos();
    }

    function renderTablaDispositivos() {
        actualizarEncabezadoDispositivos();
        var filtered = getDispositivosFiltrados();
        var total = filtered.length;
        var totalPaginas = Math.max(1, Math.ceil(total / DISP_POR_PAGINA));
        var inicio = (dispPaginaActual - 1) * DISP_POR_PAGINA;
        var fin = Math.min(inicio + DISP_POR_PAGINA, total);
        var pagina = filtered.slice(inicio, fin);

        tablaDispositivosBody.innerHTML = '';
        if (pagina.length === 0) {
            var colspan = rol === 'ADMINISTRADOR' ? 7 : 6;
            tablaDispositivosBody.innerHTML = '<tr><td colspan="' + colspan + '" style="text-align:center;padding:30px;color:#94a3b8;">No se encontraron dispositivos</td></tr>';
        } else {
            var rol = obtenerRolActual();
            pagina.forEach(function(d) {
                var codigo = 'D' + String(d.idDispositivo).padStart(3, '0');
                var tr = document.createElement('tr');
                tr.style.cssText = 'border-bottom:1px solid #f1f5f9;transition:background .15s;';
                var fila = [
                    '<td style="padding:12px 14px;font-weight:600;color:#64748b;">' + codigo + '</td>',
                    '<td style="padding:12px 14px;font-weight:500;color:#1e293b;">' + d.nombre + '</td>',
                    '<td style="padding:12px 14px;color:#475569;">' + d.tipo + '</td>',
                    '<td style="padding:12px 14px;">' + getEstadoBadgeDisp(d.estado) + '</td>'
                ];
                if (rol === 'TECNICO') {
                    fila.push('<td style="padding:12px 14px;color:#64748b;font-size:13px;">' + (d.descripcion || '\u2014') + '</td>');
                } else {
                    fila.push('<td style="padding:12px 14px;color:#64748b;font-size:13px;">' + (d.ubicacion || '\u2014') + '</td>');
                    fila.push('<td style="padding:12px 14px;color:#64748b;font-size:13px;">' + (d.fechaCreacion ? formatearFecha(d.fechaCreacion) : '\u2014') + '</td>');
                }
                fila.push(
                    '<td style="padding:12px 14px;text-align:center;">',
                    '  <button class="btn-editar-disp" data-id="' + d.idDispositivo + '" title="Editar" style="background:none;border:none;cursor:pointer;padding:5px;color:#f59e0b;">',
                    '    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>',
                    '  </button>',
                    '  <button class="btn-eliminar-disp" data-id="' + d.idDispositivo + '" title="Eliminar" style="background:none;border:none;cursor:pointer;padding:5px;color:#ef4444;">',
                    '    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>',
                    '  </button>',
                    '</td>'
                );
                tr.innerHTML = fila.join('');
                tablaDispositivosBody.appendChild(tr);
            });

            document.querySelectorAll('.btn-editar-disp').forEach(function(btn) {
                btn.addEventListener('click', function(e) {
                    var id = e.currentTarget.getAttribute('data-id');
                    var disp = todosLosDispositivos.find(function(d) { return d.idDispositivo == id; });
                    if (disp) abrirFormDispositivo(disp);
                });
            });
            document.querySelectorAll('.btn-eliminar-disp').forEach(function(btn) {
                btn.addEventListener('click', function(e) { eliminarDispositivo(e.currentTarget.getAttribute('data-id')); });
            });
        }

        var infoPagina = document.getElementById('disp-info-pagina');
        var btnPrev = document.getElementById('btn-disp-prev');
        var btnNext = document.getElementById('btn-disp-next');
        if(infoPagina) infoPagina.textContent = total > 0 ? ('Mostrando ' + (inicio+1) + '\u2013' + fin + ' de ' + total + ' dispositivos') : 'Sin resultados';
        if(btnPrev) { btnPrev.disabled = dispPaginaActual <= 1; btnPrev.style.opacity = dispPaginaActual <= 1 ? '0.4' : '1'; }
        if(btnNext) { btnNext.disabled = dispPaginaActual >= totalPaginas; btnNext.style.opacity = dispPaginaActual >= totalPaginas ? '0.4' : '1'; }
    }

    function cargarDispositivos() {
        fetch(apiBase + '/dispositivos')
            .then(function(res) { return res.json(); })
            .then(function(dispositivos) {
                if (Array.isArray(dispositivos)) {
                    todosLosDispositivos = dispositivos;
                    actualizarDashboard(dispositivos);
                    renderTablaDispositivos();
                }
            })
            .catch(function(error) { console.error('Error cargando dispositivos:', error); });
    }

    formDispositivo.addEventListener('submit', function(e) {
        e.preventDefault();
        var id = document.getElementById('dispositivo-id').value;
        var payload = {
            nombre: document.getElementById('dispositivo-nombre').value,
            tipo: document.getElementById('dispositivo-tipo').value,
            estado: document.getElementById('dispositivo-estado').value,
            descripcion: document.getElementById('dispositivo-descripcion').value
        };
        var method = id ? 'PUT' : 'POST';
        var url = id ? (apiBase + '/dispositivos/' + id) : (apiBase + '/dispositivos');
        fetch(url, { method: method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) })
            .then(function(res) { return res.json().then(function(data) { return { status: res.status, body: data }; }); })
            .then(function(res) {
                if (res.status >= 200 && res.status < 300) {
                    showToast(res.body.mensaje || 'Dispositivo guardado correctamente', 'success');
                    resetFormDispositivo();
                    cargarDispositivos();
                } else {
                    showToast(res.body.error || 'Error al guardar dispositivo', 'error');
                }
            })
            .catch(function() { showToast('Error de conexi\u00f3n con el servidor', 'error'); });
    });

    function eliminarDispositivo(id) {
        var disp = todosLosDispositivos.find(function(d) { return d.idDispositivo == id; });
        if (disp && disp.estado === 'EN_USO') {
            showToast('No se puede eliminar un dispositivo que est\u00e1 EN USO', 'warning');
            return;
        }
        if (!confirm('\u00bfEliminar el dispositivo "' + (disp ? disp.nombre : '') + '"? Esta acci\u00f3n no se puede deshacer.')) return;
        fetch(apiBase + '/dispositivos/' + id, { method: 'DELETE' })
            .then(function(res) { return res.json().then(function(data) { return { status: res.status, body: data }; }); })
            .then(function(res) {
                if (res.status >= 200 && res.status < 300) {
                    showToast(res.body.mensaje || 'Dispositivo eliminado', 'success');
                    cargarDispositivos();
                } else {
                    showToast(res.body.error || 'Error al eliminar dispositivo', 'error');
                }
            })
            .catch(function() { showToast('Error de conexi\u00f3n con el servidor', 'error'); });
    }


    const tablaSolicitudesBody = document.getElementById('tabla-solicitudes-body');
    const tablaRecientesBody = document.getElementById('tabla-recientes-body');
    const tablaPrestamosPanelBody = document.getElementById('tabla-prestamos-panel-body');
    const modalCrearPrestamo = document.getElementById('modal-crear-prestamo');
    const formCrearPrestamo = document.getElementById('form-crear-prestamo');
    const modalDetallePrestamo = document.getElementById('modal-detalle-prestamo');
    
    let prestamosActuales = [];

    document.getElementById('btn-solicitar-prestamo-dash').addEventListener('click', abrirModalCrearPrestamo);
    document.getElementById('btn-cancelar-prestamo').addEventListener('click', () => modalCrearPrestamo.style.display = 'none');
    document.getElementById('btn-cerrar-detalle').addEventListener('click', () => modalDetallePrestamo.style.display = 'none');

    function formatearFecha(timestamp) {
        const d = new Date(timestamp);
        return d.toLocaleString('es-ES', { dateStyle: 'short', timeStyle: 'short' });
    }

    function renderTablaSolicitudesDashboard() {
        if (!tablaSolicitudesBody) return;
        const lista = Array.isArray(prestamosDashboard) ? prestamosDashboard.slice() : [];
        lista.sort((a, b) => new Date(b.fechaInicio || b.fechaCreacion || 0) - new Date(a.fechaInicio || a.fechaCreacion || 0));
        const total = lista.length;
        const totalPages = Math.max(1, Math.ceil(total / DASH_SOLICITUDES_PER_PAGE));
        if (dashSolicitudesPage > totalPages) dashSolicitudesPage = totalPages;
        const inicio = (dashSolicitudesPage - 1) * DASH_SOLICITUDES_PER_PAGE;
        const pagina = lista.slice(inicio, inicio + DASH_SOLICITUDES_PER_PAGE);
        tablaSolicitudesBody.innerHTML = '';
        if (pagina.length === 0) {
            tablaSolicitudesBody.innerHTML = '<tr><td colspan="5" style="text-align:center;padding:30px;color:#94a3b8;">No hay solicitudes</td></tr>';
        } else {
            pagina.forEach(p => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${p.nombreUsuario}</td>
                    <td>${p.nombreDispositivo}</td>
                    <td>${formatearFecha(p.fechaInicio)}</td>
                    <td>${getEstadoBadge(p.estado)}</td>
                    <td><button class="view-btn btn-ver-prestamo" data-id="${p.idPrestamo}">Ver</button></td>
                `;
                tablaSolicitudesBody.appendChild(tr);
            });
            document.querySelectorAll('.btn-ver-prestamo').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const id = parseInt(e.target.getAttribute('data-id'));
                    abrirModalDetallePrestamo(id);
                });
            });
        }
        const pageLabel = document.getElementById('dash-solicitudes-pagina');
        if (pageLabel) pageLabel.textContent = 'Página ' + dashSolicitudesPage + ' de ' + totalPages;
        const btnPrev = document.getElementById('btn-dash-solicitudes-prev');
        const btnNext = document.getElementById('btn-dash-solicitudes-next');
        if (btnPrev) btnPrev.disabled = dashSolicitudesPage <= 1;
        if (btnNext) btnNext.disabled = dashSolicitudesPage >= totalPages;
    }

    function renderTablaRecientesTecnico() {
        if (!tablaRecientesBody) return;
        const lista = Array.isArray(mantenimientosActuales) ? mantenimientosActuales.slice() : [];
        lista.sort((a, b) => new Date(b.fechaInicio || 0) - new Date(a.fechaInicio || 0));
        const total = lista.length;
        const totalPages = Math.max(1, Math.ceil(total / TECNICO_RECIENTES_PER_PAGE));
        if (tecnicoRecientesPage > totalPages) tecnicoRecientesPage = totalPages;
        const inicio = (tecnicoRecientesPage - 1) * TECNICO_RECIENTES_PER_PAGE;
        const pagina = lista.slice(inicio, inicio + TECNICO_RECIENTES_PER_PAGE);
        tablaRecientesBody.innerHTML = '';
        if (pagina.length === 0) {
            tablaRecientesBody.innerHTML = '<tr><td colspan="7" style="text-align:center;padding:30px;color:#94a3b8;">No hay mantenimientos recientes</td></tr>';
        } else {
            pagina.forEach(m => {
                const ubicacion = m.ubicacion || (m.nombreSede ? m.nombreSede + (m.numeroSalon ? ' - Salón ' + m.numeroSalon : '') : '-');
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${m.nombreDispositivo || m.idDispositivo || '-'}</td>
                    <td><span class="badge" style="background:#8e44ad; color:white; padding:4px 8px; border-radius:12px; font-size:12px;">${m.tipo}</span></td>
                    <td>${ubicacion}</td>
                    <td>${formatearFecha(m.fechaInicio)}</td>
                    <td>${m.fechaFin ? formatearFecha(m.fechaFin) : 'En curso'}</td>
                    <td>${getEstadoBadgeMantenimiento(m.estado)}</td>
                    <td style="text-align:center;"><button class="view-btn btn-ver-mantenimiento" data-id="${m.idMantenimiento}">Ver</button></td>
                `;
                tablaRecientesBody.appendChild(tr);
            });
            document.querySelectorAll('.btn-ver-mantenimiento').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const id = parseInt(e.target.getAttribute('data-id'));
                    abrirModalDetalleMantenimiento(id);
                });
            });
        }
        const pageLabel = document.getElementById('recientes-pagina');
        if (pageLabel) pageLabel.textContent = 'Página ' + tecnicoRecientesPage + ' de ' + totalPages;
        const btnPrev = document.getElementById('btn-recientes-prev');
        const btnNext = document.getElementById('btn-recientes-next');
        if (btnPrev) btnPrev.disabled = tecnicoRecientesPage <= 1;
        if (btnNext) btnNext.disabled = tecnicoRecientesPage >= totalPages;
    }

    function renderTablaUsuarios() {
        const tbody = document.getElementById('tabla-usuarios-body');
        if (!tbody) return;
        const lista = Array.isArray(usuariosActuales) ? usuariosActuales.slice() : [];
        lista.sort((a, b) => a.nombre.localeCompare(b.nombre));
        const total = lista.length;
        const totalPages = Math.max(1, Math.ceil(total / PANEL_PAGE_SIZE));
        if (usuariosPage > totalPages) usuariosPage = totalPages;
        const inicio = (usuariosPage - 1) * PANEL_PAGE_SIZE;
        const pagina = lista.slice(inicio, inicio + PANEL_PAGE_SIZE);
        tbody.innerHTML = '';
        if (pagina.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;padding:30px;color:#94a3b8;">No se encontraron usuarios</td></tr>';
        } else {
            pagina.forEach(u => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${u.nombre}</td>
                    <td>${u.correo}</td>
                    <td><span class="badge" style="background:#2c3e50; color:white; padding:4px 8px; border-radius:12px; font-size:12px;">${u.rol}</span></td>
                    <td>
                        <button class="btn-editar" data-id="${u.idUsuario}" style="background:#f39c12; color:white; border:none; padding:5px 10px; cursor:pointer; border-radius:4px; margin-right:5px;">Editar</button>
                        <button class="btn-eliminar" data-id="${u.idUsuario}" style="background:#e74c3c; color:white; border:none; padding:5px 10px; cursor:pointer; border-radius:4px;">Eliminar</button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
            document.querySelectorAll('.btn-editar').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const id = e.target.getAttribute('data-id');
                    const usuario = usuariosActuales.find(user => user.idUsuario == id);
                    if (usuario) abrirModalUsuario(usuario);
                });
            });
            document.querySelectorAll('.btn-eliminar').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const id = e.target.getAttribute('data-id');
                    eliminarUsuario(id);
                });
            });
        }
        const pageLabel = document.getElementById('usuarios-pagina');
        if (pageLabel) pageLabel.textContent = 'Página ' + usuariosPage + ' de ' + totalPages;
        const btnPrev = document.getElementById('btn-usuarios-prev');
        const btnNext = document.getElementById('btn-usuarios-next');
        if (btnPrev) btnPrev.disabled = usuariosPage <= 1;
        if (btnNext) btnNext.disabled = usuariosPage >= totalPages;
    }

    function renderTablaAuditoria() {
        const tbody = document.getElementById('tabla-auditoria-body');
        if (!tbody) return;
        const lista = Array.isArray(auditoriaActuales) ? auditoriaActuales.slice() : [];
        lista.sort((a, b) => new Date(b.fechaEvento || 0) - new Date(a.fechaEvento || 0));
        const total = lista.length;
        const totalPages = Math.max(1, Math.ceil(total / PANEL_PAGE_SIZE));
        if (auditoriaPage > totalPages) auditoriaPage = totalPages;
        const inicio = (auditoriaPage - 1) * PANEL_PAGE_SIZE;
        const pagina = lista.slice(inicio, inicio + PANEL_PAGE_SIZE);
        tbody.innerHTML = '';
        if (pagina.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:30px;color:#94a3b8;">No se encontraron registros de auditoría</td></tr>';
        } else {
            pagina.forEach(log => {
                let badgeColor = '#3498db';
                if (log.accion === 'INSERT') badgeColor = '#2ecc71';
                else if (log.accion === 'UPDATE') badgeColor = '#f39c12';
                else if (log.accion === 'DELETE') badgeColor = '#e74c3c';
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${formatearFecha(log.fechaEvento)}</td>
                    <td>${log.nombreUsuario}</td>
                    <td><span class="badge" style="background:${badgeColor}; color:white; padding:4px 8px; border-radius:12px; font-size:12px;">${log.accion}</span></td>
                    <td>${log.tablaAfectada}</td>
                    <td>${log.descripcion}</td>
                    <td>${log.ip}</td>
                `;
                tbody.appendChild(tr);
            });
        }
        const pageLabel = document.getElementById('auditoria-pagina');
        if (pageLabel) pageLabel.textContent = 'Página ' + auditoriaPage + ' de ' + totalPages;
        const btnPrev = document.getElementById('btn-auditoria-prev');
        const btnNext = document.getElementById('btn-auditoria-next');
        if (btnPrev) btnPrev.disabled = auditoriaPage <= 1;
        if (btnNext) btnNext.disabled = auditoriaPage >= totalPages;
    }

    function renderTablaMantenimientosPanel() {
        if (!tablaMantenimientosBody) return;
        const lista = Array.isArray(mantenimientosActuales) ? mantenimientosActuales.slice() : [];
        lista.sort((a, b) => new Date(b.fechaInicio || 0) - new Date(a.fechaInicio || 0));
        const total = lista.length;
        const totalPages = Math.max(1, Math.ceil(total / PANEL_PAGE_SIZE));
        if (mantenimientosPage > totalPages) mantenimientosPage = totalPages;
        const inicio = (mantenimientosPage - 1) * PANEL_PAGE_SIZE;
        const pagina = lista.slice(inicio, inicio + PANEL_PAGE_SIZE);
        tablaMantenimientosBody.innerHTML = '';
        const rol = obtenerRolActual();
        if (pagina.length === 0) {
            tablaMantenimientosBody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:30px;color:#94a3b8;">No se encontraron mantenimientos</td></tr>';
        } else {
            pagina.forEach(m => {
                const tr = document.createElement('tr');
                if (rol === 'TECNICO') {
                    tr.innerHTML = `
                        <td>#${m.idMantenimiento}</td>
                        <td>${m.nombreDispositivo || m.idDispositivo || '-'}</td>
                        <td><span class="badge" style="background:#8e44ad; color:white; padding:4px 8px; border-radius:12px; font-size:12px;">${m.tipo}</span></td>
                        <td>${formatearFecha(m.fechaInicio)}</td>
                        <td>${m.fechaFin ? formatearFecha(m.fechaFin) : 'En curso'}</td>
                        <td>${getEstadoBadgeMantenimiento(m.estado)}</td>
                        <td>${m.descripcion || '-'}</td>
                        <td style="text-align:center;"><button class="view-btn btn-ver-mantenimiento" data-id="${m.idMantenimiento}">Ver</button></td>
                    `;
                } else if (rol === 'ADMINISTRADOR') {
                    tr.innerHTML = `
                        <td>#${m.idMantenimiento}</td>
                        <td>${m.nombreDispositivo || m.idDispositivo || '-'}</td>
                        <td>${m.nombreUsuario || '-'}</td>
                        <td><span class="badge" style="background:#8e44ad; color:white; padding:4px 8px; border-radius:12px; font-size:12px;">${m.tipo}</span></td>
                        <td>${formatearFecha(m.fechaInicio)}</td>
                        <td>${m.fechaFin ? formatearFecha(m.fechaFin) : 'En curso'}</td>
                        <td>${getEstadoBadgeMantenimiento(m.estado)}</td>
                        <td>${m.descripcion || '-'}</td>
                        <td>${m.fechaCreacion ? formatearFecha(m.fechaCreacion) : '-'}</td>
                        <td style="text-align:center;"><button class="view-btn btn-ver-mantenimiento" data-id="${m.idMantenimiento}">Ver</button></td>
                    `;
                } else {
                    tr.innerHTML = `
                        <td>${m.nombreDispositivo}</td>
                        <td>${m.nombreUsuario}</td>
                        <td><span class="badge" style="background:#8e44ad; color:white; padding:4px 8px; border-radius:12px; font-size:12px;">${m.tipo}</span></td>
                        <td>${formatearFecha(m.fechaInicio)}</td>
                        <td>${getEstadoBadgeMantenimiento(m.estado)}</td>
                        <td style="text-align:center;"><button class="view-btn btn-ver-mantenimiento" data-id="${m.idMantenimiento}">Ver</button></td>
                    `;
                }
                tablaMantenimientosBody.appendChild(tr);
            });
            document.querySelectorAll('.btn-ver-mantenimiento').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const id = parseInt(e.target.getAttribute('data-id'));
                    abrirModalDetalleMantenimiento(id);
                });
            });
        }
        const pageLabel = document.getElementById('mantenimientos-pagina');
        if (pageLabel) pageLabel.textContent = 'Página ' + mantenimientosPage + ' de ' + totalPages;
        const btnPrev = document.getElementById('btn-mantenimientos-prev');
        const btnNext = document.getElementById('btn-mantenimientos-next');
        if (btnPrev) btnPrev.disabled = mantenimientosPage <= 1;
        if (btnNext) btnNext.disabled = mantenimientosPage >= totalPages;
    }

    function renderTablaPrestamosPanel() {
        if (!tablaPrestamosPanelBody) return;
        const rol = obtenerRolActual();
        let lista = Array.isArray(prestamosActuales) ? prestamosActuales.slice() : [];
        if (rol === 'DOCENTE' || rol === 'ADMINISTRATIVO') {
            lista = lista.filter(function(p) { return p.idUsuario == idUsuarioGlobal; });
        }
        lista.sort((a, b) => new Date(b.fechaInicio || 0) - new Date(a.fechaInicio || 0));
        const total = lista.length;
        const totalPages = Math.max(1, Math.ceil(total / PANEL_PAGE_SIZE));
        if (prestamosPanelPage > totalPages) prestamosPanelPage = totalPages;
        const inicio = (prestamosPanelPage - 1) * PANEL_PAGE_SIZE;
        const pagina = lista.slice(inicio, inicio + PANEL_PAGE_SIZE);
        tablaPrestamosPanelBody.innerHTML = '';
        if (pagina.length === 0) {
            const colspan = rol === 'DOCENTE' || rol === 'ADMINISTRATIVO' ? 7 : 8;
            tablaPrestamosPanelBody.innerHTML = '<tr><td colspan="' + colspan + '" style="text-align:center;padding:30px;color:#94a3b8;">No hay solicitudes de préstamo</td></tr>';
        } else {
            pagina.forEach(function(p) {
                const esAdminTec = (rolGlobal === 'ADMINISTRADOR' || rolGlobal === 'TECNICO');
                const esPend = p.estado === 'PENDIENTE';
                let acciones = '';
                if (esAdminTec && esPend) {
                    acciones += '<button onclick="aprobarPrestamoPanel(' + p.idPrestamo + ')" style="background:#22c55e;color:white;border:none;padding:4px 10px;border-radius:6px;font-size:12px;cursor:pointer;margin-right:4px;">✓ Aprobar</button>';
                    acciones += '<button onclick="rechazarPrestamoPanel(' + p.idPrestamo + ')" style="background:#ef4444;color:white;border:none;padding:4px 10px;border-radius:6px;font-size:12px;cursor:pointer;">✕ Rechazar</button>';
                } else if (esPend) {
                    acciones += '<button onclick="cancelarPrestamoPanel(' + p.idPrestamo + ')" style="background:#64748b;color:white;border:none;padding:4px 10px;border-radius:6px;font-size:12px;cursor:pointer;">Cancelar</button>';
                } else {
                    acciones = '<span style="color:#94a3b8;font-size:12px;">—</span>';
                }
                const salon = p.numeroSalon ? (p.nombreSede + ' - Salón ' + p.numeroSalon) : '—';
                const tr = document.createElement('tr');
                if (rol === 'DOCENTE' || rol === 'ADMINISTRATIVO') {
                    tr.innerHTML = [
                        '<td style="padding:11px 14px;color:#64748b;font-weight:600;">#' + p.idPrestamo + '</td>',
                        '<td style="padding:11px 14px;font-weight:500;">' + (p.nombreDispositivo || p.idDispositivo || '-') + '</td>',
                        '<td style="padding:11px 14px;color:#64748b;">' + (p.idSalon || '-') + '</td>',
                        '<td style="padding:11px 14px;font-size:13px;color:#64748b;">' + formatearFecha(p.fechaInicio) + '</td>',
                        '<td style="padding:11px 14px;font-size:13px;color:#64748b;">' + formatearFecha(p.fechaFin) + '</td>',
                        '<td style="padding:11px 14px;">' + getEstadoBadge(p.estado) + '</td>',
                        '<td style="padding:11px 14px;text-align:center;">' + acciones + '</td>'
                    ].join('');
                } else {
                    tr.innerHTML = [
                        '<td style="padding:11px 14px;color:#64748b;font-weight:600;">#' + p.idPrestamo + '</td>',
                        '<td style="padding:11px 14px;">' + p.nombreUsuario + '</td>',
                        '<td style="padding:11px 14px;font-weight:500;">' + p.nombreDispositivo + '</td>',
                        '<td style="padding:11px 14px;color:#64748b;">' + salon + '</td>',
                        '<td style="padding:11px 14px;font-size:13px;color:#64748b;">' + formatearFecha(p.fechaInicio) + '</td>',
                        '<td style="padding:11px 14px;font-size:13px;color:#64748b;">' + formatearFecha(p.fechaFin) + '</td>',
                        '<td style="padding:11px 14px;">' + getEstadoBadge(p.estado) + '</td>',
                        '<td style="padding:11px 14px;text-align:center;">' + acciones + '</td>'
                    ].join('');
                }
                tablaPrestamosPanelBody.appendChild(tr);
            });
        }
        const pageLabel = document.getElementById('prestamos-pagina');
        if (pageLabel) pageLabel.textContent = 'Página ' + prestamosPanelPage + ' de ' + totalPages;
        const btnPrev = document.getElementById('btn-prestamos-prev');
        const btnNext = document.getElementById('btn-prestamos-next');
        if (btnPrev) btnPrev.disabled = prestamosPanelPage <= 1;
        if (btnNext) btnNext.disabled = prestamosPanelPage >= totalPages;
    }

    function cargarPrestamos() {
        fetch(`${apiBase}/prestamos`)
            .then(res => res.json())
            .then(prestamos => {
                prestamosActuales = prestamos;
                prestamosDashboard = Array.isArray(prestamos) ? prestamos : [];
                renderTablaSolicitudesDashboard();
            })
            .catch(error => console.error('Error cargando prestamos:', error));
    }

    function cargarMisSolicitudes() {
        // El backend devuelve solo las solicitudes del usuario logueado
        cargarPrestamos();
    }

    // Panel dedicado de prestamos
    function cargarPrestamosPanelDedicado() {
        var filtroEstado = document.getElementById('prest-filtro-estado') ? document.getElementById('prest-filtro-estado').value : '';
        var tbody = document.getElementById('tabla-prestamos-panel-body');
        if (!tbody) return;

        fetch(apiBase + '/prestamos')
            .then(function(res) { return res.json(); })
            .then(function(prestamos) {
                prestamosActuales = Array.isArray(prestamos) ? prestamos : [];
                if (filtroEstado) {
                    prestamosActuales = prestamosActuales.filter(function(p) { return p.estado === filtroEstado; });
                }
                actualizarEncabezadoPrestamosPanel();
                renderTablaPrestamosPanel();
            })
            .catch(function(error) { console.error('Error panel prestamos:', error); });
    }

    window.aprobarPrestamoPanel = function(id) {
        fetch(apiBase + '/prestamos/' + id + '/estado', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ estado: 'APROBADO' })
        })
            .then(function(res) { return res.json().then(function(d) { return { status: res.status, body: d }; }); })
            .then(function(res) {
                showToast(res.status < 300 ? (res.body.mensaje || 'Pr\u00e9stamo aprobado') : (res.body.error || 'Error'), res.status < 300 ? 'success' : 'error');
                if (res.status < 300) { cargarPrestamosPanelDedicado(); cargarDispositivos(); cargarPrestamos(); }
            });
    };
    window.rechazarPrestamoPanel = function(id) {
        fetch(apiBase + '/prestamos/' + id + '/estado', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ estado: 'RECHAZADO' })
        })
            .then(function(res) { return res.json().then(function(d) { return { status: res.status, body: d }; }); })
            .then(function(res) {
                showToast(res.status < 300 ? (res.body.mensaje || 'Pr\u00e9stamo rechazado') : (res.body.error || 'Error'), res.status < 300 ? 'info' : 'error');
                if (res.status < 300) { cargarPrestamosPanelDedicado(); cargarDispositivos(); }
            });
    };
    window.cancelarPrestamoPanel = function(id) {
        if (!confirm('\u00bfCancelar esta solicitud de pr\u00e9stamo?')) return;
        fetch(apiBase + '/prestamos/' + id, { method: 'DELETE' })
            .then(function(res) { return res.json().then(function(d) { return { status: res.status, body: d }; }); })
            .then(function(res) {
                showToast(res.status < 300 ? 'Solicitud cancelada' : (res.body.error || 'Error'), res.status < 300 ? 'info' : 'error');
                if (res.status < 300) cargarPrestamosPanelDedicado();
            });
    };
    var btnNuevoPrestamoPanelBtn = document.getElementById('btn-nuevo-prestamo-panel');
    if (btnNuevoPrestamoPanelBtn) btnNuevoPrestamoPanelBtn.addEventListener('click', abrirModalCrearPrestamo);
    var pRestFiltro = document.getElementById('prest-filtro-estado');
    if (pRestFiltro) pRestFiltro.addEventListener('change', cargarPrestamosPanelDedicado);


    function abrirModalCrearPrestamo() {
        formCrearPrestamo.reset();
        
        // Cargar dispositivos DISPONIBLES
        fetch(`${apiBase}/dispositivos?estado=DISPONIBLE`)
            .then(res => res.json())
            .then(dispositivos => {
                const selectD = document.getElementById('prestamo-dispositivo');
                selectD.innerHTML = '<option value="">Seleccione un dispositivo...</option>';
                dispositivos.forEach(d => {
                    selectD.innerHTML += `<option value="${d.idDispositivo}">${d.nombre} (${d.tipo})</option>`;
                });
            });

        // Cargar salones
        fetch(`${apiBase}/salones`)
            .then(res => res.json())
            .then(salones => {
                const selectS = document.getElementById('prestamo-salon');
                selectS.innerHTML = '<option value="">Seleccione un salón...</option>';
                salones.forEach(s => {
                    selectS.innerHTML += `<option value="${s.idSalon}">Salón ${s.numero}</option>`;
                });
            });

        modalCrearPrestamo.style.display = 'flex';
    }

    formCrearPrestamo.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const payload = {
            idDispositivo: parseInt(document.getElementById('prestamo-dispositivo').value),
            idSalon: parseInt(document.getElementById('prestamo-salon').value),
            fechaInicio: document.getElementById('prestamo-inicio').value,
            fechaFin: document.getElementById('prestamo-fin').value
        };

        fetch(`${apiBase}/prestamos`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
        .then(res => res.json().then(data => ({status: res.status, body: data})))
        .then(res => {
            if (res.status >= 200 && res.status < 300) {
                alert(res.body.mensaje);
                modalCrearPrestamo.style.display = 'none';
                cargarPrestamos();
            } else {
                alert(res.body.error || 'Error al solicitar préstamo');
            }
        })
        .catch(err => alert('Error de conexión con el servidor'));
    });

    let idPrestamoDetalle = null;

    function abrirModalDetallePrestamo(id) {
        const prestamo = prestamosActuales.find(p => p.idPrestamo === id);
        if (!prestamo) return;

        idPrestamoDetalle = id;

        document.getElementById('det-prestamo-usuario').textContent = prestamo.nombreUsuario;
        document.getElementById('det-prestamo-dispositivo').textContent = prestamo.nombreDispositivo;
        document.getElementById('det-prestamo-salon').textContent = prestamo.numeroSalon + ' (' + prestamo.nombreSede + ')';
        document.getElementById('det-prestamo-inicio').textContent = formatearFecha(prestamo.fechaInicio);
        document.getElementById('det-prestamo-fin').textContent = formatearFecha(prestamo.fechaFin);
        document.getElementById('det-prestamo-estado').innerHTML = getEstadoBadge(prestamo.estado);

        const btnAprobar = document.getElementById('btn-aprobar-prestamo');
        const btnRechazar = document.getElementById('btn-rechazar-prestamo');

        // Solo Admin/Técnico pueden aprobar/rechazar solicitudes PENDIENTES
        if ((rolGlobal === 'ADMINISTRADOR' || rolGlobal === 'TECNICO') && prestamo.estado === 'PENDIENTE') {
            btnAprobar.style.display = 'inline-block';
            btnRechazar.style.display = 'inline-block';
        } else {
            btnAprobar.style.display = 'none';
            btnRechazar.style.display = 'none';
        }

        modalDetallePrestamo.style.display = 'flex';
    }

    document.getElementById('btn-aprobar-prestamo').addEventListener('click', () => {
        cambiarEstadoPrestamo(idPrestamoDetalle, 'APROBADO');
    });

    document.getElementById('btn-rechazar-prestamo').addEventListener('click', () => {
        cambiarEstadoPrestamo(idPrestamoDetalle, 'RECHAZADO');
    });

    function cambiarEstadoPrestamo(id, nuevoEstado) {
        fetch(`${apiBase}/prestamos/${id}/estado`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ estado: nuevoEstado })
        })
        .then(res => res.json().then(data => ({status: res.status, body: data})))
        .then(res => {
            if (res.status >= 200 && res.status < 300) {
                alert(res.body.mensaje);
                modalDetallePrestamo.style.display = 'none';
                cargarPrestamos();
                cargarDispositivos(); // Actualizar contadores del dashboard
            } else {
                alert(res.body.error || 'Error al cambiar estado');
            }
        })
        .catch(err => alert('Error de conexión con el servidor'));
    }
    // ---------------------------------------------
    // LOGICA CRUD MANTENIMIENTOS
    // ---------------------------------------------
    const tablaMantenimientosBody = document.getElementById('tabla-mantenimientos-body');
    const modalCrearMantenimiento = document.getElementById('modal-crear-mantenimiento');
    const formCrearMantenimiento = document.getElementById('form-crear-mantenimiento');
    const modalDetalleMantenimiento = document.getElementById('modal-detalle-mantenimiento');
    
    let mantenimientosActuales = [];

    const btnNuevoMantenimiento = document.getElementById('btn-nuevo-mantenimiento');
    if (btnNuevoMantenimiento) {
        btnNuevoMantenimiento.addEventListener('click', abrirModalCrearMantenimiento);
    }
    
    const btnCancelarMant = document.getElementById('btn-cancelar-mantenimiento');
    if(btnCancelarMant) btnCancelarMant.addEventListener('click', () => modalCrearMantenimiento.style.display = 'none');
    
    const btnCerrarMant = document.getElementById('btn-cerrar-detalle-mant');
    if(btnCerrarMant) btnCerrarMant.addEventListener('click', () => modalDetalleMantenimiento.style.display = 'none');

    function cargarMantenimientos() {
        if (!tablaMantenimientosBody) return;
        fetch(apiBase + '/mantenimientos')
            .then(res => res.json())
            .then(mantenimientos => {
                mantenimientosActuales = mantenimientos;
                if(Array.isArray(mantenimientos)){
                    actualizarEncabezadoMantenimientos();
                    tablaMantenimientosBody.innerHTML = '';
                    var rol = obtenerRolActual();
                    var esTecnico = rol === 'TECNICO';
                    mantenimientos.forEach(m => {
                        const tr = document.createElement('tr');
                        if (rol === 'TECNICO') {
                            tr.innerHTML = `
                                <td style="padding:12px 14px;color:#64748b;font-weight:600;">#${m.idMantenimiento}</td>
                                <td style="padding:12px 14px;font-weight:500;">${m.nombreDispositivo || m.idDispositivo || '-'}</td>
                                <td style="padding:12px 14px;"><span class="badge" style="background:#8e44ad; color:white; padding:4px 8px; border-radius:12px; font-size:12px;">${m.tipo}</span></td>
                                <td style="padding:12px 14px;font-size:13px;color:#64748b;">${formatearFecha(m.fechaInicio)}</td>
                                <td style="padding:12px 14px;font-size:13px;color:#64748b;">${m.fechaFin ? formatearFecha(m.fechaFin) : 'En curso'}</td>
                                <td style="padding:12px 14px;">${getEstadoBadgeMantenimiento(m.estado)}</td>
                                <td style="padding:12px 14px;color:#64748b;font-size:13px;">${m.descripcion || '-'}</td>
                                <td style="padding:12px 14px;text-align:center;"><button class="view-btn btn-ver-mantenimiento" data-id="${m.idMantenimiento}">Ver</button></td>
                            `;
                        } else if (rol === 'ADMINISTRADOR') {
                            tr.innerHTML = `
                                <td style="padding:12px 14px;color:#64748b;font-weight:600;">#${m.idMantenimiento}</td>
                                <td style="padding:12px 14px;font-weight:500;">${m.nombreDispositivo || m.idDispositivo || '-'}</td>
                                <td style="padding:12px 14px;">${m.nombreUsuario || '-'}</td>
                                <td style="padding:12px 14px;"><span class="badge" style="background:#8e44ad; color:white; padding:4px 8px; border-radius:12px; font-size:12px;">${m.tipo}</span></td>
                                <td style="padding:12px 14px;font-size:13px;color:#64748b;">${formatearFecha(m.fechaInicio)}</td>
                                <td style="padding:12px 14px;font-size:13px;color:#64748b;">${m.fechaFin ? formatearFecha(m.fechaFin) : 'En curso'}</td>
                                <td style="padding:12px 14px;">${getEstadoBadgeMantenimiento(m.estado)}</td>
                                <td style="padding:12px 14px;color:#64748b;font-size:13px;">${m.descripcion || '-'}</td>
                                <td style="padding:12px 14px;font-size:13px;color:#64748b;">${m.fechaCreacion ? formatearFecha(m.fechaCreacion) : '-'}</td>
                                <td style="padding:12px 14px;text-align:center;"><button class="view-btn btn-ver-mantenimiento" data-id="${m.idMantenimiento}">Ver</button></td>
                            `;
                        } else {
                            tr.innerHTML = `
                                <td style="padding:12px 14px;font-weight:500;">${m.nombreDispositivo}</td>
                                <td style="padding:12px 14px;">${m.nombreUsuario}</td>
                                <td style="padding:12px 14px;"><span class="badge" style="background:#8e44ad; color:white; padding:4px 8px; border-radius:12px; font-size:12px;">${m.tipo}</span></td>
                                <td style="padding:12px 14px;font-size:13px;color:#64748b;">${formatearFecha(m.fechaInicio)}</td>
                                <td style="padding:12px 14px;">${getEstadoBadgeMantenimiento(m.estado)}</td>
                                <td style="padding:12px 14px;text-align:center;"><button class="view-btn btn-ver-mantenimiento" data-id="${m.idMantenimiento}">Ver</button></td>
                            `;
                        }
                        tablaMantenimientosBody.appendChild(tr);
                    });

                    document.querySelectorAll('.btn-ver-mantenimiento').forEach(btn => {
                        btn.addEventListener('click', (e) => {
                            const id = parseInt(e.target.getAttribute('data-id'));
                            abrirModalDetalleMantenimiento(id);
                        });
                    });
                }
            })
            .catch(error => console.error('Error cargando mantenimientos:', error));
    }

    function getEstadoBadgeMantenimiento(estado) {
        if(estado === 'FINALIZADO') return '<span class="badge approved"><span class="dot"></span>Finalizado</span>';
        if(estado === 'EN_PROCESO') return '<span class="badge pending"><span class="dot"></span>En Proceso</span>';
        return estado;
    }

    function abrirModalCrearMantenimiento() {
        if(formCrearMantenimiento) formCrearMantenimiento.reset();
        
        fetch(apiBase + '/dispositivos?estado=DISPONIBLE')
            .then(res => res.json())
            .then(dispositivos => {
                const selectD = document.getElementById('mantenimiento-dispositivo');
                if(selectD) {
                    selectD.innerHTML = '<option value="">Seleccione un dispositivo...</option>';
                    dispositivos.forEach(d => {
                        selectD.innerHTML += `<option value="${d.idDispositivo}">${d.nombre} (${d.tipo})</option>`;
                    });
                }
            });

        if(modalCrearMantenimiento) modalCrearMantenimiento.style.display = 'flex';
    }

    if(formCrearMantenimiento) {
        formCrearMantenimiento.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const payload = {
                idDispositivo: parseInt(document.getElementById('mantenimiento-dispositivo').value),
                tipo: document.getElementById('mantenimiento-tipo').value,
                descripcion: document.getElementById('mantenimiento-descripcion').value
            };

            fetch(apiBase + '/mantenimientos', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            .then(res => res.json().then(data => ({status: res.status, body: data})))
            .then(res => {
                if (res.status >= 200 && res.status < 300) {
                    alert(res.body.mensaje);
                    modalCrearMantenimiento.style.display = 'none';
                    cargarMantenimientos();
                    cargarDispositivos();
                } else {
                    alert(res.body.error || 'Error al registrar mantenimiento');
                }
            })
            .catch(err => alert('Error de conexión con el servidor'));
        });
    }

    let idMantenimientoDetalle = null;

    function abrirModalDetalleMantenimiento(id) {
        const mant = mantenimientosActuales.find(m => m.idMantenimiento === id);
        if (!mant) return;

        idMantenimientoDetalle = id;

        document.getElementById('det-mantenimiento-dispositivo').textContent = mant.nombreDispositivo;
        document.getElementById('det-mantenimiento-usuario').textContent = mant.nombreUsuario;
        document.getElementById('det-mantenimiento-tipo').textContent = mant.tipo;
        document.getElementById('det-mantenimiento-inicio').textContent = formatearFecha(mant.fechaInicio);
        document.getElementById('det-mantenimiento-fin').textContent = mant.fechaFin ? formatearFecha(mant.fechaFin) : 'No finalizado';
        document.getElementById('det-mantenimiento-descripcion').textContent = mant.descripcion;
        document.getElementById('det-mantenimiento-estado').innerHTML = getEstadoBadgeMantenimiento(mant.estado);

        const btnFinalizar = document.getElementById('btn-finalizar-mantenimiento');

        if ((rolGlobal === 'ADMINISTRADOR' || rolGlobal === 'TECNICO') && mant.estado === 'EN_PROCESO') {
            btnFinalizar.style.display = 'inline-block';
        } else {
            btnFinalizar.style.display = 'none';
        }

        modalDetalleMantenimiento.style.display = 'flex';
    }

    const btnFinalizarMantenimiento = document.getElementById('btn-finalizar-mantenimiento');
    if (btnFinalizarMantenimiento) {
        btnFinalizarMantenimiento.addEventListener('click', () => {
            fetch(apiBase + '/mantenimientos/' + idMantenimientoDetalle, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ estado: 'FINALIZADO' })
            })
            .then(res => res.json().then(data => ({status: res.status, body: data})))
            .then(res => {
                if (res.status >= 200 && res.status < 300) {
                    alert(res.body.mensaje);
                    modalDetalleMantenimiento.style.display = 'none';
                    cargarMantenimientos();
                    cargarDispositivos(); 
                } else {
                    alert(res.body.error || 'Error al finalizar mantenimiento');
                }
            })
            .catch(err => alert('Error de conexión con el servidor'));
        });
    }

    // ---------------------------------------------
    // LOGICA AUDITORIA
    // ---------------------------------------------
    const tablaAuditoriaBody = document.getElementById('tabla-auditoria-body');
    
    function cargarAuditoria() {
        if (!tablaAuditoriaBody) return;
        fetch(apiBase + '/auditoria')
            .then(res => res.json())
            .then(logs => {
                if(Array.isArray(logs)){
                    tablaAuditoriaBody.innerHTML = '';
                    logs.forEach(log => {
                        let badgeColor = '#3498db'; // Default blue (LOGIN, etc)
                        if (log.accion === 'INSERT') badgeColor = '#2ecc71'; // Green
                        else if (log.accion === 'UPDATE') badgeColor = '#f39c12'; // Orange
                        else if (log.accion === 'DELETE') badgeColor = '#e74c3c'; // Red
                        
                        const tr = document.createElement('tr');
                        tr.innerHTML = `
                            <td>${formatearFecha(log.fechaEvento)}</td>
                            <td>${log.nombreUsuario}</td>
                            <td><span class="badge" style="background:${badgeColor}; color:white; padding:4px 8px; border-radius:12px; font-size:12px;">${log.accion}</span></td>
                            <td>${log.tablaAfectada}</td>
                            <td>${log.descripcion}</td>
                            <td>${log.ip}</td>
                        `;
                        tablaAuditoriaBody.appendChild(tr);
                    });
                }
            })
            .catch(error => console.error('Error cargando auditoria:', error));
    }

    // ---------------------------------------------
    // LOGICA REPORTES
    // ---------------------------------------------
    let chartDispositivos = null;
    let chartMantenimientos = null;

    function cargarReportes() {
        fetch(apiBase + '/reportes/resumen')
            .then(res => res.json())
            .then(data => {
                if(data.error) {
                    console.error('Error cargando reportes:', data.error);
                    return;
                }
                renderChartDispositivos(data.dispositivos);
                renderChartMantenimientos(data.mantenimientos);
            })
            .catch(error => console.error('Error cargando reportes:', error));
    }

    function renderChartDispositivos(dataDisp) {
        const ctx = document.getElementById('chart-dispositivos');
        if (!ctx) return;
        
        if (chartDispositivos) {
            chartDispositivos.destroy();
        }

        const labels = Object.keys(dataDisp);
        const values = Object.values(dataDisp);
        const bgColors = labels.map(l => l === 'DISPONIBLE' ? '#2ecc71' : (l === 'EN_USO' ? '#3498db' : '#e74c3c'));

        chartDispositivos = new Chart(ctx, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    data: values,
                    backgroundColor: bgColors
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { position: 'bottom' }
                }
            }
        });
    }

    function renderChartMantenimientos(dataMant) {
        const ctx = document.getElementById('chart-mantenimientos');
        if (!ctx) return;

        if (chartMantenimientos) {
            chartMantenimientos.destroy();
        }

        const labels = Object.keys(dataMant);
        const values = Object.values(dataMant);
        const bgColors = labels.map(l => l === 'PREVENTIVO' ? '#f39c12' : '#8e44ad');

        chartMantenimientos = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: labels,
                datasets: [{
                    data: values,
                    backgroundColor: bgColors
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { position: 'bottom' }
                }
            }
        });
    }

    const btnExportarCSV = document.getElementById('btn-exportar-csv');
    if (btnExportarCSV) {
        btnExportarCSV.addEventListener('click', exportarInventarioCSV);
    }

    function exportarInventarioCSV() {
        fetch(apiBase + '/dispositivos')
            .then(res => res.json())
            .then(dispositivos => {
                if (!Array.isArray(dispositivos) || dispositivos.length === 0) {
                    alert('No hay dispositivos para exportar');
                    return;
                }

                const headers = ['ID', 'Nombre', 'Tipo', 'Estado', 'Descripción', 'Fecha Creación'];
                const rows = dispositivos.map(d => [
                    d.idDispositivo,
                    `"${d.nombre}"`,
                    d.tipo,
                    d.estado,
                    `"${(d.descripcion || '').replace(/"/g, '""')}"`,
                    formatearFecha(d.fechaCreacion)
                ]);

                let csvContent = "data:text/csv;charset=utf-8," 
                    + headers.join(",") + "\n"
                    + rows.map(e => e.join(",")).join("\n");

                const encodedUri = encodeURI(csvContent);
                const link = document.createElement("a");
                link.setAttribute("href", encodedUri);
                link.setAttribute("download", "inventario_systechpro.csv");
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            })
            .catch(error => alert('Error al exportar inventario'));
    }

});
