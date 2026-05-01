const API_BASE = window.location.origin + '/systechpro/api';

function tecnicoLoadDashboard() {
    fetch(`${API_BASE}/mantenimientos`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            const mantenimientos = Array.isArray(data) ? data : [];
            const dispositivosEnMantenimiento = mantenimientos.filter(m => m.estado === 'EN_PROCESO').length;
            const mantenimientosEnProceso = mantenimientos.filter(m => m.estado === 'EN_PROCESO').length;
            const mantenimientosFinalizados = mantenimientos.filter(m => m.estado === 'FINALIZADO').length;

            const cardDisp = document.getElementById('card-dispositivos-mantenimiento');
            const cardProceso = document.getElementById('card-mantenimientos-proceso');
            const cardFinalizados = document.getElementById('card-mantenimientos-finalizados');

            if (cardDisp) cardDisp.textContent = dispositivosEnMantenimiento;
            if (cardProceso) cardProceso.textContent = mantenimientosEnProceso;
            if (cardFinalizados) cardFinalizados.textContent = mantenimientosFinalizados;

            const recientes = [...mantenimientos].sort((a, b) => new Date(b.fechaInicio) - new Date(a.fechaInicio)).slice(0, 5);
            tecnicoRenderRecientes(recientes);
        })
        .catch(err => {
            console.error('Error cargando dashboard técnico:', err);
        });
}

