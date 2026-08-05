// auditoria.js - Registro de auditoría (solo ADMINISTRADOR)
let auditoriaActuales = [];
let buscadorAuditoria = null; // se inicializa en DOMContentLoaded (busqueda.js)

const AUDITORIA_PANEL_IDS = { info: 'auditoria-pagina', prev: 'btn-auditoria-prev', next: 'btn-auditoria-next' };
const AUDITORIA_TAMANO_PAGINA = 10;

function cargarAuditoria(estadoBuscador) {
    const tablaAuditoriaBody = document.getElementById('tabla-auditoria-body');
    if (!tablaAuditoriaBody) return;
    const e = estadoBuscador || (buscadorAuditoria ? buscadorAuditoria.estado : { pagina: 1, q: '', filtros: {} });

    const params = new URLSearchParams();
    if (e.q) params.set('q', e.q);
    if (e.filtros.fechaDesde) params.set('fechaDesde', e.filtros.fechaDesde);
    if (e.filtros.fechaHasta) params.set('fechaHasta', e.filtros.fechaHasta);
    params.set('pagina', e.pagina);
    params.set('tamano', AUDITORIA_TAMANO_PAGINA);

    fetch(apiBase + '/auditoria?' + params.toString())
        .then(res => res.json())
        .then(resp => {
            auditoriaActuales = Array.isArray(resp.datos) ? resp.datos : [];
            renderTablaAuditoria(auditoriaActuales);
            renderInfoPaginacion(AUDITORIA_PANEL_IDS, resp.pagina || 1, resp.tamanoPagina || AUDITORIA_TAMANO_PAGINA, resp.total || 0);
        })
        .catch(error => console.error('Error cargando auditoria:', error));
}

function renderTablaAuditoria(lista) {
    const tablaAuditoriaBody = document.getElementById('tabla-auditoria-body');
    if (!tablaAuditoriaBody) return;
    tablaAuditoriaBody.innerHTML = '';
    if (!lista.length) {
        tablaAuditoriaBody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:30px;color:#94a3b8;">No hay registros de auditoría</td></tr>';
    } else {
        lista.forEach(log => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td style="padding:12px 14px;">${formatearFecha(log.fechaEvento)}</td>
                <td style="padding:12px 14px;">${escapeHtml(log.nombreUsuario || log.idUsuario)}</td>
                <td style="padding:12px 14px;"><span class="badge" style="background:#e0f2fe;color:#0369a1;">${escapeHtml(log.accion)}</span></td>
                <td style="padding:12px 14px;">${escapeHtml(log.tablaAfectada || '-')}</td>
                <td style="padding:12px 14px;">${escapeHtml(log.descripcion)}</td>
                <td style="padding:12px 14px;color:#94a3b8;">${escapeHtml(log.ip || '-')}</td>
            `;
            tablaAuditoriaBody.appendChild(tr);
        });
    }
}

document.addEventListener('DOMContentLoaded', function() {
    buscadorAuditoria = crearBuscadorPaginado({ onCargar: cargarAuditoria });

    const audBuscar = document.getElementById('aud-buscar');
    if (audBuscar) audBuscar.addEventListener('input', (e) => buscadorAuditoria.onBuscar(e.target.value));

    const audDesde = document.getElementById('aud-fecha-desde');
    if (audDesde) audDesde.addEventListener('change', (e) => buscadorAuditoria.onFiltro('fechaDesde', e.target.value));

    const audHasta = document.getElementById('aud-fecha-hasta');
    if (audHasta) audHasta.addEventListener('change', (e) => buscadorAuditoria.onFiltro('fechaHasta', e.target.value));

    const btnAudPrev = document.getElementById('btn-auditoria-prev');
    if (btnAudPrev) {
        btnAudPrev.addEventListener('click', () => buscadorAuditoria.irAPagina(buscadorAuditoria.estado.pagina - 1));
    }

    const btnAudNext = document.getElementById('btn-auditoria-next');
    if (btnAudNext) {
        btnAudNext.addEventListener('click', () => buscadorAuditoria.irAPagina(buscadorAuditoria.estado.pagina + 1));
    }

    const navAuditoriaBtn = document.getElementById('nav-auditoria-btn');
    if (navAuditoriaBtn) {
        navAuditoriaBtn.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-auditoria');
            buscadorAuditoria.cargarInicial();
        });
    }
});
