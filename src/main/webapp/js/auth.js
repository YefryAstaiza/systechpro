/**
 * auth.js - Control global de permisos por rol
 * Gestiona la visibilidad del sidebar según el rol del usuario autenticado
 */

/**
 * Aplica permisos de sidebar según el rol del usuario
 * Se ejecuta al cargar el DOM
 */
function aplicarPermisosSidebar() {
    const usuarioJSON = localStorage.getItem("usuario");
    
    // Si no existe usuario en localStorage, redirigir a login
    if (!usuarioJSON) {
        console.warn("No hay usuario autenticado. Redirigiendo a login...");
        window.location.href = "index.html";
        return;
    }

    let usuario;
    try {
        usuario = JSON.parse(usuarioJSON);
    } catch (e) {
        console.error("Error al parsear usuario desde localStorage:", e);
        window.location.href = "index.html";
        return;
    }

    const rol = usuario.rol;

    // Mapeo de permisos: rol -> array de IDs de botones del sidebar permitidos
    const permisos = {
        ADMINISTRADOR: [
            "nav-inicio-btn",
            "nav-dispositivos-btn",
            "nav-prestamos-btn",
            "nav-mantenimientos-btn",
            "nav-usuarios-btn",
            "nav-auditoria-btn",
            "nav-reportes-btn"
        ],
        TECNICO: [
            "nav-inicio-btn",
            "nav-mantenimientos-btn",
            "nav-historial-btn"
        ],
        DOCENTE: [
            "nav-inicio-btn",
            "nav-mis-solicitudes-btn"
        ],
        ADMINISTRATIVO: [
            "nav-inicio-btn",
            "nav-mis-solicitudes-btn"
        ]
    };

    // Verificar que el rol sea válido
    if (!permisos[rol]) {
        console.error("Rol inválido:", rol);
        window.location.href = "index.html";
        return;
    }

    // PASO 1: Ocultar TODOS los enlaces del sidebar (con !important para sobreescribir CSS)
    const todosLosEnlaces = document.querySelectorAll(".nav-link");
    todosLosEnlaces.forEach(enlace => {
        enlace.style.setProperty('display', 'none', 'important');
    });

    // PASO 2: Mostrar solo los permisos asignados al rol (con !important)
    const permisosDelRol = permisos[rol];
    permisosDelRol.forEach(idBoton => {
        const elemento = document.getElementById(idBoton);
        if (elemento) {
            elemento.style.setProperty('display', 'flex', 'important');
        } else {
            console.warn("No se encontró elemento con ID:", idBoton);
        }
    });

    console.log(`✓ Permisos aplicados para rol: ${rol}`);
}

/**
 * Inicializa el sistema de autenticación
 * Se ejecuta cuando el DOM está completamente cargado
 */
document.addEventListener("DOMContentLoaded", () => {
    aplicarPermisosSidebar();
});
