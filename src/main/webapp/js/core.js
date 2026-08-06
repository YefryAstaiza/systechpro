// core.js - Estado compartido, utilidades y layout (permisos de sidebar, paneles, encabezados)
const apiBase = window.location.origin + '/systechpro/api';
let rolGlobal = '';
let idUsuarioGlobal = '';
let autoRefreshTimer = null;
const PANEL_PAGE_SIZE = 10;

// Escapa datos antes de insertarlos como HTML (previene XSS almacenado en campos de texto libre)
function escapeHtml(value) {
    if (value === null || value === undefined) return '';
    return String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

// ---------------------------------------------
// PERMISOS Y NAVEGACIÓN POR ROL
// ---------------------------------------------
const permisos = {
    ADMINISTRADOR: ['inicio','usuarios','dispositivos','prestamos','mantenimientos','auditoria','reportes', 'password-requests', 'corte-diario'],
    TECNICO: ['inicio','mantenimientos','corte-diario'],
    DOCENTE: ['inicio','mis-solicitudes'],
    ADMINISTRATIVO: ['inicio','mis-solicitudes'],
    MONITOR: ['inicio','dispositivos','prestamos','corte-diario']
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
    'mis-solicitudes': 'nav-mis-solicitudes-btn',
    'password-requests': 'nav-password-requests-btn',
    'corte-diario': 'nav-corte-diario-btn'
};

const panelInicioByRol = {
    ADMINISTRADOR: 'panel-dashboard',
    TECNICO: 'panel-inicio-tecnico',
    DOCENTE: 'panel-inicio-docente',
    ADMINISTRATIVO: 'panel-inicio-docente',
    MONITOR: 'panel-inicio-monitor'
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
        'panel-inicio-monitor': 'nav-inicio-btn',
        'panel-usuarios': 'nav-usuarios-btn',
        'panel-dispositivos': 'nav-dispositivos-btn',
        'panel-prestamos': 'nav-prestamos-btn',
        'panel-mantenimientos': 'nav-mantenimientos-btn',
        'panel-auditoria': 'nav-auditoria-btn',
        'panel-reportes': 'nav-reportes-btn',
        'panel-password-requests': 'nav-password-requests-btn',
        'panel-corte-diario': 'nav-corte-diario-btn'
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
    } else if (rol === 'MONITOR') {
        mostrarPanel('panel-inicio-monitor');
    } else {
        mostrarPanel('panel-inicio-docente');
    }
}

function actualizarTituloInicio(rol) {
    const titulo = document.getElementById('titulo-inicio');
    if (!titulo) return;

    if (rol === 'TECNICO') {
        titulo.textContent = 'Inicio Técnico';
    } else if (rol === 'MONITOR') {
        titulo.textContent = 'Inicio Monitor';
    } else {
        titulo.textContent = 'Inicio';
    }
}

