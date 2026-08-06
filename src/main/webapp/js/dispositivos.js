// dispositivos.js - Gestión de dispositivos y tarjetas del dashboard de ADMINISTRADOR
let todosLosDispositivos = [];
let dispPaginaActual = 1;
const DISP_POR_PAGINA = 10;

function abrirFormDispositivo(dispositivo) {
    const formDispositivo = document.getElementById('form-dispositivo');
    formDispositivo.reset();
    document.getElementById('dispositivo-id').value = '';
    document.getElementById('disp-form-titulo').textContent = dispositivo ? 'Editar Dispositivo' : 'Registrar Nuevo Dispositivo';
    if (dispositivo) {
        document.getElementById('dispositivo-id').value = dispositivo.idDispositivo;
        document.getElementById('dispositivo-nombre').value = dispositivo.nombre;
        document.getElementById('dispositivo-tipo').value = dispositivo.tipo;
        document.getElementById('dispositivo-estado').value = dispositivo.estado;
        document.getElementById('dispositivo-descripcion').value = dispositivo.descripcion || '';
    }
}

function resetFormDispositivo() {
    const formDispositivo = document.getElementById('form-dispositivo');
    formDispositivo.reset();
    document.getElementById('dispositivo-id').value = '';
    document.getElementById('disp-form-titulo').textContent = 'Gestión Detallada del Dispositivo';
}

