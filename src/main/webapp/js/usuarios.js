// usuarios.js - Gestión de usuarios (solo ADMINISTRADOR)
let usuariosActuales = [];
let buscadorUsuarios = null; // se inicializa en DOMContentLoaded (busqueda.js)

const USUARIOS_PANEL_IDS = { info: 'usuarios-pagina', prev: 'btn-usuarios-prev', next: 'btn-usuarios-next' };
const USUARIOS_TAMANO_PAGINA = 10;

function abrirModalUsuario(usuario = null) {
    const formUsuario = document.getElementById('form-usuario');
    const modalUsuario = document.getElementById('modal-usuario');
    formUsuario.reset();
    document.getElementById('usuario-id').value = '';

    if (usuario) {
        document.getElementById('modal-usuario-title').textContent = 'Editar Usuario';
        document.getElementById('usuario-id').value = usuario.idUsuario;
        document.getElementById('usuario-nombre').value = usuario.nombre;
        document.getElementById('usuario-correo').value = usuario.correo;
        document.getElementById('usuario-rol').value = usuario.rol;
        document.getElementById('usuario-password').required = false;
        document.getElementById('hint-password').textContent = '(Dejar en blanco para no cambiar)';
    } else {
        document.getElementById('modal-usuario-title').textContent = 'Crear Usuario';
        document.getElementById('usuario-password').required = true;
        document.getElementById('hint-password').textContent = '';
    }
    modalUsuario.style.display = 'flex';
}

function cargarUsuarios(estadoBuscador) {
    const tbody = document.getElementById('tabla-usuarios-body');
    if (!tbody) return;
    const e = estadoBuscador || (buscadorUsuarios ? buscadorUsuarios.estado : { pagina: 1, q: '', filtros: {} });

    const params = new URLSearchParams();
    if (e.q) params.set('q', e.q);
    if (e.filtros.rol) params.set('rol', e.filtros.rol);
    params.set('pagina', e.pagina);
    params.set('tamano', USUARIOS_TAMANO_PAGINA);

    fetch(`${apiBase}/usuarios?` + params.toString())
        .then(res => res.json())
        .then(resp => {
            usuariosActuales = Array.isArray(resp.datos) ? resp.datos : [];
            renderTablaUsuarios(usuariosActuales);
            renderInfoPaginacion(USUARIOS_PANEL_IDS, resp.pagina || 1, resp.tamanoPagina || USUARIOS_TAMANO_PAGINA, resp.total || 0);
        })
        .catch(error => console.error('Error cargando usuarios:', error));
}

function recargarUsuarios() {
    cargarUsuarios(buscadorUsuarios ? buscadorUsuarios.estado : undefined);
}

function guardarUsuario(e) {
    e.preventDefault();
    const modalUsuario = document.getElementById('modal-usuario');
    const rawId = document.getElementById('usuario-id').value;
    const id = rawId && rawId !== 'undefined' && rawId !== 'null' ? rawId : '';
    const password = document.getElementById('usuario-password').value;
    const nombreUsuario = document.getElementById('usuario-nombre').value;

    if (!esTextoLibreValido(nombreUsuario)) {
        showToast('Nombre de usuario inválido. Usa un nombre real y legible.', 'warning');
        return;
    }

    const payload = {
        nombre: nombreUsuario.trim(),
        correo: document.getElementById('usuario-correo').value,
        rol: document.getElementById('usuario-rol').value
    };

    if (password) {
        payload.contrasena = password;
    }

    const method = id ? 'PUT' : 'POST';
    const url = id ? `${apiBase}/usuarios/${id}` : `${apiBase}/usuarios`;

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(res => res.json().then(data => ({status: res.status, body: data})))
    .then(res => {
        if (res.status >= 200 && res.status < 300) {
            showToast(res.body.mensaje, 'success');
            modalUsuario.style.display = 'none';
            recargarUsuarios();
        } else {
            const detalle = res.body.detalle ? ' (' + res.body.detalle + ')' : '';
            showToast((res.body.error || 'Ocurrió un error al guardar') + detalle, 'error');
        }
    })
    .catch(err => {
        showToast('Error de conexión con el servidor', 'error');
    });
}