function renderLayoutByRole() {
    if (!rolGlobal) return;
    aplicarPermisosPorRol(rolGlobal);
    const passwordRequestsBtn = document.getElementById('nav-password-requests-btn');
    if (passwordRequestsBtn) {
        passwordRequestsBtn.style.display = rolGlobal === 'ADMINISTRADOR' ? 'flex' : 'none';
    }
    // Dispositivos es de solo lectura para cualquier rol distinto de ADMINISTRADOR (p.ej. MONITOR).
    const btnNuevoDispositivo = document.getElementById('btn-nuevo-dispositivo');
    if (btnNuevoDispositivo) {
        btnNuevoDispositivo.style.display = rolGlobal === 'ADMINISTRADOR' ? 'flex' : 'none';
    }
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
        return (usuario.rol || '').trim().toUpperCase();
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

// ---------------------------------------------
// TOAST NOTIFICATIONS
// ---------------------------------------------
function showToast(message, type) {
    type = type || 'success';
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    const colors = { success: '#22c55e', error: '#ef4444', info: '#3b82f6', warning: '#f59e0b' };
    const icons = { success: '✓', error: '✕', info: 'ℹ', warning: '⚠' };
    const c = colors[type] || colors.info;
    const ic = icons[type] || icons.info;
    toast.style.cssText = 'background:white;border-left:4px solid ' + c + ';border-radius:8px;padding:12px 18px;box-shadow:0 4px 20px rgba(0,0,0,0.15);display:flex;align-items:center;gap:10px;font-size:14px;font-weight:500;color:#1e293b;pointer-events:all;min-width:260px;max-width:380px;animation:slideIn .3s ease;';
    toast.innerHTML = '<span style="color:' + c + ';font-size:18px;font-weight:700;">' + ic + '</span><span>' + escapeHtml(message) + '</span>';
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
// MODAL DE CONFIRMACIÓN (reemplaza confirm() nativo)
// ---------------------------------------------
function confirmarAccion(mensaje) {
    return new Promise((resolve) => {
        const modal = document.getElementById('modal-confirmacion');
        const msgEl = document.getElementById('confirmacion-mensaje');
        const btnAceptar = document.getElementById('btn-confirmacion-aceptar');
        const btnCancelar = document.getElementById('btn-confirmacion-cancelar');

        msgEl.textContent = mensaje;
        modal.style.display = 'flex';

        function limpiar() {
            modal.style.display = 'none';
            btnAceptar.removeEventListener('click', onAceptar);
            btnCancelar.removeEventListener('click', onCancelar);
        }
        function onAceptar() { limpiar(); resolve(true); }
        function onCancelar() { limpiar(); resolve(false); }

        btnAceptar.addEventListener('click', onAceptar);
        btnCancelar.addEventListener('click', onCancelar);
    });
}

// ---------------------------------------------
// VALIDACIONES DE TEXTO LIBRE
// ---------------------------------------------
function esTextoLibreValido(texto) {
    if (!texto) return false;
    const valor = texto.trim();
    if (valor.length < 5 || valor.length > 80) return false;
    if (!/^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ0-9 .,_\-\(\)\/]+$/.test(valor)) return false;
    if (!/[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]/.test(valor)) return false;
    if (/^\d+$/.test(valor)) return false;
    if (/[0-9]/.test(valor) && !/[ \-_/]/.test(valor)) return false;
    if (valor.replace(/[^A-Za-zÁÉÍÓÚÜÑáéíóúüñ]/g, '').length < 2) return false;
    return true;
}

function esNombreDispositivoValido(nombre) {
    return esTextoLibreValido(nombre);
}

function esDescripcionValida(descripcion) {
    if (!descripcion) return true;
    if (descripcion.length > 250) return false;
    return /^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ0-9 .,_;:\-\(\)\[\]\/\n\r]*$/.test(descripcion);
}

// ---------------------------------------------
// FECHAS
// ---------------------------------------------
function parseFecha(timestamp) {
    if (timestamp == null) return null;
    if (timestamp instanceof Date) return timestamp;

    if (typeof timestamp === 'number' || /^\d+$/.test(String(timestamp).trim())) {
        return new Date(Number(timestamp));
    }

    if (typeof timestamp === 'string') {
        const trimmed = timestamp.trim();
        if (!trimmed) return null;

        // Normalizar formato: '2024-05-01 08:00:00' -> '2024-05-01T08:00:00'
        let normalized = trimmed.replace(' ', 'T');

        // Si ya tiene información de zona horaria (Z o offset como +00:00 o -05:00)
        if (normalized.includes('Z') || /[+-]\d{2}:?\d{2}$/.test(normalized)) {
            return new Date(normalized);
        }

        // Si es un formato ISO básico sin zona, forzamos Bogotá (-05:00)
        if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2}(\.\d+)?)?$/.test(normalized)) {
            normalized += '-05:00';
        }

        const date = new Date(normalized);
        if (!Number.isNaN(date.getTime())) {
            return date;
        }
    }

    return null;
}

