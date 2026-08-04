// usuarios.js - Gestión de usuarios (solo ADMINISTRADOR)
let usuariosActuales = [];
let usuariosPage = 1;

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

function cargarUsuarios() {
    fetch(`${apiBase}/usuarios`)
        .then(res => res.json())
        .then(usuarios => {
            usuariosActuales = Array.isArray(usuarios) ? usuarios : [];
            renderTablaUsuarios();
        })
        .catch(error => console.error('Error cargando usuarios:', error));
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
            alert(res.body.mensaje);
            modalUsuario.style.display = 'none';
            cargarUsuarios();
        } else {
            const detalle = res.body.detalle ? ' (' + res.body.detalle + ')' : '';
            alert((res.body.error || 'Ocurrió un error al guardar') + detalle);
        }
    })
    .catch(err => {
        alert('Error de conexión con el servidor');
    });
}

function eliminarUsuario(id) {
    if (confirm('¿Estás seguro de que deseas eliminar este usuario? Esta acción no se puede deshacer.')) {
        fetch(`${apiBase}/usuarios/${id}`, {
            method: 'DELETE'
        })
        .then(res => res.json().then(data => ({status: res.status, body: data})))
        .then(res => {
            if (res.status >= 200 && res.status < 300) {
                alert(res.body.mensaje);
                cargarUsuarios();
            } else {
                alert(res.body.error || 'Ocurrió un error al eliminar');
            }
        })
        .catch(err => alert('Error de conexión con el servidor'));
    }
}

function renderTablaUsuarios() {
    const tbody = document.getElementById('tabla-usuarios-body');
    if (!tbody) return;
    const lista = Array.isArray(usuariosActuales) ? usuariosActuales.slice() : [];
    lista.sort((a, b) => a.nombre.localeCompare(b.nombre));
    const total = lista.length;
    const totalPages = Math.max(1, Math.ceil(total / PANEL_PAGE_SIZE));
    if (usuariosPage > totalPages) usuariosPage = totalPages;
    const inicio = (usuariosPage - 1) * PANEL_PAGE_SIZE;
    const pagina = lista.slice(inicio, inicio + PANEL_PAGE_SIZE);
    tbody.innerHTML = '';
    if (pagina.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;padding:30px;color:#94a3b8;">No se encontraron usuarios</td></tr>';
    } else {
        pagina.forEach(u => {
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
    const pageLabel = document.getElementById('usuarios-pagina');
    if (pageLabel) pageLabel.textContent = 'Página ' + usuariosPage + ' de ' + totalPages;
    const btnPrev = document.getElementById('btn-usuarios-prev');
    const btnNext = document.getElementById('btn-usuarios-next');
    if (btnPrev) btnPrev.disabled = usuariosPage <= 1;
    if (btnNext) btnNext.disabled = usuariosPage >= totalPages;
}

document.addEventListener('DOMContentLoaded', function() {
    const btnNuevoUsuario = document.getElementById('btn-nuevo-usuario');
    const modalUsuario = document.getElementById('modal-usuario');
    const formUsuario = document.getElementById('form-usuario');
    const btnCancelarUsuario = document.getElementById('btn-cancelar-usuario');

    btnNuevoUsuario.addEventListener('click', () => abrirModalUsuario());
    btnCancelarUsuario.addEventListener('click', () => modalUsuario.style.display = 'none');
    formUsuario.addEventListener('submit', guardarUsuario);

    const quickAddUserBtn = document.getElementById('quick-add-user-btn');
    if (quickAddUserBtn) {
        quickAddUserBtn.addEventListener('click', () => {
            mostrarPanel('panel-usuarios');
            cargarUsuarios();
            abrirModalUsuario();
        });
    }

    const btnUsuariosPrev = document.getElementById('btn-usuarios-prev');
    const btnUsuariosNext = document.getElementById('btn-usuarios-next');
    if (btnUsuariosPrev) {
        btnUsuariosPrev.addEventListener('click', function() {
            if (usuariosPage > 1) { usuariosPage--; renderTablaUsuarios(); }
        });
    }
    if (btnUsuariosNext) {
        btnUsuariosNext.addEventListener('click', function() {
            const total = (Array.isArray(usuariosActuales) ? usuariosActuales.length : 0);
            const totalPages = Math.max(1, Math.ceil(total / PANEL_PAGE_SIZE));
            if (usuariosPage < totalPages) { usuariosPage++; renderTablaUsuarios(); }
        });
    }

    const navUsuariosBtn = document.getElementById('nav-usuarios-btn');
    navUsuariosBtn.addEventListener('click', (e) => {
        e.preventDefault();
        mostrarPanel('panel-usuarios');
        cargarUsuarios();
    });
});
