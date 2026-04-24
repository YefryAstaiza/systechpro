// admin.js - Redirección y lógica básica para la interfaz de admin

document.addEventListener('DOMContentLoaded', function() {
    // Lógica para cerrar sesión
    const logoutBtn = document.querySelector('.logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function() {
            const apiBase = window.location.origin + '/systechpro/api';
            fetch(`${apiBase}/auth/logout`, {
                method: 'GET',
                credentials: 'include'
            })
            .then(() => {
                window.location.href = 'index.html';
            })
            .catch(() => {
                window.location.href = 'index.html';
            });
        });
    }
    // Aquí puedes agregar más lógica JS para la interfaz de admin
});