function formatearFecha(timestamp) {
    const d = parseFecha(timestamp);
    if (!d || Number.isNaN(d.getTime())) {
        return 'N/A';
    }
    const formatter = new Intl.DateTimeFormat('es-CO', {
        timeZone: 'America/Bogota',
        year: '2-digit',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
    });
    return formatter.format(d).replace(',', '');
}

// ---------------------------------------------
// BADGES DE ESTADO
// ---------------------------------------------
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
    if(estado === 'DISPONIBLE' || estado === 'APROBADO' || estado === 'APROBADA') return '<span class="badge approved"><span class="dot"></span>' + estado + '</span>';
    if(estado === 'EN_USO' || estado === 'PENDIENTE') return '<span class="badge pending"><span class="dot"></span>' + estado + '</span>';
    if(estado === 'MANTENIMIENTO' || estado === 'RECHAZADO' || estado === 'RECHAZADA') return '<span class="badge rejected"><span class="dot"></span>' + estado + '</span>';
    if(estado === 'DEVUELTO') return '<span class="badge returned"><span class="dot"></span>' + estado + '</span>';
    return estado;
}

// Con reservas anticipadas, "APROBADO" ya no significa "en curso ahora" - puede ser una
// reserva para más adelante, estar en curso, o haber pasado su fecha_fin sin que nadie
// lo marque devuelto (vencido). Se distinguen los tres comparando fechaInicio/fechaFin
// con el momento actual.
function getEstadoBadgePrestamo(prestamo) {
    if (prestamo.estado === 'APROBADO') {
        var inicio = parseFecha(prestamo.fechaInicio);
        var fin = parseFecha(prestamo.fechaFin);
        var ahora = Date.now();
        if (inicio && inicio.getTime() > ahora) {
            return '<span class="badge reserved"><span class="dot"></span>RESERVADO</span>';
        }
        if (fin && fin.getTime() < ahora) {
            return '<span class="badge overdue"><span class="dot"></span>VENCIDO</span>';
        }
        return '<span class="badge approved"><span class="dot"></span>EN CURSO</span>';
    }
    return getEstadoBadge(prestamo.estado);
}

function getTipoBadgeMantenimiento(tipo) {
    var cfg = {
        'PREVENTIVO': { bg: '#dbeafe', color: '#2563eb' },
        'CORRECTIVO': { bg: '#ffedd5', color: '#ea580c' }
    };
    var s = cfg[tipo] || { bg: '#f3e8ff', color: '#8e44ad' };
    return '<span class="badge" style="background:' + s.bg + '; color:' + s.color + '; padding:4px 8px; border-radius:12px; font-size:12px;">' + escapeHtml(tipo) + '</span>';
}

function getEstadoBadgeMantenimiento(estado) {
    if (estado === 'FINALIZADO') return '<span class="badge approved"><span class="dot"></span>Finalizado</span>';
    if (estado === 'EN_PROCESO') return '<span class="badge pending"><span class="dot"></span>En Proceso</span>';
    return '<span class="badge" style="background:#e2e8f0;color:#475569;padding:4px 10px;border-radius:12px;font-size:12px;display:inline-flex;align-items:center;gap:5px;">' + estado + '</span>';
}

function generarUbicacionSalon(prestamo) {
    if (prestamo.codigoSede && prestamo.numeroSalon) {
        return prestamo.codigoSede + '-' + prestamo.numeroSalon;
    }
    if (prestamo.nombreSede && prestamo.numeroSalon) {
        const codigo = prestamo.nombreSede
            .split(' ')
            .filter(Boolean)
            .map(word => word[0])
            .join('')
            .toUpperCase()
            .slice(0, 2);
        return codigo + '-' + prestamo.numeroSalon;
    }
    return prestamo.numeroSalon ? prestamo.numeroSalon : '—';
}
