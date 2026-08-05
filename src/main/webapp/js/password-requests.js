// password-requests.js - Solicitudes de restablecimiento de contraseña (solo ADMINISTRADOR)
let buscadorPasswordRequests = null; // se inicializa en DOMContentLoaded (busqueda.js)

const PWREQ_PANEL_IDS = { info: 'password-requests-pagina', prev: 'btn-password-requests-prev', next: 'btn-password-requests-next' };
const PWREQ_TAMANO_PAGINA = 10;

window.cargarPasswordRequests = function(estadoBuscador) {
    const tbody = document.getElementById('tabla-password-requests-body');
    if (!tbody) return;
    const e = estadoBuscador || (buscadorPasswordRequests ? buscadorPasswordRequests.estado : { pagina: 1, q: '', filtros: {} });

    const params = new URLSearchParams();
    if (e.q) params.set('q', e.q);
    if (e.filtros.estado) params.set('estado', e.filtros.estado);
    params.set('pagina', e.pagina);
    params.set('tamano', PWREQ_TAMANO_PAGINA);

    fetch(`${apiBase}/admin/password-requests?${params.toString()}`, { credentials: 'include' })
        .then(res => res.json())
        .then(resp => {
            const solicitudes = Array.isArray(resp.datos) ? resp.datos : [];
            renderTablaPasswordRequests(solicitudes);
            renderInfoPaginacion(PWREQ_PANEL_IDS, resp.pagina || 1, resp.tamanoPagina || PWREQ_TAMANO_PAGINA, resp.total || 0);
        })
        .catch(error => console.error('Error cargando solicitudes de password:', error));
};

function renderTablaPasswordRequests(solicitudes) {
    const tbody = document.getElementById('tabla-password-requests-body');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (solicitudes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:30px;color:#94a3b8;">No hay solicitudes</td></tr>';
        return;
    }

    solicitudes.forEach(sol => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td style="padding:12px 14px;">${sol.idSolicitud}</td>
            <td style="padding:12px 14px; font-weight:600;">${escapeHtml(sol.nombreUsuario)}</td>
            <td style="padding:12px 14px;">${formatearFecha(sol.fechaSolicitud)}</td>
            <td style="padding:12px 14px;">${getEstadoBadge(sol.estado)}</td>
            <td style="padding:12px 14px; font-family:monospace; font-weight:bold; color:#14477b;">${sol.passwordTemporal || '---'}</td>
            <td style="padding:12px 14px; text-align:center;">
                ${sol.estado === 'PENDIENTE' ? `
                    <button class="view-btn" onclick="procesarSolicitudPassword(${sol.idSolicitud}, 'approve')" style="background:#22c55e; color:white; border:none; margin-right:5px;">Aprobar</button>
                    <button class="view-btn" onclick="procesarSolicitudPassword(${sol.idSolicitud}, 'reject')" style="background:#ef4444; color:white; border:none;">Rechazar</button>
                ` : sol.passwordTemporal ? `
                    <button class="view-btn" onclick="copiarClave('${sol.passwordTemporal}')">Copiar</button>
                ` : '---'}
            </td>
        `;
        tbody.appendChild(tr);
    });
}

window.procesarSolicitudPassword = async function(id, accion) {
    const confirmMsg = accion === 'approve' ? '¿Aprobar solicitud? Se generará una clave temporal.' : '¿Rechazar solicitud?';
    const confirmado = await confirmarAccion(confirmMsg);
    if (!confirmado) return;

    fetch(`${apiBase}/admin/password-requests/${id}/${accion}`, {
        method: 'PUT',
        credentials: 'include'
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            showToast(data.mensaje, 'success');
            if (data.passwordTemporal) {
                alert(`Solicitud Aprobada. Clave Temporal: ${data.passwordTemporal}\nEntregue esta clave al usuario.`);
            }
            cargarPasswordRequests();
        } else {
            showToast(data.error || 'Error al procesar', 'error');
        }
    })
    .catch(() => showToast('Error de conexión', 'error'));
};

window.copiarClave = function(clave) {
    navigator.clipboard.writeText(clave).then(() => {
        showToast('Clave copiada al portapapeles', 'info');
    });
};

document.addEventListener('DOMContentLoaded', function() {
    buscadorPasswordRequests = crearBuscadorPaginado({ onCargar: cargarPasswordRequests });

    const pwreqBuscar = document.getElementById('pwreq-buscar');
    if (pwreqBuscar) pwreqBuscar.addEventListener('input', (e) => buscadorPasswordRequests.onBuscar(e.target.value));

    const pwreqFiltroEstado = document.getElementById('pwreq-filtro-estado');
    if (pwreqFiltroEstado) pwreqFiltroEstado.addEventListener('change', (e) => buscadorPasswordRequests.onFiltro('estado', e.target.value));

    const btnPwreqPrev = document.getElementById('btn-password-requests-prev');
    if (btnPwreqPrev) {
        btnPwreqPrev.addEventListener('click', () => buscadorPasswordRequests.irAPagina(buscadorPasswordRequests.estado.pagina - 1));
    }

    const btnPwreqNext = document.getElementById('btn-password-requests-next');
    if (btnPwreqNext) {
        btnPwreqNext.addEventListener('click', () => buscadorPasswordRequests.irAPagina(buscadorPasswordRequests.estado.pagina + 1));
    }

    const navPasswordRequestsBtn = document.getElementById('nav-password-requests-btn');
    if (navPasswordRequestsBtn) {
        navPasswordRequestsBtn.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-password-requests');
            buscadorPasswordRequests.cargarInicial();
        });
    }
});
