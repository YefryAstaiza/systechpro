// notificaciones.js - Campana de notificaciones en el panel (polling, sin dependencias externas)
let notifPollingTimer = null;

function cargarNotificaciones() {
    fetch(`${apiBase}/notificaciones`)
        .then(res => res.ok ? res.json() : Promise.reject(res.status))
        .then(data => {
            actualizarBadgeNotificaciones(data.noLeidas || 0);
            renderListaNotificaciones(data.notificaciones || []);
        })
        .catch(error => console.error('Error cargando notificaciones:', error));
}

function actualizarBadgeNotificaciones(noLeidas) {
    const badge = document.getElementById('notif-badge');
    if (!badge) return;
    if (noLeidas > 0) {
        badge.textContent = noLeidas > 99 ? '99+' : String(noLeidas);
        badge.classList.remove('hidden');
    } else {
        badge.classList.add('hidden');
    }
}

function renderListaNotificaciones(notificaciones) {
    const lista = document.getElementById('notif-list');
    if (!lista) return;

    if (!notificaciones.length) {
        lista.innerHTML = '<div class="notif-empty">No tienes notificaciones</div>';
        return;
    }

    lista.innerHTML = notificaciones.map(n => {
        const claseLeida = n.leida ? 'leida' : 'no-leida';
        return '<div class="notif-item ' + claseLeida + '" data-id="' + n.idNotificacion + '">' +
            '<span class="notif-dot"></span>' +
            '<div class="notif-item-body">' +
                '<div class="notif-item-mensaje">' + escapeHtml(n.mensaje) + '</div>' +
                '<div class="notif-item-fecha">' + formatearFecha(n.fechaCreacion) + '</div>' +
            '</div>' +
        '</div>';
    }).join('');

    lista.querySelectorAll('.notif-item.no-leida').forEach(item => {
        item.addEventListener('click', () => marcarNotificacionLeida(item.dataset.id, item));
    });
}

function marcarNotificacionLeida(idNotificacion, itemEl) {
    fetch(`${apiBase}/notificaciones/${idNotificacion}`, { method: 'PUT' })
        .then(res => res.ok ? res.json() : Promise.reject(res.status))
        .then(() => {
            if (itemEl) {
                itemEl.classList.remove('no-leida');
                itemEl.classList.add('leida');
            }
            cargarNotificaciones();
        })
        .catch(error => console.error('Error marcando notificación como leída:', error));
}

function marcarTodasNotificacionesLeidas() {
    fetch(`${apiBase}/notificaciones/leer-todas`, { method: 'PUT' })
        .then(res => res.ok ? res.json() : Promise.reject(res.status))
        .then(() => cargarNotificaciones())
        .catch(error => console.error('Error marcando todas las notificaciones como leídas:', error));
}

function iniciarNotificaciones() {
    const btnBell = document.getElementById('btn-notificaciones');
    const dropdown = document.getElementById('notif-dropdown');
    const btnMarcarTodas = document.getElementById('btn-marcar-todas-leidas');
    if (!btnBell || !dropdown) return;

    cargarNotificaciones();

    btnBell.addEventListener('click', (e) => {
        e.stopPropagation();
        dropdown.classList.toggle('hidden');
    });

    document.addEventListener('click', (e) => {
        if (!dropdown.classList.contains('hidden') && !dropdown.contains(e.target) && e.target !== btnBell) {
            dropdown.classList.add('hidden');
        }
    });

    if (btnMarcarTodas) {
        btnMarcarTodas.addEventListener('click', (e) => {
            e.stopPropagation();
            marcarTodasNotificacionesLeidas();
        });
    }

    if (notifPollingTimer) clearInterval(notifPollingTimer);
    notifPollingTimer = setInterval(cargarNotificaciones, 30000);
}
