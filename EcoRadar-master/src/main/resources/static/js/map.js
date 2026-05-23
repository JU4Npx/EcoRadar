let map;
let marcacaoAtual;
let debounceTimer;

// Inicializa o mapa na posição atual do usuário
navigator.geolocation.getCurrentPosition((position) => {
    let { latitude, longitude } = position.coords;

    map = L.map('map').setView([latitude, longitude], 17);

    // TileLayer do OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap contributors'
    }).addTo(map);

    // Marcador da localização atual
    L.marker([latitude, longitude]).addTo(map)
        .bindPopup("Minha localização")
        .openPopup();
});

const searchInput = document.getElementById('searchInput');
const listaPesquisas = document.getElementById('listaPesquisas');

// Função para buscar sugestões via Nominatim
async function buscarSugestoes(query) {
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}`;
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`Erro HTTP: ${response.status}`);
        }
        const results = await response.json();
        console.log(results);

        listaPesquisas.innerHTML = '';
        results.forEach(r => {
            const item = document.createElement('li');
            item.textContent = r.display_name;
            item.classList.add('list-group-item', 'list-group-item-action');

            item.addEventListener('click', () => {
                if (marcacaoAtual) {
                    map.removeLayer(marcacaoAtual);
                }
                marcacaoAtual = L.marker([parseFloat(r.lat), parseFloat(r.lon)]).addTo(map)
                    .bindPopup(r.display_name)
                    .openPopup();
                map.setView([parseFloat(r.lat), parseFloat(r.lon)], 14);

                listaPesquisas.innerHTML = '';
                searchInput.value = r.display_name;
            });

            listaPesquisas.appendChild(item);
        });
    } catch (error) {
        console.error('Erro ao buscar sugestões:', error);
    }
}

// Autocomplete com debounce
searchInput.addEventListener('input', () => {
    clearTimeout(debounceTimer);
    const query = searchInput.value.trim();
    if (!query) {
        listaPesquisas.innerHTML = '';
        return;
    }
    debounceTimer = setTimeout(() => buscarSugestoes(query), 400);
});

// Função de busca ao clicar no botão
document.getElementById('searchBtn').addEventListener('click', async () => {
    const query = searchInput.value.trim();
    if (!query || !map) return;

    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}`;
    try {
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`Erro HTTP: ${response.status}`);
        }
        const results = await response.json();

        if (results.length > 0) {
            const { lat, lon, display_name } = results[0];

            if (marcacaoAtual) {
                map.removeLayer(marcacaoAtual);
            }

            marcacaoAtual = L.marker([parseFloat(lat), parseFloat(lon)]).addTo(map)
                .bindPopup(display_name)
                .openPopup();

            map.setView([parseFloat(lat), parseFloat(lon)], 14);
        } else {
            alert('Nenhum local encontrado');
        }
    } catch (error) {
        console.error('Erro na busca:', error);
    }
});

// Enter para confirmar pesquisa
searchInput.addEventListener('keydown', function (e) {
    if (e.key === 'Enter') {
        const primeiroItem = listaPesquisas.querySelector('li');
        if (primeiroItem) {
            primeiroItem.click();
        } else {
            document.getElementById('searchBtn').click();
        }
    }
});
