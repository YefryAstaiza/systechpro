// admin.js - Redirección y lógica básica para la interfaz de admin

document.addEventListener('DOMContentLoaded', function() {
    // Lógica para cerrar sesión
    const logoutBtn = document.querySelector('.logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function() {
            window.location.href = 'login.html';
        });
    }
    // Aquí puedes agregar más lógica JS para la interfaz de admin
});