async function eliminarUsuario(id) {
    const confirmado = await confirmarAccion('¿Estás seguro de que deseas eliminar este usuario? Esta acción no se puede deshacer.');
    if (!confirmado) return;
    fetch(`${apiBase}/usuarios/${id}`, {
        method: 'DELETE'
    })
    .then(res => res.json().then(data => ({status: res.status, body: data})))
    .then(res => {
        if (res.status >= 200 && res.status < 300) {
            showToast(res.body.mensaje, 'success');
            recargarUsuarios();
        } else {
            showToast(res.body.error || 'Ocurrió un error al eliminar', 'error');
        }
    })
    .catch(err => showToast('Error de conexión con el servidor', 'error'));
}

function renderTablaUsuarios(lista) {
    const tbody = document.getElementById('tabla-usuarios-body');
    if (!tbody) return;
    tbody.innerHTML = '';
    if (!lista.length) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;padding:30px;color:#94a3b8;">No se encontraron usuarios</td></tr>';
    } else {
        lista.forEach(u => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${escapeHtml(u.nombre)}</td>
                <td>${escapeHtml(u.correo)}</td>
                <td><span class="badge" style="background:#2c3e50; color:white; padding:4px 8px; border-radius:12px; font-size:12px;">${escapeHtml(u.rol)}</span></td>
                <td>
                    <button class="btn-editar" data-id="${u.idUsuario}" style="background:#f39c12; color:white; border:none; padding:5px 10px; cursor:pointer; border-radius:4px; margin-right:5px;">Editar</button>
                    <button class="btn-eliminar" data-id="${u.idUsuario}" style="background:#e74c3c; color:white; border:none; padding:5px 10px; cursor:pointer; border-radius:4px;">Eliminar</button>
                </td>
            `;
            tbody.appendChild(tr);
        });
        document.querySelectorAll('.btn-editar').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const id = e.target.getAttribute('data-id');
                const usuario = usuariosActuales.find(user => user.idUsuario == id);
                if (usuario) abrirModalUsuario(usuario);
            });
        });
        document.querySelectorAll('.btn-eliminar').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const id = e.target.getAttribute('data-id');
                eliminarUsuario(id);
            });
        });
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const btnNuevoUsuario = document.getElementById('btn-nuevo-usuario');
    const modalUsuario = document.getElementById('modal-usuario');
    const formUsuario = document.getElementById('form-usuario');
    const btnCancelarUsuario = document.getElementById('btn-cancelar-usuario');

    btnNuevoUsuario.addEventListener('click', () => abrirModalUsuario());
    btnCancelarUsuario.addEventListener('click', () => modalUsuario.style.display = 'none');
    formUsuario.addEventListener('submit', guardarUsuario);

    buscadorUsuarios = crearBuscadorPaginado({ onCargar: cargarUsuarios });

    const usuBuscar = document.getElementById('usu-buscar');
    if (usuBuscar) usuBuscar.addEventListener('input', (e) => buscadorUsuarios.onBuscar(e.target.value));

    const usuFiltroRol = document.getElementById('usu-filtro-rol');
    if (usuFiltroRol) usuFiltroRol.addEventListener('change', (e) => buscadorUsuarios.onFiltro('rol', e.target.value));

    const quickAddUserBtn = document.getElementById('quick-add-user-btn');
    if (quickAddUserBtn) {
        quickAddUserBtn.addEventListener('click', () => {
            mostrarPanel('panel-usuarios');
            buscadorUsuarios.cargarInicial();
            abrirModalUsuario();
        });
    }

    const btnUsuariosPrev = document.getElementById('btn-usuarios-prev');
    const btnUsuariosNext = document.getElementById('btn-usuarios-next');
    if (btnUsuariosPrev) {
        btnUsuariosPrev.addEventListener('click', () => buscadorUsuarios.irAPagina(buscadorUsuarios.estado.pagina - 1));
    }
    if (btnUsuariosNext) {
        btnUsuariosNext.addEventListener('click', () => buscadorUsuarios.irAPagina(buscadorUsuarios.estado.pagina + 1));
    }

    const navUsuariosBtn = document.getElementById('nav-usuarios-btn');
    navUsuariosBtn.addEventListener('click', (e) => {
        e.preventDefault();
        mostrarPanel('panel-usuarios');
        buscadorUsuarios.cargarInicial();
    });
});
