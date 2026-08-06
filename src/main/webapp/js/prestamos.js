// prestamos.js - Solicitudes de préstamo: dashboard, panel dedicado y modales
let prestamosActuales = [];
let prestamosDashboard = [];
let dashSolicitudesPage = 1;
const DASH_SOLICITUDES_PER_PAGE = 5;
let idPrestamoDetalle = null;
let buscadorPrestamos = null; // se inicializa en DOMContentLoaded (busqueda.js)

// totalReal viene del backend (ya filtrado por usuario ahí); aprobadas/pendientes se
// calculan sobre el lote recibido (el más reciente, acotado por tamano en cargarPrestamos).
function actualizarTarjetasSolicitudes(prestamos, totalReal) {
    if (!Array.isArray(prestamos)) return;
    var aprobadas = prestamos.filter(function(p) { return p.estado === 'APROBADO'; }).length;
    var pendientes = prestamos.filter(function(p) { return p.estado === 'PENDIENTE'; }).length;

    var totalEl = document.getElementById('card-solicitudes-totales');
    var aprobadasEl = document.getElementById('card-solicitudes-aprobadas');
    var pendientesEl = document.getElementById('card-solicitudes-pendientes');
    if (totalEl) totalEl.textContent = totalReal;
    if (aprobadasEl) aprobadasEl.textContent = aprobadas;
    if (pendientesEl) pendientesEl.textContent = pendientes;
}

function actualizarDispositivosDisponibles() {
    fetch(apiBase + '/dispositivos')
        .then(function(res) { return res.json(); })
        .then(function(dispositivos) {
            if (!Array.isArray(dispositivos)) return;
            var disponibles = dispositivos.filter(function(d) { return d.estado === 'DISPONIBLE'; }).length;
            var disponiblesEl = document.getElementById('card-dispositivos-disponibles');
            if (disponiblesEl) disponiblesEl.textContent = disponibles;
        })
        .catch(function(error) {
            console.error('Error actualizando dispositivos disponibles:', error);
        });
}

