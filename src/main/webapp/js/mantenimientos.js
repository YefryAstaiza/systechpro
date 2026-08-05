// mantenimientos.js - Registro de mantenimientos, dashboard e historial de técnico
let mantenimientosActuales = [];
let tecnicoRecientesPage = 1;
const TECNICO_RECIENTES_PER_PAGE = 5;
let idMantenimientoDetalle = null;
let buscadorMantenimientos = null; // se inicializa en DOMContentLoaded (busqueda.js)

// Resumen para dashboards de Técnico/Administrador (no es el módulo con búsqueda dedicada):
// se pide el máximo permitido, suficiente para un resumen de actividad reciente.
function cargarMantenimientos() {
    fetch(apiBase + '/mantenimientos?tamano=50')
        .then(res => res.json())
        .then(resp => {
            mantenimientosActuales = Array.isArray(resp.datos) ? resp.datos : [];

            if (rolGlobal === 'TECNICO') {
                tecnicoRecientesPage = 1;
                actualizarDashboardTecnico(mantenimientosActuales);
                renderMantenimientosRecientes(mantenimientosActuales);
            }
            if (rolGlobal === 'ADMINISTRADOR') {
                actualizarContadorMantenimientoAdmin(mantenimientosActuales);
            }
        })
        .catch(error => console.error('Error cargando mantenimientos:', error));
}

const MANT_PANEL_IDS = { info: 'mantenimientos-pagina', prev: 'btn-mantenimientos-prev', next: 'btn-mantenimientos-next' };
const MANT_TAMANO_PAGINA = 10;

function cargarMantenimientosPanelDedicado(estadoBuscador) {
    const tablaMantenimientosBody = document.getElementById('tabla-mantenimientos-body');
    if (!tablaMantenimientosBody) return;
    const e = estadoBuscador || (buscadorMantenimientos ? buscadorMantenimientos.estado : { pagina: 1, q: '', filtros: {} });

    const params = new URLSearchParams();
    if (e.q) params.set('q', e.q);
    if (e.filtros.estado) params.set('estado', e.filtros.estado);
    params.set('pagina', e.pagina);
    params.set('tamano', MANT_TAMANO_PAGINA);

    fetch(apiBase + '/mantenimientos?' + params.toString())
        .then(res => res.json())
        .then(resp => {
            mantenimientosActuales = Array.isArray(resp.datos) ? resp.datos : [];
            actualizarEncabezadoMantenimientos();
            renderTablaMantenimientosPanel(mantenimientosActuales);
            renderInfoPaginacion(MANT_PANEL_IDS, resp.pagina || 1, resp.tamanoPagina || MANT_TAMANO_PAGINA, resp.total || 0);
        })
        .catch(error => console.error('Error panel mantenimientos:', error));
}

function actualizarContadorMantenimientoAdmin(mantenimientos) {
    if (!Array.isArray(mantenimientos)) return;
    const enProceso = mantenimientos.filter(m => m.estado === 'EN_PROCESO');
    const dispositivosEnMantenimiento = new Set(enProceso.map(m => m.idDispositivo)).size;
    const cardMaintenance = document.getElementById('dash-maintenance');
    if (cardMaintenance) {
        cardMaintenance.textContent = dispositivosEnMantenimiento;
    }
}

function filtrarMantenimientosTecnico(mantenimientos) {
    const tecnicoId = Number(idUsuarioGlobal);
    return mantenimientos.filter(m => Number(m.idUsuario) === tecnicoId);
}

function actualizarDashboardTecnico(mantenimientos) {
    const tecnicos = filtrarMantenimientosTecnico(mantenimientos);
    const enProceso = tecnicos.filter(m => m.estado === 'EN_PROCESO');
    const finalizados = tecnicos.filter(m => m.estado === 'FINALIZADO');

    const dispositivosEnMantenimiento = new Set(enProceso.map(m => m.idDispositivo)).size;
    const pendientes = tecnicos.filter(m => m.estado !== 'FINALIZADO').length;

    const cardDisp = document.getElementById('card-dispositivos-mantenimiento');
    const cardProceso = document.getElementById('card-mantenimientos-proceso');
    const cardFinalizados = document.getElementById('card-mantenimientos-finalizados');
    const cardPendientes = document.getElementById('card-mantenimientos-pendientes');

    if(cardDisp) cardDisp.textContent = dispositivosEnMantenimiento;
    if(cardProceso) cardProceso.textContent = enProceso.length;
    if(cardFinalizados) cardFinalizados.textContent = finalizados.length;
    if(cardPendientes) cardPendientes.textContent = pendientes;
}

