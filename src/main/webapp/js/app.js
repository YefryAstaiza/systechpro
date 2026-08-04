// SysTechPro - Página de login
const API_BASE = window.location.origin + '/systechpro/api';

// Elementos del DOM
const loginView = document.getElementById('login-view');
const loginForm = document.getElementById('login-form');
const loginError = document.getElementById('login-error');
const modalForgot = document.getElementById('modal-forgot');
const modalChangePass = document.getElementById('modal-change-pass');

// Inicialización
document.addEventListener('DOMContentLoaded', () => {
    checkSession();
    setupEventListeners();
});

// Verificar sesión: si ya hay una sesión activa, redirige directamente al panel
function checkSession() {
    fetch(`${API_BASE}/auth/sesion`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            if (data.authenticated && data.usuario) {
                window.location.href = window.location.origin + '/systechpro/admin.html';
            } else {
                showLogin();
            }
        })
        .catch(() => showLogin());
}

// Configurar eventos
function setupEventListeners() {
    if (loginForm) loginForm.addEventListener('submit', handleLogin);

    const forgotLink = document.getElementById('forgot-password-link');
    if (forgotLink) {
        forgotLink.addEventListener('click', (e) => {
            e.preventDefault();
            if (modalForgot) modalForgot.classList.add('active');
        });
    }

    const closeForgotBtn = document.querySelector('.close-modal-forgot');
    if (closeForgotBtn) {
        closeForgotBtn.addEventListener('click', () => {
            if (modalForgot) modalForgot.classList.remove('active');
        });
    }

    const forgotForm = document.getElementById('forgot-form');
    if (forgotForm) forgotForm.addEventListener('submit', handleForgotRequest);

    const changePassForm = document.getElementById('change-pass-form');
    if (changePassForm) changePassForm.addEventListener('submit', handleChangePassword);
}

// Login
function handleLogin(e) {
    e.preventDefault();
    const correo = document.getElementById('correo').value;
    const password = document.getElementById('password').value;

    fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ correo, password }),
        credentials: 'include'
    })
    .then(res => res.json())
    .then(data => {
        if (data.success && data.usuario) {
            if (data.requirePasswordChange) {
                document.getElementById('change-pass-id').value = data.usuario.idUsuario;
                modalChangePass.classList.add('active');
                return;
            }
            window.location.replace(window.location.origin + '/systechpro/admin.html');
        } else {
            loginError.textContent = data.error || 'Error en el login';
        }
    })
    .catch(() => {
        loginError.textContent = 'Error de conexión';
    });
}

function handleForgotRequest(e) {
    e.preventDefault();
    const correo = document.getElementById('forgot-correo').value;
    const msg = document.getElementById('forgot-message');
    msg.textContent = 'Enviando solicitud...';
    msg.style.color = '#333';

    fetch(`${API_BASE}/auth/reset-request`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ correo })
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            msg.textContent = 'Solicitud enviada. Contacta al administrador para tu nueva clave.';
            msg.style.color = 'green';
            e.target.reset();
        } else {
            msg.textContent = data.error || 'Error al enviar solicitud';
            msg.style.color = 'red';
        }
    })
    .catch(() => {
        msg.textContent = 'Error de conexión';
        msg.style.color = 'red';
    });
}

function handleChangePassword(e) {
    e.preventDefault();
    const idUsuario = document.getElementById('change-pass-id').value;
    const newPassword = document.getElementById('new-password').value;
    const confirmPassword = document.getElementById('confirm-password').value;
    const errorMsg = document.getElementById('change-pass-error');

    if (newPassword !== confirmPassword) {
        errorMsg.textContent = 'Las contraseñas no coinciden';
        return;
    }

    fetch(`${API_BASE}/auth/change-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ idUsuario, newPassword })
    })
    .then(res => res.json())
    .then(data => {
        if (data.success) {
            alert('Contraseña actualizada correctamente. Inicia sesión con tu nueva contraseña.');
            modalChangePass.classList.remove('active');
            loginForm.reset();
        } else {
            errorMsg.textContent = data.error || 'Error al actualizar';
        }
    })
    .catch(() => {
        errorMsg.textContent = 'Error de conexión';
    });
}

// Mostrar login (#login-view ya viene con class="active" en el HTML por defecto)
function showLogin() {
    if (loginView) loginView.classList.add('active');
}