function renderTablaSolicitudesDashboard() {
    const tablaSolicitudesBody = document.getElementById('tabla-solicitudes-body');
    if (!tablaSolicitudesBody) return;
    const lista = Array.isArray(prestamosDashboard) ? prestamosDashboard.slice() : [];
    lista.sort((a, b) => (parseFecha(b.fechaInicio || b.fechaCreacion) || 0) - (parseFecha(a.fechaInicio || a.fechaCreacion) || 0));
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
                <td>${escapeHtml(p.nombreUsuario)}</td>
                <td>${escapeHtml(p.nombreDispositivo)}</td>
                <td>${formatearFecha(p.fechaInicio)}</td>
                <td>${getEstadoBadgePrestamo(p)}</td>
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

function renderTablaPrestamosPanel(lista) {
    const tablaPrestamosPanelBody = document.getElementById('tabla-prestamos-panel-body');
    if (!tablaPrestamosPanelBody) return;
    const rol = obtenerRolActual();
    // La lista ya viene filtrada, ordenada y paginada por el backend (búsqueda, estado,
    // rango de fechas y el alcance por usuario para Docente/Administrativo).
    tablaPrestamosPanelBody.innerHTML = '';
    if (!lista.length) {
        const colspan = rol === 'DOCENTE' || rol === 'ADMINISTRATIVO' ? 7 : 8;
        tablaPrestamosPanelBody.innerHTML = '<tr><td colspan="' + colspan + '" style="text-align:center;padding:30px;color:#94a3b8;">No hay solicitudes de préstamo</td></tr>';
    } else {
        lista.forEach(function(p) {
            const esAdminTec = (rolGlobal === 'ADMINISTRADOR' || rolGlobal === 'TECNICO' || rolGlobal === 'MONITOR');
            const esPend = p.estado === 'PENDIENTE';
            const esAprobado = p.estado === 'APROBADO';
            let acciones = '';
            if (esAdminTec && esPend) {
                acciones += '<button onclick="aprobarPrestamoPanel(' + p.idPrestamo + ')" style="background:#22c55e;color:white;border:none;padding:4px 10px;border-radius:6px;font-size:12px;cursor:pointer;margin-right:4px;">✓ Aprobar</button>';
                acciones += '<button onclick="rechazarPrestamoPanel(' + p.idPrestamo + ')" style="background:#ef4444;color:white;border:none;padding:4px 10px;border-radius:6px;font-size:12px;cursor:pointer;">✕ Rechazar</button>';
            } else if (!esAdminTec && esPend) {
                acciones += '<button onclick="cancelarPrestamoPanel(' + p.idPrestamo + ')" style="background:#64748b;color:white;border:none;padding:4px 10px;border-radius:6px;font-size:12px;cursor:pointer;">Cancelar</button>';
            } else if (esAprobado && Number(p.idUsuario) === Number(idUsuarioGlobal)) {
                // La devolución/cancelación de reserva es autoservicio de quien lo pidió, sin
                // importar su rol - un admin o monitor que se autopresta un dispositivo también
                // necesita poder devolverlo (el backend ya lo permite vía esAutoDevolucion).
                const inicioPrestamo = parseFecha(p.fechaInicio);
                const esReservado = inicioPrestamo && inicioPrestamo.getTime() > Date.now();
                const labelDevolver = esReservado ? 'Cancelar reserva' : 'Devolver';
                acciones += '<button onclick="devolverPrestamoPanel(' + p.idPrestamo + ', ' + esReservado + ')" style="background:#2563eb;color:white;border:none;padding:4px 10px;border-radius:6px;font-size:12px;cursor:pointer;">' + labelDevolver + '</button>';
            } else {
                acciones = '<span style="color:#94a3b8;font-size:12px;">—</span>';
            }
            const ubicacionSalon = generarUbicacionSalon(p);
            const tr = document.createElement('tr');
            if (rol === 'DOCENTE' || rol === 'ADMINISTRATIVO') {
                tr.innerHTML = [
                    '<td style="padding:11px 14px;color:#64748b;font-weight:600;">#' + p.idPrestamo + '</td>',
                    '<td style="padding:11px 14px;font-weight:500;">' + escapeHtml(p.nombreDispositivo || p.idDispositivo || '-') + '</td>',
                    '<td style="padding:11px 14px;color:#64748b;">' + escapeHtml(ubicacionSalon) + '</td>',
                    '<td style="padding:11px 14px;font-size:13px;color:#64748b;">' + formatearFecha(p.fechaInicio) + '</td>',
                    '<td style="padding:11px 14px;font-size:13px;color:#64748b;">' + formatearFecha(p.fechaFin) + '</td>',
                    '<td style="padding:11px 14px;">' + getEstadoBadgePrestamo(p) + '</td>',
                    '<td style="padding:11px 14px;text-align:center;">' + acciones + '</td>'
                ].join('');
            } else {
                tr.innerHTML = [
                    '<td style="padding:11px 14px;color:#64748b;font-weight:600;">#' + p.idPrestamo + '</td>',
                    '<td style="padding:11px 14px;">' + escapeHtml(p.nombreUsuario) + '</td>',
                    '<td style="padding:11px 14px;font-weight:500;">' + escapeHtml(p.nombreDispositivo) + '</td>',
                    '<td style="padding:11px 14px;color:#64748b;">' + escapeHtml(ubicacionSalon) + '</td>',
                    '<td style="padding:11px 14px;font-size:13px;color:#64748b;">' + formatearFecha(p.fechaInicio) + '</td>',
                    '<td style="padding:11px 14px;font-size:13px;color:#64748b;">' + formatearFecha(p.fechaFin) + '</td>',
                    '<td style="padding:11px 14px;">' + getEstadoBadgePrestamo(p) + '</td>',
                    '<td style="padding:11px 14px;text-align:center;">' + acciones + '</td>'
                ].join('');
            }
            tablaPrestamosPanelBody.appendChild(tr);
        });
    }
}

// Resumen para el widget de "Inicio" (no es uno de los 5 módulos con búsqueda dedicada):
// se pide el máximo permitido, ordenado por más reciente primero, suficiente para un
// resumen de actividad reciente sin traer el historial completo.
function cargarPrestamos() {
    fetch(`${apiBase}/prestamos?tamano=50`)
        .then(res => res.json())
        .then(prestamos => {
            var datos = (prestamos && Array.isArray(prestamos.datos)) ? prestamos.datos : [];
            prestamosActuales = datos;
            prestamosDashboard = datos;
            renderTablaSolicitudesDashboard();
            if (rolGlobal === 'DOCENTE' || rolGlobal === 'ADMINISTRATIVO') {
                actualizarTarjetasSolicitudes(datos, prestamos.total || 0);
            }
        })
        .catch(error => console.error('Error cargando prestamos:', error));
}

function cargarMisSolicitudes() {
    cargarPrestamos();
    if (rolGlobal === 'DOCENTE' || rolGlobal === 'ADMINISTRATIVO') {
        actualizarDispositivosDisponibles();
    }
}

const PREST_PANEL_IDS = { info: 'prestamos-pagina', prev: 'btn-prestamos-prev', next: 'btn-prestamos-next' };
const PREST_TAMANO_PAGINA = 10;

function cargarPrestamosPanelDedicado(estadoBuscador) {
    var tbody = document.getElementById('tabla-prestamos-panel-body');
    if (!tbody) return;
    var e = estadoBuscador || (buscadorPrestamos ? buscadorPrestamos.estado : { pagina: 1, q: '', filtros: {} });

    var params = new URLSearchParams();
    if (e.q) params.set('q', e.q);
    if (e.filtros.estado) params.set('estado', e.filtros.estado);
    if (e.filtros.fechaDesde) params.set('fechaDesde', e.filtros.fechaDesde);
    if (e.filtros.fechaHasta) params.set('fechaHasta', e.filtros.fechaHasta);
    params.set('pagina', e.pagina);
    params.set('tamano', PREST_TAMANO_PAGINA);

    fetch(apiBase + '/prestamos?' + params.toString())
        .then(function(res) { return res.json(); })
        .then(function(resp) {
            prestamosActuales = Array.isArray(resp.datos) ? resp.datos : [];
            actualizarEncabezadoPrestamosPanel();
            renderTablaPrestamosPanel(prestamosActuales);
            renderInfoPaginacion(PREST_PANEL_IDS, resp.pagina || 1, resp.tamanoPagina || PREST_TAMANO_PAGINA, resp.total || 0);
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
            showToast(res.status < 300 ? (res.body.mensaje || 'Préstamo aprobado') : (res.body.error || 'Error'), res.status < 300 ? 'success' : 'error');
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
            showToast(res.status < 300 ? (res.body.mensaje || 'Préstamo rechazado') : (res.body.error || 'Error'), res.status < 300 ? 'info' : 'error');
            if (res.status < 300) { cargarPrestamosPanelDedicado(); cargarDispositivos(); }
        });
};
window.cancelarPrestamoPanel = async function(id) {
    const confirmado = await confirmarAccion('¿Cancelar esta solicitud de préstamo?');
    if (!confirmado) return;
    fetch(apiBase + '/prestamos/' + id, { method: 'DELETE' })
        .then(function(res) { return res.json().then(function(d) { return { status: res.status, body: d }; }); })
        .then(function(res) {
            showToast(res.status < 300 ? 'Solicitud cancelada' : (res.body.error || 'Error'), res.status < 300 ? 'info' : 'error');
            if (res.status < 300) cargarPrestamosPanelDedicado();
        });
};
window.devolverPrestamoPanel = async function(id, esReservado) {
    const mensajeConfirm = esReservado ? '¿Cancelar esta reserva?' : '¿Confirmas que ya devolviste este dispositivo?';
    const confirmado = await confirmarAccion(mensajeConfirm);
    if (!confirmado) return;
    fetch(apiBase + '/prestamos/' + id + '/estado', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ estado: 'DEVUELTO' })
    })
        .then(function(res) { return res.json().then(function(d) { return { status: res.status, body: d }; }); })
        .then(function(res) {
            showToast(res.status < 300 ? (res.body.mensaje || 'Dispositivo devuelto') : (res.body.error || 'Error'), res.status < 300 ? 'success' : 'error');
            if (res.status < 300) { cargarPrestamosPanelDedicado(); cargarDispositivos(); cargarPrestamos(); }
        });
};

