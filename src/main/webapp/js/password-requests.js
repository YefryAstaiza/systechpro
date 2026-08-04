// password-requests.js - Solicitudes de restablecimiento de contraseña (solo ADMINISTRADOR)
window.cargarPasswordRequests = function() {
    fetch(`${apiBase}/admin/password-requests`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            if (Array.isArray(data)) {
                renderTablaPasswordRequests(data);
            }
        })
        .catch(error => console.error('Error cargando solicitudes de password:', error));
};

function renderTablaPasswordRequests(solicitudes) {
    const tbody = document.getElementById('tabla-password-requests-body');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (solicitudes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:30px;color:#94a3b8;">No hay solicitudes pendientes</td></tr>';
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
    const navPasswordRequestsBtn = document.getElementById('nav-password-requests-btn');
    if (navPasswordRequestsBtn) {
        navPasswordRequestsBtn.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-password-requests');
            cargarPasswordRequests();
        });
    }
});
