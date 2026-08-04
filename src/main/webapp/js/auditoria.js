// auditoria.js - Registro de auditoría (solo ADMINISTRADOR)
let auditoriaActuales = [];
let auditoriaPage = 1;

function cargarAuditoria() {
    const tablaAuditoriaBody = document.getElementById('tabla-auditoria-body');
    if (!tablaAuditoriaBody) return;
    fetch(apiBase + '/auditoria')
        .then(res => res.json())
        .then(logs => {
            auditoriaActuales = Array.isArray(logs) ? logs : [];
            auditoriaPage = 1;
            renderTablaAuditoria();
        })
        .catch(error => console.error('Error cargando auditoria:', error));
}

function renderTablaAuditoria() {
    const tablaAuditoriaBody = document.getElementById('tabla-auditoria-body');
    const total = auditoriaActuales.length;
    const totalPages = Math.max(1, Math.ceil(total / 10));
    if (auditoriaPage > totalPages) auditoriaPage = totalPages;

    const start = (auditoriaPage - 1) * 10;
    const pageItems = auditoriaActuales.slice(start, start + 10);

    tablaAuditoriaBody.innerHTML = '';
    if (pageItems.length === 0) {
        tablaAuditoriaBody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:30px;color:#94a3b8;">No hay registros de auditoría</td></tr>';
    } else {
        pageItems.forEach(log => {
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

    const info = document.getElementById('auditoria-pagina');
    if (info) info.textContent = `Página ${auditoriaPage} de ${totalPages}`;

    const prevBtn = document.getElementById('btn-auditoria-prev');
    const nextBtn = document.getElementById('btn-auditoria-next');
    if(prevBtn) {
        prevBtn.disabled = auditoriaPage <= 1;
        prevBtn.style.opacity = auditoriaPage <= 1 ? '0.4' : '1';
    }
    if(nextBtn) {
        nextBtn.disabled = auditoriaPage >= totalPages;
        nextBtn.style.opacity = auditoriaPage >= totalPages ? '0.4' : '1';
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const btnAudPrev = document.getElementById('btn-auditoria-prev');
    if(btnAudPrev) {
        btnAudPrev.addEventListener('click', () => {
            if (auditoriaPage > 1) {
                auditoriaPage--;
                renderTablaAuditoria();
            }
        });
    }

    const btnAudNext = document.getElementById('btn-auditoria-next');
    if(btnAudNext) {
        btnAudNext.addEventListener('click', () => {
            const totalPages = Math.ceil(auditoriaActuales.length / 10);
            if (auditoriaPage < totalPages) {
                auditoriaPage++;
                renderTablaAuditoria();
            }
        });
    }

    const navAuditoriaBtn = document.getElementById('nav-auditoria-btn');
    if (navAuditoriaBtn) {
        navAuditoriaBtn.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-auditoria');
            cargarAuditoria();
        });
    }
});