function abrirModalCrearPrestamo() {
    const formCrearPrestamo = document.getElementById('form-crear-prestamo');
    const modalCrearPrestamo = document.getElementById('modal-crear-prestamo');
    formCrearPrestamo.reset();

    const prestamoSede = document.getElementById('prestamo-sede');
    const prestamoSalon = document.getElementById('prestamo-salon');
    const prestanoUbicacion = document.getElementById('prestamo-ubicacion-generada');

    prestamoSalon.innerHTML = '<option value="">Seleccione una sede primero</option>';
    prestanoUbicacion.value = '';

    fetch(`${apiBase}/dispositivos?estado=DISPONIBLE`)
        .then(res => res.json())
        .then(dispositivos => {
            const selectD = document.getElementById('prestamo-dispositivo');
            selectD.innerHTML = '<option value="">Seleccione un dispositivo...</option>';
            dispositivos.forEach(d => {
                selectD.innerHTML += `<option value="${d.idDispositivo}">${escapeHtml(d.nombre)} (${escapeHtml(d.tipo)})</option>`;
            });
        });

    prestamoSede.innerHTML = '<option value="">Cargando sedes...</option>';
    fetch(`${apiBase}/sedes`)
        .then(res => res.json())
        .then(sedes => {
            prestamoSede.innerHTML = '<option value="">Seleccione una sede...</option>';
            sedes.forEach(s => {
                prestamoSede.innerHTML += `<option value="${s.idSede}" data-codigo="${escapeHtml(s.codigo)}">${escapeHtml(s.nombre)} (${escapeHtml(s.codigo)})</option>`;
            });
        });

    modalCrearPrestamo.style.display = 'flex';
}

