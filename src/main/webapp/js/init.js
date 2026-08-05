// init.js - Arranque: verificación de sesión, auto-refresh y navegación global (se carga al final)
document.addEventListener('DOMContentLoaded', function() {

    // Verificar sesión (Opcional, pero recomendado para obtener nombre del admin)
    fetch(`${apiBase}/auth/sesion`)
        .then(res => res.json())
        .then(data => {
            if (data.authenticated && data.usuario) {
                // Guardar usuario en localStorage para que obtenerRolActual() pueda leerlo antes de que rolGlobal esté listo
                localStorage.setItem('usuario', JSON.stringify(data.usuario));

                const userNameEl = document.getElementById('admin-user-name');
                if (userNameEl) userNameEl.textContent = data.usuario.nombre;
                rolGlobal = (data.usuario.rol || '').trim().toUpperCase();
                idUsuarioGlobal = data.usuario.idUsuario;
                renderLayoutByRole();

                if (rolGlobal === 'ADMINISTRADOR') {
                    cargarDispositivos();
                    cargarPrestamos();
                    cargarMantenimientos();
                } else if (rolGlobal === 'TECNICO') {
                    cargarMantenimientos();
                } else if (rolGlobal === 'DOCENTE' || rolGlobal === 'ADMINISTRATIVO') {
                    cargarMisSolicitudes();
                }
                iniciarAutoRefresh();
                iniciarNotificaciones();
            } else {
                window.location.href = 'index.html';
            }
        });

    document.addEventListener('visibilitychange', function() {
        if (!document.hidden && rolGlobal) {
            if (rolGlobal === 'ADMINISTRADOR') {
                actualizarTarjetasAdmin();
            } else if (rolGlobal === 'TECNICO') {
                actualizarTarjetasTecnico();
            } else if (rolGlobal === 'DOCENTE' || rolGlobal === 'ADMINISTRATIVO') {
                cargarMisSolicitudes();
            }
        }
    });

    function iniciarAutoRefresh() {
        if (autoRefreshTimer) {
            clearInterval(autoRefreshTimer);
        }
        autoRefreshTimer = setInterval(function() {
            if (rolGlobal === 'ADMINISTRADOR') {
                actualizarTarjetasAdmin();
            } else if (rolGlobal === 'TECNICO') {
                actualizarTarjetasTecnico();
            } else if (rolGlobal === 'DOCENTE' || rolGlobal === 'ADMINISTRATIVO') {
                cargarMisSolicitudes();
            }
        }, 300000);
    }

    function actualizarTarjetasAdmin() {
        fetch(apiBase + '/dispositivos')
            .then(res => res.json())
            .then(dispositivos => {
                if (Array.isArray(dispositivos)) {
                    actualizarDashboard(dispositivos);
                }
            })
            .catch(error => console.error('Error actualizando tarjetas admin:', error));
    }

    function actualizarTarjetasTecnico() {
        fetch(apiBase + '/mantenimientos?tamano=50')
            .then(res => res.json())
            .then(resp => {
                if (resp && Array.isArray(resp.datos)) {
                    actualizarDashboardTecnico(resp.datos);
                }
            })
            .catch(error => console.error('Error actualizando tarjetas técnico:', error));
    }

    // TEMPORAL: botón de diagnóstico para confirmar que el deploy tiene el código de Fase 5
    // (toast propio + modal de confirmación propio, no alert()/confirm() nativos del navegador).
    // Quitar una vez confirmado que el navegador está sirviendo el código nuevo.
    const btnTestDeploy = document.getElementById('btn-test-deploy');
    if (btnTestDeploy) {
        btnTestDeploy.addEventListener('click', async () => {
            showToast('✅ Este toast es del código NUEVO (Fase 5). Si ves esta notificación flotante, el deploy está actualizado.', 'success');
            const confirmado = await confirmarAccion('Este cuadro también es NUEVO (reemplaza el confirm() nativo del navegador). Si ves este recuadro blanco con botones "Cancelar"/"Confirmar" en vez del diálogo gris del navegador, el código actual sí está corriendo.');
            showToast(confirmado ? 'Hiciste clic en Confirmar ✅' : 'Hiciste clic en Cancelar', 'info');
        });
    }

    // Lógica para cerrar sesión
    const logoutBtn = document.getElementById('btn-logout');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function() {
            fetch(`${apiBase}/auth/logout`, { method: 'GET' })
            .then(() => window.location.href = 'index.html')
            .catch(() => window.location.href = 'index.html');
        });
    }

    // Navegación de vistas
    const navInicioBtn = document.getElementById('nav-inicio-btn');
    navInicioBtn.addEventListener('click', (e) => {
        e.preventDefault();
        renderLayoutByRole();
    });
});
