// corte-diario.js - Snapshot histórico manual del inventario (Administrador/Técnico)

function cargarHistorialCortes() {
    fetch(`${apiBase}/cortes`)
        .then(res => res.ok ? res.json() : Promise.reject(res.status))
        .then(cortes => renderHistorialCortes(Array.isArray(cortes) ? cortes : []))
        .catch(error => console.error('Error cargando historial de cortes:', error));
}

function renderHistorialCortes(cortes) {
    const tbody = document.getElementById('cortes-tbody');
    if (!tbody) return;

    if (!cortes.length) {
        tbody.innerHTML = '<tr><td colspan="7" style="padding:16px;text-align:center;color:#94a3b8;">Aún no se ha generado ningún corte.</td></tr>';
        return;
    }

    tbody.innerHTML = cortes.map(c => (
        '<tr>' +
            '<td class="th-cell--nowrap">' + formatearFecha(c.fechaCorte) + '</td>' +
            '<td class="th-cell">' + c.totalDispositivos + '</td>' +
            '<td class="th-cell">' + c.disponibles + '</td>' +
            '<td class="th-cell">' + c.enPrestamo + '</td>' +
            '<td class="th-cell">' + c.enMantenimiento + '</td>' +
            '<td class="th-cell">' + escapeHtml(c.nombreGenerador) + '</td>' +
            '<td class="th-cell--center"><button class="btn-secondary" onclick="verDetalleCorte(' + c.idCorte + ')">Ver</button></td>' +
        '</tr>'
    )).join('');
}

function generarCorteDiario() {
    confirmarAccion('¿Generar el corte diario ahora? Quedará guardado como historial permanente.')
        .then(confirmado => {
            if (!confirmado) return;
            fetch(`${apiBase}/cortes`, { method: 'POST' })
                .then(res => res.json().then(data => ({ ok: res.ok, data })))
                .then(({ ok, data }) => {
                    if (ok) {
                        showToast('Corte generado con éxito', 'success');
                        cargarHistorialCortes();
                    } else {
                        showToast(data.error || 'No se pudo generar el corte', 'error');
                    }
                })
                .catch(() => showToast('Error de conexión', 'error'));
        });
}

function verDetalleCorte(idCorte) {
    fetch(`${apiBase}/cortes/${idCorte}`)
        .then(res => res.ok ? res.json() : Promise.reject(res.status))
        .then(corte => {
            const resumen = document.getElementById('corte-detalle-resumen');
            const tbody = document.getElementById('corte-detalle-tbody');
            if (resumen) {
                resumen.textContent = formatearFecha(corte.fechaCorte) + ' — Total: ' + corte.totalDispositivos +
                    ' · Disponibles: ' + corte.disponibles + ' · En préstamo: ' + corte.enPrestamo +
                    ' · En mantenimiento: ' + corte.enMantenimiento +
                    ' · Generado por: ' + corte.nombreGenerador;
            }
            if (tbody) {
                const detalle = corte.detalle || [];
                tbody.innerHTML = !detalle.length
                    ? '<tr><td colspan="3" style="padding:16px;text-align:center;color:#94a3b8;">Ningún préstamo estaba activo en este corte.</td></tr>'
                    : detalle.map(d => (
                        '<tr>' +
                            '<td class="th-cell">' + escapeHtml(d.nombreDispositivo) + '</td>' +
                            '<td class="th-cell">' + escapeHtml(d.nombreUsuario) + '</td>' +
                            '<td class="th-cell">' + formatearFecha(d.fechaToma) + '</td>' +
                        '</tr>'
                    )).join('');
            }
            const modal = document.getElementById('modal-detalle-corte');
            if (modal) modal.classList.add('active');
        })
        .catch(() => showToast('Error al cargar el detalle del corte', 'error'));
}

document.addEventListener('DOMContentLoaded', function() {
    const btnGenerar = document.getElementById('btn-generar-corte');
    if (btnGenerar) {
        btnGenerar.addEventListener('click', generarCorteDiario);
    }

    const btnCerrarDetalle = document.getElementById('btn-cerrar-detalle-corte');
    if (btnCerrarDetalle) {
        btnCerrarDetalle.addEventListener('click', function() {
            const modal = document.getElementById('modal-detalle-corte');
            if (modal) modal.classList.remove('active');
        });
    }

    const navCorteDiarioBtn = document.getElementById('nav-corte-diario-btn');
    if (navCorteDiarioBtn) {
        navCorteDiarioBtn.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-corte-diario');
            cargarHistorialCortes();
        });
    }
});
