// reportes.js - Gráficos de reportes y exportación de inventario (CSV/XLSX)
let chartDispositivos = null;
let chartMasPrestados = null;
let chartUsoSemana = null;

function cargarReportes() {
    fetch(apiBase + '/reportes/resumen')
        .then(res => res.json())
        .then(data => {
            if(data.error) {
                console.error('Error cargando reportes:', data.error);
                return;
            }
            renderChartDispositivos(data.dispositivos);
            renderAlertasMantenimiento(data.alertasMantenimiento || []);
            renderTiempoPromedio(data.promedioHorasPrestamo);
            renderChartMasPrestados(data.masPrestados || []);
            renderChartUsoSemana(data.usoPorDiaSemana || {});
        })
        .catch(error => console.error('Error cargando reportes:', error));
}

function renderTiempoPromedio(horas) {
    const el = document.getElementById('reporte-tiempo-promedio');
    if (!el) return;
    if (horas === null || horas === undefined) {
        el.textContent = 'Sin datos aún';
        return;
    }
    if (horas < 24) {
        el.textContent = horas.toFixed(1) + ' h';
    } else {
        el.textContent = (horas / 24).toFixed(1) + ' días';
    }
}

function renderAlertasMantenimiento(alertas) {
    const contenedor = document.getElementById('reporte-alertas-mantenimiento');
    if (!contenedor) return;

    if (!alertas.length) {
        contenedor.innerHTML = '<p style="color:#94a3b8;text-align:center;padding:20px 0;">Ningún mantenimiento lleva más de 3 días en proceso. ✓</p>';
        return;
    }

    contenedor.innerHTML = alertas.map(a => (
        '<div style="display:flex;align-items:center;justify-content:space-between;padding:10px 14px;border-bottom:1px solid #f1f5f9;">' +
            '<span style="font-weight:600;color:#1e293b;">' + escapeHtml(a.nombreDispositivo) + '</span>' +
            '<span style="background:#fdf2f2;color:#e74c3c;padding:3px 10px;border-radius:12px;font-size:12px;font-weight:600;">' + a.dias + ' días</span>' +
        '</div>'
    )).join('');
}

function renderChartMasPrestados(lista) {
    const ctx = document.getElementById('chart-mas-prestados');
    if (!ctx) return;

    if (chartMasPrestados) {
        chartMasPrestados.destroy();
    }

    if (!lista.length) {
        return;
    }

    chartMasPrestados = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: lista.map(d => d.nombreDispositivo),
            datasets: [{
                label: 'Préstamos',
                data: lista.map(d => d.cantidad),
                backgroundColor: '#3498db'
            }]
        },
        options: {
            responsive: true,
            indexAxis: 'y',
            plugins: {
                legend: { display: false }
            },
            scales: {
                x: { ticks: { precision: 0 } }
            }
        }
    });
}

function renderChartUsoSemana(dataUso) {
    const ctx = document.getElementById('chart-uso-semana');
    if (!ctx) return;

    if (chartUsoSemana) {
        chartUsoSemana.destroy();
    }

    const labels = Object.keys(dataUso);
    const values = Object.values(dataUso);

    chartUsoSemana = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Préstamos iniciados',
                data: values,
                backgroundColor: '#1cc7a5'
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: { ticks: { precision: 0 } }
            }
        }
    });
}

function renderChartDispositivos(dataDisp) {
    const ctx = document.getElementById('chart-dispositivos');
    if (!ctx) return;

    if (chartDispositivos) {
        chartDispositivos.destroy();
    }

    const labels = Object.keys(dataDisp);
    const values = Object.values(dataDisp);
    const bgColors = labels.map(l => l === 'DISPONIBLE' ? '#2ecc71' : (l === 'EN_USO' ? '#3498db' : '#e74c3c'));

    chartDispositivos = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: bgColors
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { position: 'bottom' }
            }
        }
    });
}

function renderChartMantenimientos(dataMant) {
    const ctx = document.getElementById('chart-mantenimientos');
    if (!ctx) return;

    if (chartMantenimientos) {
        chartMantenimientos.destroy();
    }

    const labels = Object.keys(dataMant);
    const values = Object.values(dataMant);
    const bgColors = labels.map(l => l === 'PREVENTIVO' ? '#f39c12' : '#8e44ad');

    chartMantenimientos = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: values,
                backgroundColor: bgColors
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { position: 'bottom' }
            }
        }
    });
}

function exportarInventarioCSV() {
    fetch(apiBase + '/dispositivos')
        .then(res => res.json())
        .then(dispositivos => {
            if (!Array.isArray(dispositivos) || dispositivos.length === 0) {
                showToast('No hay dispositivos para exportar', 'warning');
                return;
            }

            const headers = ['ID', 'Nombre', 'Tipo', 'Estado', 'Descripción', 'Fecha Creación'];
            const rows = dispositivos.map(d => [
                d.idDispositivo,
                `"${d.nombre}"`,
                d.tipo,
                d.estado,
                `"${(d.descripcion || '').replace(/"/g, '""')}"`,
                formatearFecha(d.fechaCreacion)
            ]);

            let csvContent = "data:text/csv;charset=utf-8,"
                + headers.join(",") + "\n"
                + rows.map(e => e.join(",")).join("\n");

            const encodedUri = encodeURI(csvContent);
            const link = document.createElement("a");
            link.setAttribute("href", encodedUri);
            link.setAttribute("download", `inventario_${new Date().toISOString().split('T')[0]}.csv`);
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        });
}

function exportarInventarioXLSX() {
    if (typeof XLSX === 'undefined') {
        showToast('La librería de Excel (SheetJS) no se ha cargado. Verifica tu conexión a internet o recarga la página.', 'error');
        return;
    }

    fetch(apiBase + '/dispositivos')
        .then(res => res.json())
        .then(dispositivos => {
            if (!Array.isArray(dispositivos) || dispositivos.length === 0) {
                showToast('No hay dispositivos para exportar', 'warning');
                return;
            }

            const data = dispositivos.map(d => ({
                'ID': d.idDispositivo,
                'Nombre': d.nombre,
                'Tipo': d.tipo,
                'Estado': d.estado,
                'Descripción': d.descripcion || '',
                'Fecha Creación': formatearFecha(d.fechaCreacion)
            }));

            const worksheet = XLSX.utils.json_to_sheet(data);
            const workbook = XLSX.utils.book_new();
            XLSX.utils.book_append_sheet(workbook, worksheet, "Inventario");

            XLSX.writeFile(workbook, `inventario_${new Date().toISOString().split('T')[0]}.xlsx`);
        })
        .catch(err => {
            console.error('Error al exportar XLSX:', err);
            showToast('Error al exportar el inventario', 'error');
        });
}

document.addEventListener('DOMContentLoaded', function() {
    const btnExportarCSV = document.getElementById('btn-exportar-csv');
    if (btnExportarCSV) {
        btnExportarCSV.addEventListener('click', exportarInventarioCSV);
    }

    const btnExportarXLSX = document.getElementById('btn-exportar-xlsx');
    if (btnExportarXLSX) {
        btnExportarXLSX.addEventListener('click', exportarInventarioXLSX);
    }

    const navReportesBtn = document.getElementById('nav-reportes-btn');
    if (navReportesBtn) {
        navReportesBtn.addEventListener('click', (e) => {
            e.preventDefault();
            mostrarPanel('panel-reportes');
            cargarReportes();
        });
    }
});
