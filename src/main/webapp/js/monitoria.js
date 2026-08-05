// monitoria.js - Checkout rápido de dispositivos para monitoría (Docente/Administrativo)
let monitoriaDisponiblesCache = [];

function cargarPanelMonitoria() {
    cargarDispositivosDisponiblesMonitoria();
    cargarMisMonitorias();
}

function cargarDispositivosDisponiblesMonitoria() {
    fetch(`${apiBase}/monitoria/disponibles`)
        .then(res => res.ok ? res.json() : Promise.reject(res.status))
        .then(dispositivos => {
            monitoriaDisponiblesCache = Array.isArray(dispositivos) ? dispositivos : [];
            renderDisponiblesMonitoria(monitoriaDisponiblesCache);
        })
        .catch(error => console.error('Error cargando dispositivos disponibles:', error));
}

function renderDisponiblesMonitoria(dispositivos) {
    const contenedor = document.getElementById('monitoria-disponibles-lista');
    if (!contenedor) return;

    if (!dispositivos.length) {
        contenedor.innerHTML = '<p style="color:#94a3b8;">No hay dispositivos disponibles.</p>';
        return;
    }

    contenedor.innerHTML = dispositivos.map(d => (
        '<div style="display:flex;align-items:center;justify-content:space-between;padding:10px 14px;border-bottom:1px solid #f1f5f9;">' +
            '<div>' +
                '<div style="font-weight:600;color:#1e293b;">' + escapeHtml(d.nombre) + '</div>' +
                '<div style="font-size:12px;color:#94a3b8;">' + escapeHtml(d.tipo) + '</div>' +
            '</div>' +
            '<button class="btn-primary" data-id="' + d.idDispositivo + '" onclick="tomarDispositivoMonitoria(' + d.idDispositivo + ')">Tomar</button>' +
        '</div>'
    )).join('');
}

function tomarDispositivoMonitoria(idDispositivo) {
    fetch(`${apiBase}/monitoria`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ idDispositivo })
    })
        .then(res => res.json().then(data => ({ ok: res.ok, data })))
        .then(({ ok, data }) => {
            if (ok) {
                showToast('Dispositivo tomado', 'success');
                cargarPanelMonitoria();
            } else {
                showToast(data.error || 'No se pudo tomar el dispositivo', 'error');
            }
        })
        .catch(() => showToast('Error de conexión', 'error'));
}

function cargarMisMonitorias() {
    fetch(`${apiBase}/monitoria/activas`)
        .then(res => res.ok ? res.json() : Promise.reject(res.status))
        .then(activas => renderMisMonitorias(Array.isArray(activas) ? activas : []))
        .catch(error => console.error('Error cargando monitorías activas:', error));
}

function renderMisMonitorias(activas) {
    const contenedor = document.getElementById('monitoria-activas-lista');
    if (!contenedor) return;

    if (!activas.length) {
        contenedor.innerHTML = '<p style="color:#94a3b8;">No tienes dispositivos en monitoría.</p>';
        return;
    }

    contenedor.innerHTML = activas.map(m => (
        '<div style="display:flex;align-items:center;justify-content:space-between;padding:10px 14px;border-bottom:1px solid #f1f5f9;">' +
            '<div>' +
                '<div style="font-weight:600;color:#1e293b;">' + escapeHtml(m.nombreDispositivo) + '</div>' +
                '<div style="font-size:12px;color:#94a3b8;">Desde ' + formatearFecha(m.fechaToma) + '</div>' +
            '</div>' +
            '<button class="btn-secondary" onclick="devolverDispositivoMonitoria(' + m.idMonitoria + ')">Devolver</button>' +
        '</div>'
    )).join('');
}

function devolverDispositivoMonitoria(idMonitoria) {
    fetch(`${apiBase}/monitoria/${idMonitoria}/devolver`, { method: 'PUT' })
        .then(res => res.json().then(data => ({ ok: res.ok, data })))
        .then(({ ok, data }) => {
            if (ok) {
                showToast('Dispositivo devuelto', 'success');
                cargarPanelMonitoria();
            } else {
                showToast(data.error || 'No se pudo devolver el dispositivo', 'error');
            }
        })
        .catch(() => showToast('Error de conexión', 'error'));
}

document.addEventListener('DOMContentLoaded', function() {
    const buscador = document.getElementById('monitoria-buscador');
    if (buscador) {
        buscador.addEventListener('input', function() {
            const filtro = buscador.value.trim().toLowerCase();
            const filtrados = !filtro ? monitoriaDisponiblesCache : monitoriaDisponiblesCache.filter(d =>
                d.nombre.toLowerCase().includes(filtro) || d.tipo.toLowerCase().includes(filtro)
            );
            renderDisponiblesMonitoria(filtrados);
        });
    }

    const navMonitoriaBtn = document.getElementById('nav-monitoria-btn');
    if (navMonitoriaBtn) {
        navMonitoriaBtn.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-monitoria');
            cargarPanelMonitoria();
        });
    }
});