function guardarPrestamo(e) {
    e.preventDefault();
    const modalCrearPrestamo = document.getElementById('modal-crear-prestamo');

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
            showToast(res.body.mensaje, 'success');
            modalCrearPrestamo.style.display = 'none';
            cargarPrestamos();
        } else {
            showToast(res.body.error || 'Error al solicitar préstamo', 'error');
        }
    })
    .catch(err => showToast('Error de conexión con el servidor', 'error'));
}

function abrirModalDetallePrestamo(id) {
    const modalDetallePrestamo = document.getElementById('modal-detalle-prestamo');
    const prestamo = prestamosActuales.find(p => p.idPrestamo === id);
    if (!prestamo) return;

    idPrestamoDetalle = id;

    document.getElementById('det-prestamo-usuario').textContent = prestamo.nombreUsuario;
    document.getElementById('det-prestamo-dispositivo').textContent = prestamo.nombreDispositivo;
    document.getElementById('det-prestamo-salon').textContent = generarUbicacionSalon(prestamo);
    document.getElementById('det-prestamo-inicio').textContent = formatearFecha(prestamo.fechaInicio);
    document.getElementById('det-prestamo-fin').textContent = formatearFecha(prestamo.fechaFin);
    document.getElementById('det-prestamo-estado').innerHTML = getEstadoBadgePrestamo(prestamo);

    const btnAprobar = document.getElementById('btn-aprobar-prestamo');
    const btnRechazar = document.getElementById('btn-rechazar-prestamo');

    if ((rolGlobal === 'ADMINISTRADOR' || rolGlobal === 'TECNICO' || rolGlobal === 'MONITOR') && prestamo.estado === 'PENDIENTE') {
        btnAprobar.style.display = 'inline-block';
        btnRechazar.style.display = 'inline-block';
    } else {
        btnAprobar.style.display = 'none';
        btnRechazar.style.display = 'none';
    }

    modalDetallePrestamo.style.display = 'flex';
}