function tecnicoRenderRecientes(items) {
    const tbody = document.querySelector('#tabla-recientes tbody');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (!items.length) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:#64748b;">No hay registros recientes.</td></tr>`;
        return;
    }

    items.forEach(item => {
        const estadoTexto = item.estado === 'EN_PROCESO' ? 'En proceso' : 'Finalizado';
        const estadoClass = item.estado === 'EN_PROCESO' ? 'status-en-proceso' : 'status-finalizado';
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${item.nombreDispositivo || 'ID: ' + item.idDispositivo}</td>
            <td>${item.tipo || 'N/A'}</td>
            <td>${item.ubicacion || 'N/A'}</td>
            <td>${formatFecha(item.fechaInicio)}</td>
            <td>${item.fechaFin ? formatFecha(item.fechaFin) : 'En curso'}</td>
            <td><span class="status-pill ${estadoClass}">${estadoTexto}</span></td>
            <td><button class="btn-secondary" onclick="verDetalle(${item.idMantenimiento})">Ver detalle</button></td>
        `;
        tbody.appendChild(tr);
    });
}

function tecnicoLoadMantenimientoPanel() {
    tecnicoLoadDispositivosDisponibles();
    tecnicoLoadMantenimientosEnProceso();
}

function tecnicoLoadDispositivosDisponibles() {
    const select = document.getElementById('mantenimiento-tecnico-dispositivo');
    if (!select) return;
    select.innerHTML = '<option value="">Cargando dispositivos...</option>';

    fetch(`${API_BASE}/dispositivos`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            const disponibles = Array.isArray(data) ? data.filter(d => d.estado !== 'MANTENIMIENTO') : [];
            if (!disponibles.length) {
                select.innerHTML = '<option value="">No hay dispositivos disponibles</option>';
                return;
            }
            select.innerHTML = '<option value="">Selecciona un dispositivo</option>';
            disponibles.forEach(d => {
                const option = document.createElement('option');
                option.value = d.idDispositivo;
                option.textContent = `${d.nombre} (${d.tipo}) - ${d.estado}`;
                select.appendChild(option);
            });
        })
        .catch(err => {
            console.error('Error cargando dispositivos para técnico:', err);
            select.innerHTML = '<option value="">Error cargando dispositivos</option>';
        });
}

function tecnicoLoadMantenimientosEnProceso() {
    fetch(`${API_BASE}/mantenimientos`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            const items = Array.isArray(data) ? data.filter(m => m.estado === 'EN_PROCESO') : [];
            tecnicoRenderMantenimientoProceso(items);
        })
        .catch(err => {
            console.error('Error cargando mantenimientos en proceso:', err);
        });
}

function tecnicoRenderMantenimientoProceso(items) {
    const tbody = document.querySelector('#tabla-proceso tbody');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (!items.length) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:#64748b;">No hay mantenimientos en proceso.</td></tr>`;
        return;
    }

    items.forEach(item => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${item.nombreDispositivo || 'ID: ' + item.idDispositivo}</td>
            <td>${item.tipo || 'N/A'}</td>
            <td>${item.ubicacion || 'N/A'}</td>
            <td>${formatFecha(item.fechaInicio)}</td>
            <td>${item.fechaFin ? formatFecha(item.fechaFin) : 'En curso'}</td>
            <td><span class="status-pill status-en-proceso">En proceso</span></td>
            <td>
                <button class="btn-secondary" onclick="verDetalle(${item.idMantenimiento})">Ver detalle</button>
                <button class="btn-primary" onclick="finalizarMantenimiento(${item.idMantenimiento})" style="background:#10b981; margin-left:8px;">Finalizar</button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function tecnicoLoadHistorial() {
    fetch(`${API_BASE}/mantenimientos`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            const items = Array.isArray(data) ? data.filter(m => m.estado === 'FINALIZADO') : [];
            tecnicoRenderHistorial(items);
        })
        .catch(err => {
            console.error('Error cargando historial técnico:', err);
        });
}

function tecnicoRenderHistorial(items) {
    const tbody = document.querySelector('#tabla-historial tbody');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (!items.length) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:#64748b;">No hay historial disponible.</td></tr>`;
        return;
    }

    items.forEach(item => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${item.nombreDispositivo || 'ID: ' + item.idDispositivo}</td>
            <td>${item.tipo || 'N/A'}</td>
            <td>${item.ubicacion || 'N/A'}</td>
            <td>${formatFecha(item.fechaInicio)}</td>
            <td>${item.fechaFin ? formatFecha(item.fechaFin) : 'N/A'}</td>
            <td><span class="status-pill status-finalizado">Finalizado</span></td>
            <td><button class="btn-secondary" onclick="verDetalle(${item.idMantenimiento})">Ver detalle</button></td>
        `;
        tbody.appendChild(tr);
    });
}

function registrarMantenimiento() {
    const idDispositivo = Number(document.getElementById('mantenimiento-tecnico-dispositivo').value);
    const tipo = document.getElementById('mantenimiento-tecnico-tipo').value;
    const descripcion = document.getElementById('mantenimiento-tecnico-descripcion').value.trim();

    if (!idDispositivo || !tipo || !descripcion) {
        alert('Completa todos los campos antes de guardar.');
        return;
    }

    const ahora = new Date();
    const fechaInicio = `${ahora.getFullYear()}-${String(ahora.getMonth() + 1).padStart(2, '0')}-${String(ahora.getDate()).padStart(2, '0')} ${String(ahora.getHours()).padStart(2, '0')}:${String(ahora.getMinutes()).padStart(2, '0')}:${String(ahora.getSeconds()).padStart(2, '0')}`;

    const data = {
        idDispositivo: idDispositivo,
        fechaInicio: fechaInicio,
        tipo: tipo,
        descripcion: descripcion,
        estado: 'EN_PROCESO'
    };

    fetch(`${API_BASE}/mantenimientos`, {
        method: 'POST',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(res => res.json())
    .then(response => {
        if (response.idMantenimiento || response.success) {
            alert('Mantenimiento registrado correctamente.');
            document.getElementById('form-mantenimiento').reset();
            tecnicoLoadDashboard();
            tecnicoLoadMantenimientosEnProceso();
            showSection('panel-inicio');
        } else {
            alert(response.error || 'Error al registrar mantenimiento.');
        }
    })
    .catch(err => {
        console.error('Error registrando mantenimiento:', err);
        alert('Error de conexión al registrar mantenimiento.');
    });
}

function finalizarMantenimiento(id) {
    if (!confirm('¿Estás seguro de finalizar este mantenimiento?')) {
        return;
    }

    const ahora = new Date();
    const fechaFin = `${ahora.getFullYear()}-${String(ahora.getMonth() + 1).padStart(2, '0')}-${String(ahora.getDate()).padStart(2, '0')} ${String(ahora.getHours()).padStart(2, '0')}:${String(ahora.getMinutes()).padStart(2, '0')}:${String(ahora.getSeconds()).padStart(2, '0')}`;

    fetch(`${API_BASE}/mantenimientos/${id}`, {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ estado: 'FINALIZADO', fechaFin: fechaFin })
    })
    .then(res => res.json())
    .then(response => {
        if (response.success || response.idMantenimiento) {
            alert('Mantenimiento finalizado correctamente.');
            tecnicoLoadDashboard();
            tecnicoLoadMantenimientosEnProceso();
            tecnicoLoadHistorial();
            showSection('panel-inicio');
        } else {
            alert(response.error || 'Error al finalizar mantenimiento.');
        }
    })
    .catch(err => {
        console.error('Error finalizando mantenimiento:', err);
        alert('Error de conexión al finalizar mantenimiento.');
    });
}

function verDetalle(id) {
    fetch(`${API_BASE}/mantenimientos/${id}`, { credentials: 'include' })
        .then(res => res.json())
        .then(data => {
            if (data.error) {
                alert(data.error);
                return;
            }
            showModalDetalle(data);
        })
        .catch(err => {
            console.error('Error cargando detalle:', err);
        });
}

function showModalDetalle(data) {
    const body = document.getElementById('modal-detail-body');
    if (!body) return;

    const estadoTexto = data.estado === 'FINALIZADO' ? 'Finalizado' : 'En proceso';
    const tipoTexto = data.tipo === 'CORRECTIVO' ? 'Correctivo' : 'Preventivo';

    body.innerHTML = `
        <h3>Detalle de mantenimiento</h3>
        <dl>
            <dt>Dispositivo</dt><dd>${data.nombreDispositivo || 'ID: ' + data.idDispositivo}</dd>
            <dt>Tipo</dt><dd>${tipoTexto}</dd>
            <dt>Ubicación</dt><dd>${data.ubicacion || 'N/A'}</dd>
            <dt>Fecha de inicio</dt><dd>${formatFecha(data.fechaInicio)}</dd>
            <dt>Fecha de finalización</dt><dd>${data.fechaFin ? formatFecha(data.fechaFin) : 'En curso'}</dd>
            <dt>Estado</dt><dd>${estadoTexto}</dd>
            <dt>Descripción</dt><dd>${data.descripcion || 'Sin descripción'}</dd>
        </dl>
    `;
    const modal = document.getElementById('modal-detail');
    if (modal) modal.classList.add('active');
}

function closeModal() {
    const modal = document.getElementById('modal-detail');
    if (modal) modal.classList.remove('active');
}

function formatFecha(value) {
    if (!value) return 'N/A';
    try {
        if (value.includes(' ')) {
            const [fecha, hora] = value.split(' ');
            const partes = fecha.split('-');
            if (partes.length === 3) {
                return `${partes[2]}/${partes[1]}/${partes[0]} ${hora.substring(0, 5)}`;
            }
        }
        const date = new Date(value);
        if (isNaN(date.getTime())) return value;
        return date.toLocaleDateString('es-CO', { year: 'numeric', month: '2-digit', day: '2-digit' }) +
               ' ' + date.toLocaleTimeString('es-CO', { hour: '2-digit', minute: '2-digit' });
    } catch (e) {
        return value;
    }
}
