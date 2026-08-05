// busqueda.js - Utilidad compartida de búsqueda + paginación server-side.
// Evita repetir la lógica de debounce/paginación en cada módulo (hallazgo F13
// de la auditoría: paginación client-side reimplementada 6 veces).

function debounce(fn, ms) {
    let timer = null;
    return function(...args) {
        clearTimeout(timer);
        timer = setTimeout(() => fn.apply(this, args), ms || 300);
    };
}

/**
 * Crea un controlador de búsqueda+filtros+paginación para un módulo.
 * opciones.onCargar(estado) se invoca cada vez que hay que volver a pedir datos al backend
 * (estado = { pagina, q, filtros }); el módulo que llama arma la query string y hace el fetch.
 */
function crearBuscadorPaginado(opciones) {
    const estado = { pagina: 1, q: '', filtros: {} };
    const cargarConDebounce = debounce(() => opciones.onCargar(estado), opciones.debounceMs || 300);

    return {
        estado,
        onBuscar(valor) {
            estado.q = valor;
            estado.pagina = 1;
            cargarConDebounce();
        },
        onFiltro(nombre, valor) {
            estado.filtros[nombre] = valor;
            estado.pagina = 1;
            opciones.onCargar(estado);
        },
        irAPagina(pagina) {
            if (pagina < 1) return;
            estado.pagina = pagina;
            opciones.onCargar(estado);
        },
        cargarInicial() {
            opciones.onCargar(estado);
        }
    };
}

/**
 * Pinta "Mostrando X–Y de Z resultados" y habilita/deshabilita los botones anterior/siguiente.
 * ids: { info, prev, next }
 */
function renderInfoPaginacion(ids, pagina, tamanoPagina, total) {
    const info = document.getElementById(ids.info);
    const prev = document.getElementById(ids.prev);
    const next = document.getElementById(ids.next);
    const totalPaginas = Math.max(1, Math.ceil(total / tamanoPagina));

    if (info) {
        if (total === 0) {
            info.textContent = 'Sin resultados';
        } else {
            const inicio = (pagina - 1) * tamanoPagina + 1;
            const fin = Math.min(pagina * tamanoPagina, total);
            info.textContent = 'Mostrando ' + inicio + '–' + fin + ' de ' + total + ' resultados';
        }
    }
    if (prev) prev.disabled = pagina <= 1;
    if (next) next.disabled = pagina >= totalPaginas;
}
