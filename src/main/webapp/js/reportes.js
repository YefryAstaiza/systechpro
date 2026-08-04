// reportes.js - Gráficos de reportes y exportación de inventario (CSV/XLSX)
let chartDispositivos = null;
let chartMantenimientos = null;

function cargarReportes() {
    fetch(apiBase + '/reportes/resumen')
        .then(res => res.json())
        .then(data => {
            if(data.error) {
                console.error('Error cargando reportes:', data.error);
                return;
            }
            renderChartDispositivos(data.dispositivos);
            renderChartMantenimientos(data.mantenimientos);
        })
        .catch(error => console.error('Error cargando reportes:', error));
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
                alert('No hay dispositivos para exportar');
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
        alert('La librería de Excel (SheetJS) no se ha cargado. Por favor, verifica tu conexión a internet o recarga la página.');
        return;
    }

    fetch(apiBase + '/dispositivos')
        .then(res => res.json())
        .then(dispositivos => {
            if (!Array.isArray(dispositivos) || dispositivos.length === 0) {
                alert('No hay dispositivos para exportar');
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
            alert('Error al exportar el inventario');
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