function actualizarDashboard(dispositivos) {
    document.getElementById('dash-total').textContent = dispositivos.length;
    document.getElementById('dash-available').textContent = dispositivos.filter(function(d) { return d.estado === 'DISPONIBLE'; }).length;
    document.getElementById('dash-inuse').textContent = dispositivos.filter(function(d) { return d.estado === 'EN_USO'; }).length;
    document.getElementById('dash-maintenance').textContent = dispositivos.filter(function(d) { return d.estado === 'MANTENIMIENTO'; }).length;
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

    var tablaDispositivosBody = document.getElementById('tabla-dispositivos-body');
    tablaDispositivosBody.innerHTML = '';
    if (pagina.length === 0) {
        tablaDispositivosBody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:30px;color:#94a3b8;">No se encontraron dispositivos</td></tr>';
    } else {
        var esAdmin = obtenerRolActual() === 'ADMINISTRADOR';
        pagina.forEach(function(d) {
            var codigo = 'D' + String(d.idDispositivo).padStart(3, '0');
            var tr = document.createElement('tr');
            tr.style.cssText = 'border-bottom:1px solid #f1f5f9;transition:background .15s;';
            var fila = [
                '<td style="padding:12px 14px;font-weight:600;color:#64748b;">' + codigo + '</td>',
                '<td style="padding:12px 14px;font-weight:500;color:#1e293b;">' + escapeHtml(d.nombre) + '</td>',
                '<td style="padding:12px 14px;color:#475569;">' + escapeHtml(d.tipo) + '</td>',
                '<td style="padding:12px 14px;">' + getEstadoBadgeDisp(d.estado) + '</td>',
                '<td style="padding:12px 14px;color:#64748b;font-size:13px;">' + (d.fechaCreacion ? formatearFecha(d.fechaCreacion) : '—') + '</td>'
            ];
            var acciones = [
                '  <button class="btn-info-disp" data-id="' + d.idDispositivo + '" data-nombre="' + escapeHtml(d.nombre) + '" title="Ver quién lo tiene" aria-label="Ver quién tiene el dispositivo ' + escapeHtml(d.nombre) + '" style="background:none;border:none;cursor:pointer;padding:5px;color:#2563eb;">',
                '    <svg aria-hidden="true" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>',
                '  </button>'
            ];
            if (esAdmin) {
                acciones.push(
                    '  <button class="btn-editar-disp" data-id="' + d.idDispositivo + '" title="Editar" aria-label="Editar dispositivo ' + escapeHtml(d.nombre) + '" style="background:none;border:none;cursor:pointer;padding:5px;color:#f59e0b;">',
                    '    <svg aria-hidden="true" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>',
                    '  </button>',
                    '  <button class="btn-eliminar-disp" data-id="' + d.idDispositivo + '" title="Eliminar" aria-label="Eliminar dispositivo ' + escapeHtml(d.nombre) + '" style="background:none;border:none;cursor:pointer;padding:5px;color:#ef4444;">',
                    '    <svg aria-hidden="true" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/></svg>',
                    '  </button>'
                );
            }
            fila.push('<td style="padding:12px 14px;text-align:center;">' + acciones.join('') + '</td>');
            tr.innerHTML = fila.join('');
            tablaDispositivosBody.appendChild(tr);
        });

        document.querySelectorAll('.btn-info-disp').forEach(function(btn) {
            btn.addEventListener('click', function(e) {
                var id = e.currentTarget.getAttribute('data-id');
                var nombre = e.currentTarget.getAttribute('data-nombre');
                abrirModalDispositivoActual(id, nombre);
            });
        });
        if (esAdmin) {
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
    }

    var infoPagina = document.getElementById('disp-info-pagina');
    var btnPrev = document.getElementById('btn-disp-prev');
    var btnNext = document.getElementById('btn-disp-next');
    if(infoPagina) infoPagina.textContent = total > 0 ? ('Mostrando ' + (inicio+1) + '–' + fin + ' de ' + total + ' dispositivos') : 'Sin resultados';
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

function guardarDispositivo(e) {
    e.preventDefault();
    var id = document.getElementById('dispositivo-id').value;
    var nombre = document.getElementById('dispositivo-nombre').value;
    var descripcion = document.getElementById('dispositivo-descripcion').value;

    if (!esNombreDispositivoValido(nombre)) {
        showToast('Nombre inválido. Usa un nombre descriptivo, sin caracteres extraños y con al menos 2 letras.', 'warning');
        return;
    }

    if (!esDescripcionValida(descripcion)) {
        showToast('Descripción inválida. Máximo 250 caracteres y solo texto normal permitido.', 'warning');
        return;
    }

    var payload = {
        nombre: nombre.trim(),
        tipo: document.getElementById('dispositivo-tipo').value,
        estado: document.getElementById('dispositivo-estado').value,
        descripcion: descripcion.trim()
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
        .catch(function() { showToast('Error de conexión con el servidor', 'error'); });
}

async function eliminarDispositivo(id) {
    var disp = todosLosDispositivos.find(function(d) { return d.idDispositivo == id; });
    if (disp && disp.estado === 'EN_USO') {
        showToast('No se puede eliminar un dispositivo que está EN USO', 'warning');
        return;
    }
    const confirmado = await confirmarAccion('¿Eliminar el dispositivo "' + (disp ? disp.nombre : '') + '"? Esta acción no se puede deshacer.');
    if (!confirmado) return;
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
        .catch(function() { showToast('Error de conexión con el servidor', 'error'); });
}

function abrirModalDispositivoActual(idDispositivo, nombreDispositivo) {
    var modal = document.getElementById('modal-dispositivo-actual');
    var sinPrestamo = document.getElementById('dispact-sin-prestamo');
    var conPrestamo = document.getElementById('dispact-con-prestamo');

    document.getElementById('dispact-nombre-dispositivo').textContent = nombreDispositivo;
    sinPrestamo.style.display = 'none';
    conPrestamo.style.display = 'none';
    modal.style.display = 'flex';

    fetch(apiBase + '/prestamos/dispositivo/' + idDispositivo + '/actual')
        .then(function(res) { return res.json(); })
        .then(function(resp) {
            if (resp.activo && resp.prestamo) {
                document.getElementById('dispact-solicitante').textContent = resp.prestamo.nombreUsuario || '—';
                document.getElementById('dispact-aprobador').textContent = resp.prestamo.nombreAprobador || '—';
                document.getElementById('dispact-inicio').textContent = formatearFecha(resp.prestamo.fechaInicio);
                document.getElementById('dispact-fin').textContent = formatearFecha(resp.prestamo.fechaFin);
                conPrestamo.style.display = 'block';
            } else {
                sinPrestamo.style.display = 'block';
            }
        })
        .catch(function() {
            modal.style.display = 'none';
            showToast('Error al consultar la trazabilidad del dispositivo', 'error');
        });
}

document.addEventListener('DOMContentLoaded', function() {
    const formDispositivo = document.getElementById('form-dispositivo');
    formDispositivo.addEventListener('submit', guardarDispositivo);

    document.getElementById('btn-nuevo-dispositivo').addEventListener('click', function() { abrirFormDispositivo(null); });
    document.getElementById('btn-cancelar-dispositivo').addEventListener('click', resetFormDispositivo);
    document.getElementById('btn-cerrar-dispositivo-actual').addEventListener('click', function() {
        document.getElementById('modal-dispositivo-actual').style.display = 'none';
    });
    document.getElementById('disp-buscar').addEventListener('input', aplicarFiltrosDispositivos);
    document.getElementById('disp-filtro-estado').addEventListener('change', aplicarFiltrosDispositivos);
    document.getElementById('btn-disp-prev').addEventListener('click', function() {
        if (dispPaginaActual > 1) { dispPaginaActual--; renderTablaDispositivos(); }
    });
    document.getElementById('btn-disp-next').addEventListener('click', function() {
        var filtered = getDispositivosFiltrados();
        if (dispPaginaActual < Math.ceil(filtered.length / DISP_POR_PAGINA)) { dispPaginaActual++; renderTablaDispositivos(); }
    });

    const navDispositivosBtn = document.getElementById('nav-dispositivos-btn');
    navDispositivosBtn.addEventListener('click', (e) => {
        e.preventDefault();
        mostrarPanel('panel-dispositivos');
        cargarDispositivos();
    });

    const quickAddDispositivoBtn = document.getElementById('quick-add-dispositivo-btn');
    quickAddDispositivoBtn.addEventListener('click', () => {
        mostrarPanel('panel-dispositivos');
        cargarDispositivos();
        setTimeout(() => abrirFormDispositivo(null), 100);
    });
});