function renderMantenimientosRecientes(mantenimientos) {
    const recientes = filtrarMantenimientosTecnico(mantenimientos)
        .sort((a, b) => (parseFecha(b.fechaInicio) || 0) - (parseFecha(a.fechaInicio) || 0));

    const total = recientes.length;
    const totalPages = Math.max(1, Math.ceil(total / TECNICO_RECIENTES_PER_PAGE));
    if (tecnicoRecientesPage > totalPages) tecnicoRecientesPage = totalPages;

    const start = (tecnicoRecientesPage - 1) * TECNICO_RECIENTES_PER_PAGE;
    const pageItems = recientes.slice(start, start + TECNICO_RECIENTES_PER_PAGE);

    const body = document.getElementById('tabla-recientes-body');
    if (body) {
        body.innerHTML = '';
        if (pageItems.length === 0) {
            body.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:30px;color:#94a3b8;">No hay mantenimientos recientes</td></tr>';
        } else {
            pageItems.forEach(m => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td style="padding:12px 14px;font-weight:500;color:#1e293b;">${escapeHtml(m.nombreDispositivo || m.idDispositivo || '-')}</td>
                    <td style="padding:12px 14px;color:#475569;">${escapeHtml(m.tipo || '-')}</td>
                    <td style="padding:12px 14px;color:#64748b;">${formatearFecha(m.fechaInicio)}</td>
                    <td style="padding:12px 14px;color:#64748b;">${m.fechaFin ? formatearFecha(m.fechaFin) : 'En curso'}</td>
                    <td style="padding:12px 14px;">${getEstadoBadgeMantenimiento(m.estado)}</td>
                    <td style="padding:12px 14px;text-align:center;"><button class="view-btn btn-ver-mantenimiento" data-id="${m.idMantenimiento}">Ver</button></td>
                `;
                body.appendChild(tr);
            });

            document.querySelectorAll('.btn-ver-mantenimiento').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    const id = parseInt(e.target.getAttribute('data-id'));
                    abrirModalDetalleMantenimiento(id);
                });
            });
        }
    }

    const recPage = document.getElementById('recientes-pagina');
    const prevBtn = document.getElementById('btn-recientes-prev');
    const nextBtn = document.getElementById('btn-recientes-next');

    if (recPage) recPage.textContent = `Página ${tecnicoRecientesPage} de ${totalPages}`;
    if (prevBtn) {
        prevBtn.disabled = tecnicoRecientesPage <= 1;
        prevBtn.style.opacity = tecnicoRecientesPage <= 1 ? '0.4' : '1';
    }
    if (nextBtn) {
        nextBtn.disabled = tecnicoRecientesPage >= totalPages;
        nextBtn.style.opacity = tecnicoRecientesPage >= totalPages ? '0.4' : '1';
    }
}

function renderTablaMantenimientosPanel(lista) {
    const tablaMantenimientosBody = document.getElementById('tabla-mantenimientos-body');
    if (!tablaMantenimientosBody) return;
    tablaMantenimientosBody.innerHTML = '';
    const rol = obtenerRolActual();
    if (!lista.length) {
        tablaMantenimientosBody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:30px;color:#94a3b8;">No se encontraron mantenimientos</td></tr>';
    } else {
        lista.forEach(m => {
            const tr = document.createElement('tr');
            if (rol === 'TECNICO') {
                tr.innerHTML = `
                    <td>#${m.idMantenimiento}</td>
                    <td>${escapeHtml(m.nombreDispositivo || m.idDispositivo || '-')}</td>
                    <td>${getTipoBadgeMantenimiento(m.tipo)}</td>
                    <td>${formatearFecha(m.fechaInicio)}</td>
                    <td>${m.fechaFin ? formatearFecha(m.fechaFin) : 'En curso'}</td>
                    <td>${getEstadoBadgeMantenimiento(m.estado)}</td>
                    <td>${escapeHtml(m.descripcion || '-')}</td>
                    <td style="text-align:center;"><button class="view-btn btn-ver-mantenimiento" data-id="${m.idMantenimiento}">Ver</button></td>
                `;
            } else if (rol === 'ADMINISTRADOR') {
                tr.innerHTML = `
                    <td>#${m.idMantenimiento}</td>
                    <td>${escapeHtml(m.nombreDispositivo || m.idDispositivo || '-')}</td>
                    <td>${escapeHtml(m.nombreUsuario || '-')}</td>
                    <td>${getTipoBadgeMantenimiento(m.tipo)}</td>
                    <td>${formatearFecha(m.fechaInicio)}</td>
                    <td>${m.fechaFin ? formatearFecha(m.fechaFin) : 'En curso'}</td>
                    <td>${getEstadoBadgeMantenimiento(m.estado)}</td>
                    <td>${escapeHtml(m.descripcion || '-')}</td>
                    <td>${m.fechaCreacion ? formatearFecha(m.fechaCreacion) : '-'}</td>
                    <td style="text-align:center;"><button class="view-btn btn-ver-mantenimiento" data-id="${m.idMantenimiento}">Ver</button></td>
                `;
            } else {
                tr.innerHTML = `
                    <td>${escapeHtml(m.nombreDispositivo)}</td>
                    <td>${escapeHtml(m.nombreUsuario)}</td>
                    <td>${getTipoBadgeMantenimiento(m.tipo)}</td>
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
}

function abrirModalCrearMantenimiento() {
    const formCrearMantenimiento = document.getElementById('form-crear-mantenimiento');
    const modalCrearMantenimiento = document.getElementById('modal-crear-mantenimiento');
    if(formCrearMantenimiento) formCrearMantenimiento.reset();

    const sedeSelect = document.getElementById('mantenimiento-sede');
    const salonSelect = document.getElementById('mantenimiento-salon');

    if (sedeSelect) {
        sedeSelect.innerHTML = '<option value="">Cargando sedes...</option>';
    }
    if (salonSelect) {
        salonSelect.innerHTML = '<option value="">Seleccione una sede primero</option>';
    }

    fetch(apiBase + '/dispositivos?estado=DISPONIBLE')
        .then(res => res.json())
        .then(dispositivos => {
            const selectD = document.getElementById('mantenimiento-dispositivo');
            if(selectD) {
                selectD.innerHTML = '<option value="">Seleccione un dispositivo...</option>';
                dispositivos.forEach(d => {
                    selectD.innerHTML += `<option value="${d.idDispositivo}">${escapeHtml(d.nombre)} (${escapeHtml(d.tipo)})</option>`;
                });
            }
        });

    if (sedeSelect) {
        fetch(apiBase + '/sedes')
            .then(res => res.json())
            .then(sedes => {
                sedeSelect.innerHTML = '<option value="">Seleccione una sede...</option>';
                sedes.forEach(s => {
                    sedeSelect.innerHTML += `<option value="${s.idSede}" data-codigo="${escapeHtml(s.codigo)}" data-nombre="${escapeHtml(s.nombre)}">${escapeHtml(s.nombre)} (${escapeHtml(s.codigo)})</option>`;
                });
            })
            .catch(() => {
                sedeSelect.innerHTML = '<option value="">Error cargando sedes</option>';
            });

        sedeSelect.onchange = function() {
            if (!salonSelect) return;
            const sedeOpt = sedeSelect.options[sedeSelect.selectedIndex];
            const sedeId = sedeOpt.value;

            if (!sedeId) {
                salonSelect.innerHTML = '<option value="">Seleccione una sede primero</option>';
                return;
            }

            salonSelect.innerHTML = '<option value="">Cargando salones...</option>';
            fetch(`${apiBase}/sedes/${sedeId}/salones`)
                .then(res => res.json())
                .then(salones => {
                    salonSelect.innerHTML = '<option value="">Seleccione un salón...</option>';
                    salones.forEach(salon => {
                        salonSelect.innerHTML += `<option value="${salon.idSalon}" data-numero="${salon.numero}">${salon.numero}</option>`;
                    });
                })
                .catch(() => {
                    salonSelect.innerHTML = '<option value="">Error cargando salones</option>';
                });
        };
    }

    if (salonSelect) {
        salonSelect.onchange = function() {
            // No longer needed since ubicacion is removed
        };
    }

    if(modalCrearMantenimiento) modalCrearMantenimiento.style.display = 'flex';
}

function guardarMantenimiento(e) {
    e.preventDefault();
    const modalCrearMantenimiento = document.getElementById('modal-crear-mantenimiento');

    const payload = {
        idDispositivo: parseInt(document.getElementById('mantenimiento-dispositivo').value),
        tipo: document.getElementById('mantenimiento-tipo').value,
        estado: 'EN_PROCESO',
        descripcion: document.getElementById('mantenimiento-descripcion').value
    };

    fetch(apiBase + '/mantenimientos', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(res => res.json().then(data => ({ status: res.status, body: data })))
    .then(res => {
        if (res.status >= 200 && res.status < 300) {
            showToast(res.body.mensaje, 'success');
            modalCrearMantenimiento.style.display = 'none';
            cargarMantenimientos();
            cargarMantenimientosPanelDedicado();
            cargarDispositivos();
        } else {
            showToast(res.body.error || 'Error al registrar mantenimiento', 'error');
        }
    })
    .catch(err => showToast('Error de conexión con el servidor', 'error'));
}

function abrirModalDetalleMantenimiento(id) {
    const modalDetalleMantenimiento = document.getElementById('modal-detalle-mantenimiento');
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

document.addEventListener('DOMContentLoaded', function() {
    const modalCrearMantenimiento = document.getElementById('modal-crear-mantenimiento');
    const modalDetalleMantenimiento = document.getElementById('modal-detalle-mantenimiento');
    const formCrearMantenimiento = document.getElementById('form-crear-mantenimiento');

    const btnNuevoMantenimiento = document.getElementById('btn-nuevo-mantenimiento');
    if (btnNuevoMantenimiento) {
        btnNuevoMantenimiento.addEventListener('click', abrirModalCrearMantenimiento);
    }

    const btnCancelarMant = document.getElementById('btn-cancelar-mantenimiento');
    if(btnCancelarMant) btnCancelarMant.addEventListener('click', () => modalCrearMantenimiento.style.display = 'none');

    const btnCerrarMant = document.getElementById('btn-cerrar-detalle-mant');
    if(btnCerrarMant) btnCerrarMant.addEventListener('click', () => modalDetalleMantenimiento.style.display = 'none');

    if(formCrearMantenimiento) {
        formCrearMantenimiento.addEventListener('submit', guardarMantenimiento);
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
                    showToast(res.body.mensaje, 'success');
                    modalDetalleMantenimiento.style.display = 'none';
                    cargarMantenimientos();
                    cargarDispositivos();
                } else {
                    showToast(res.body.error || 'Error al finalizar mantenimiento', 'error');
                }
            })
            .catch(err => showToast('Error de conexión con el servidor', 'error'));
        });
    }

    const btnRecientesPrev = document.getElementById('btn-recientes-prev');
    const btnRecientesNext = document.getElementById('btn-recientes-next');
    if (btnRecientesPrev) {
        btnRecientesPrev.addEventListener('click', function() {
            if (tecnicoRecientesPage > 1) {
                tecnicoRecientesPage--;
                renderMantenimientosRecientes(mantenimientosActuales);
            }
        });
    }
    if (btnRecientesNext) {
        btnRecientesNext.addEventListener('click', function() {
            const total = filtrarMantenimientosTecnico(mantenimientosActuales).length;
            const totalPages = Math.max(1, Math.ceil(total / TECNICO_RECIENTES_PER_PAGE));
            if (tecnicoRecientesPage < totalPages) {
                tecnicoRecientesPage++;
                renderMantenimientosRecientes(mantenimientosActuales);
            }
        });
    }

    buscadorMantenimientos = crearBuscadorPaginado({ onCargar: cargarMantenimientosPanelDedicado });

    const mantBuscar = document.getElementById('mant-buscar');
    if (mantBuscar) mantBuscar.addEventListener('input', (e) => buscadorMantenimientos.onBuscar(e.target.value));

    const mantFiltroEstado = document.getElementById('mant-filtro-estado');
    if (mantFiltroEstado) mantFiltroEstado.addEventListener('change', (e) => buscadorMantenimientos.onFiltro('estado', e.target.value));

    const btnMantenimientosPrev = document.getElementById('btn-mantenimientos-prev');
    const btnMantenimientosNext = document.getElementById('btn-mantenimientos-next');
    if (btnMantenimientosPrev) {
        btnMantenimientosPrev.addEventListener('click', () => buscadorMantenimientos.irAPagina(buscadorMantenimientos.estado.pagina - 1));
    }
    if (btnMantenimientosNext) {
        btnMantenimientosNext.addEventListener('click', () => buscadorMantenimientos.irAPagina(buscadorMantenimientos.estado.pagina + 1));
    }

    const navMantenimientosBtn = document.getElementById('nav-mantenimientos-btn');
    if (navMantenimientosBtn) {
        navMantenimientosBtn.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-mantenimientos');
            buscadorMantenimientos.cargarInicial();
        });
    }

    const navHistorialBtn = document.getElementById('nav-historial-btn');
    if (navHistorialBtn) {
        navHistorialBtn.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-mantenimientos', 'nav-historial-btn');
            buscadorMantenimientos.cargarInicial();
        });
    }

    const btnRegistrarMantenimiento = document.getElementById('btn-registrar-mantenimiento');
    if (btnRegistrarMantenimiento) {
        btnRegistrarMantenimiento.addEventListener('click', abrirModalCrearMantenimiento);
    }

    const btnVerHistorial = document.getElementById('btn-ver-historial');
    if (btnVerHistorial) {
        btnVerHistorial.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-mantenimientos', 'nav-historial-btn');
            buscadorMantenimientos.cargarInicial();
        });
    }
});
