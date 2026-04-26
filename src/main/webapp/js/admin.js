// admin.js - Lógica para el panel de administración

const apiBase = window.location.origin + '/systechpro/api';

document.addEventListener('DOMContentLoaded', function() {
    
    // Verificar sesión (Opcional, pero recomendado para obtener nombre del admin)
    fetch(`${apiBase}/auth/sesion`)
        .then(res => res.json())
        .then(data => {
            if (data.authenticated && data.usuario) {
                document.getElementById('admin-user-name').textContent = data.usuario.nombre;
            } else {
                window.location.href = 'index.html';
            }
        });

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
    const navUsuariosBtn = document.getElementById('nav-usuarios-btn');
    const panelDashboard = document.getElementById('panel-dashboard');
    const panelUsuarios = document.getElementById('panel-usuarios');

    function ocultarPaneles() {
        panelDashboard.style.display = 'none';
        panelUsuarios.style.display = 'none';
        document.querySelectorAll('.sidebar-nav .nav-link').forEach(link => link.classList.remove('active'));
    }

    navInicioBtn.addEventListener('click', (e) => {
        e.preventDefault();
        ocultarPaneles();
        panelDashboard.style.display = 'block';
        navInicioBtn.classList.add('active');
    });

    navUsuariosBtn.addEventListener('click', (e) => {
        e.preventDefault();
        ocultarPaneles();
        panelUsuarios.style.display = 'block';
        navUsuariosBtn.classList.add('active');
        cargarUsuarios();
    });

    document.getElementById('quick-add-user-btn').addEventListener('click', () => {
        ocultarPaneles();
        panelUsuarios.style.display = 'block';
        navUsuariosBtn.classList.add('active');
        cargarUsuarios();
        abrirModalUsuario();
    });

    // ---------------------------------------------
    // LOGICA CRUD USUARIOS
    // ---------------------------------------------
    const btnNuevoUsuario = document.getElementById('btn-nuevo-usuario');
    const modalUsuario = document.getElementById('modal-usuario');
    const formUsuario = document.getElementById('form-usuario');
    const btnCancelarUsuario = document.getElementById('btn-cancelar-usuario');
    const tablaUsuariosBody = document.getElementById('tabla-usuarios-body');

    btnNuevoUsuario.addEventListener('click', abrirModalUsuario);
    btnCancelarUsuario.addEventListener('click', () => modalUsuario.style.display = 'none');

    function abrirModalUsuario(usuario = null) {
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
                if(Array.isArray(usuarios)){
                    tablaUsuariosBody.innerHTML = '';
                    usuarios.forEach(u => {
                        const tr = document.createElement('tr');
                        tr.innerHTML = `
                            <td>${u.nombre}</td>
                            <td>${u.correo}</td>
                            <td><span class="badge" style="background:#2c3e50; color:white; padding:4px 8px; border-radius:12px; font-size:12px;">${u.rol}</span></td>
                            <td>
                                <button class="btn-editar" data-id="${u.idUsuario}" style="background:#f39c12; color:white; border:none; padding:5px 10px; cursor:pointer; border-radius:4px; margin-right:5px;">Editar</button>
                                <button class="btn-eliminar" data-id="${u.idUsuario}" style="background:#e74c3c; color:white; border:none; padding:5px 10px; cursor:pointer; border-radius:4px;">Eliminar</button>
                            </td>
                        `;
                        tablaUsuariosBody.appendChild(tr);
                    });

                    document.querySelectorAll('.btn-editar').forEach(btn => {
                        btn.addEventListener('click', (e) => {
                            const id = e.target.getAttribute('data-id');
                            const usuario = usuarios.find(user => user.idUsuario == id);
                            if(usuario) abrirModalUsuario(usuario);
                        });
                    });

                    document.querySelectorAll('.btn-eliminar').forEach(btn => {
                        btn.addEventListener('click', (e) => {
                            const id = e.target.getAttribute('data-id');
                            eliminarUsuario(id);
                        });
                    });
                }
            })
            .catch(error => console.error('Error cargando usuarios:', error));
    }

    formUsuario.addEventListener('submit', function(e) {
        e.preventDefault();
        const id = document.getElementById('usuario-id').value;
        const password = document.getElementById('usuario-password').value;
        
        const payload = {
            nombre: document.getElementById('usuario-nombre').value,
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
                alert(res.body.error || 'Ocurrió un error al guardar');
            }
        })
        .catch(err => {
            alert('Error de conexión con el servidor');
        });
    });

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
});