function cambiarEstadoPrestamo(id, nuevoEstado) {
    const modalDetallePrestamo = document.getElementById('modal-detalle-prestamo');
    fetch(`${apiBase}/prestamos/${id}/estado`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ estado: nuevoEstado })
    })
    .then(res => res.json().then(data => ({status: res.status, body: data})))
    .then(res => {
        if (res.status >= 200 && res.status < 300) {
            showToast(res.body.mensaje, 'success');
            modalDetallePrestamo.style.display = 'none';
            cargarPrestamos();
            cargarDispositivos(); // Actualizar contadores del dashboard
        } else {
            showToast(res.body.error || 'Error al cambiar estado', 'error');
        }
    })
    .catch(err => showToast('Error de conexión con el servidor', 'error'));
}

document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('btn-solicitar-prestamo-dash').addEventListener('click', abrirModalCrearPrestamo);
    document.getElementById('btn-cancelar-prestamo').addEventListener('click', () => document.getElementById('modal-crear-prestamo').style.display = 'none');
    document.getElementById('btn-cerrar-detalle').addEventListener('click', () => document.getElementById('modal-detalle-prestamo').style.display = 'none');

    document.getElementById('form-crear-prestamo').addEventListener('submit', guardarPrestamo);

    document.getElementById('prestamo-sede').addEventListener('change', function() {
        const salonSelect = document.getElementById('prestamo-salon');
        const ubicacionInput = document.getElementById('prestamo-ubicacion-generada');
        const sedeId = this.value;

        ubicacionInput.value = '';

        if (!sedeId) {
            salonSelect.innerHTML = '<option value="">Seleccione una sede primero</option>';
            return;
        }

        fetch(`${apiBase}/sedes/${sedeId}/salones`)
            .then(res => res.json())
            .then(salones => {
                salonSelect.innerHTML = '<option value="">Seleccione un salón...</option>';
                salones.forEach(s => {
                    salonSelect.innerHTML += `<option value="${s.idSalon}" data-numero="${s.numero}">${s.numero}</option>`;
                });
            })
            .catch(err => console.error('Error cargando salones:', err));
    });

    document.getElementById('prestamo-salon').addEventListener('change', function() {
        const ubicacionInput = document.getElementById('prestamo-ubicacion-generada');
        const sedeSelect = document.getElementById('prestamo-sede');
        const salonOpt = this.options[this.selectedIndex];

        if (!salonOpt.value) {
            ubicacionInput.value = '';
            return;
        }

        const sedeOpt = sedeSelect.options[sedeSelect.selectedIndex];
        const codigoSede = sedeOpt ? sedeOpt.getAttribute('data-codigo') : '';
        const numSalon = salonOpt ? salonOpt.getAttribute('data-numero') : '';

        ubicacionInput.value = (codigoSede && numSalon) ? (codigoSede + '-' + numSalon) : '';
    });

    document.getElementById('btn-aprobar-prestamo').addEventListener('click', () => {
        cambiarEstadoPrestamo(idPrestamoDetalle, 'APROBADO');
    });

    document.getElementById('btn-rechazar-prestamo').addEventListener('click', () => {
        cambiarEstadoPrestamo(idPrestamoDetalle, 'RECHAZADO');
    });

    const btnNuevoPrestamoPanelBtn = document.getElementById('btn-nuevo-prestamo-panel');
    if (btnNuevoPrestamoPanelBtn) btnNuevoPrestamoPanelBtn.addEventListener('click', abrirModalCrearPrestamo);

    buscadorPrestamos = crearBuscadorPaginado({ onCargar: cargarPrestamosPanelDedicado });

    const pRestBuscar = document.getElementById('prest-buscar');
    if (pRestBuscar) pRestBuscar.addEventListener('input', (e) => buscadorPrestamos.onBuscar(e.target.value));

    const pRestFiltro = document.getElementById('prest-filtro-estado');
    if (pRestFiltro) pRestFiltro.addEventListener('change', (e) => buscadorPrestamos.onFiltro('estado', e.target.value));

    const pRestDesde = document.getElementById('prest-fecha-desde');
    if (pRestDesde) pRestDesde.addEventListener('change', (e) => buscadorPrestamos.onFiltro('fechaDesde', e.target.value));

    const pRestHasta = document.getElementById('prest-fecha-hasta');
    if (pRestHasta) pRestHasta.addEventListener('change', (e) => buscadorPrestamos.onFiltro('fechaHasta', e.target.value));

    const btnDashSolicitudesPrev = document.getElementById('btn-dash-solicitudes-prev');
    const btnDashSolicitudesNext = document.getElementById('btn-dash-solicitudes-next');
    if (btnDashSolicitudesPrev) {
        btnDashSolicitudesPrev.addEventListener('click', function() {
            if (dashSolicitudesPage > 1) { dashSolicitudesPage--; renderTablaSolicitudesDashboard(); }
        });
    }
    if (btnDashSolicitudesNext) {
        btnDashSolicitudesNext.addEventListener('click', function() {
            const total = (Array.isArray(prestamosDashboard) ? prestamosDashboard.length : 0);
            const totalPages = Math.max(1, Math.ceil(total / DASH_SOLICITUDES_PER_PAGE));
            if (dashSolicitudesPage < totalPages) { dashSolicitudesPage++; renderTablaSolicitudesDashboard(); }
        });
    }

    const btnPrestamosPrev = document.getElementById('btn-prestamos-prev');
    const btnPrestamosNext = document.getElementById('btn-prestamos-next');
    if (btnPrestamosPrev) {
        btnPrestamosPrev.addEventListener('click', () => buscadorPrestamos.irAPagina(buscadorPrestamos.estado.pagina - 1));
    }
    if (btnPrestamosNext) {
        btnPrestamosNext.addEventListener('click', () => buscadorPrestamos.irAPagina(buscadorPrestamos.estado.pagina + 1));
    }

    const navPrestamosBtn = document.getElementById('nav-prestamos-btn');
    if (navPrestamosBtn) {
        navPrestamosBtn.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-prestamos');
            buscadorPrestamos.cargarInicial();
        });
    }

    const navMisSolicitudesBtn = document.getElementById('nav-mis-solicitudes-btn');
    if (navMisSolicitudesBtn) {
        navMisSolicitudesBtn.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-prestamos', 'nav-mis-solicitudes-btn');
            buscadorPrestamos.cargarInicial();
        });
    }

    const btnCrearPrestamoDocente = document.getElementById('btn-crear-prestamo-docente');
    if (btnCrearPrestamoDocente) {
        btnCrearPrestamoDocente.addEventListener('click', abrirModalCrearPrestamo);
    }

    const btnVerSolicitudesDocente = document.getElementById('btn-ver-solicitudes-docente');
    if (btnVerSolicitudesDocente) {
        btnVerSolicitudesDocente.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-prestamos', 'nav-mis-solicitudes-btn');
            buscadorPrestamos.cargarInicial();
        });
    }

    const verSolicitudesBtn = document.getElementById('ver-solicitudes-btn');
    if (verSolicitudesBtn) {
        verSolicitudesBtn.addEventListener('click', () => {
            mostrarPanel('panel-prestamos');
            if (navPrestamosBtn) navPrestamosBtn.classList.add('active');
            buscadorPrestamos.cargarInicial();
        });
    }

    const btnMonitorVerPendientes = document.getElementById('btn-monitor-ver-pendientes');
    if (btnMonitorVerPendientes) {
        btnMonitorVerPendientes.addEventListener('click', () => {
            mostrarPanel('panel-prestamos');
            if (navPrestamosBtn) navPrestamosBtn.classList.add('active');
            const filtroEstado = document.getElementById('prest-filtro-estado');
            if (filtroEstado) filtroEstado.value = 'PENDIENTE';
            buscadorPrestamos.onFiltro('estado', 'PENDIENTE');
        });
    }
});
