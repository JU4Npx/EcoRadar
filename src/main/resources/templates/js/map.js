let map;
let marcacaoAtual;

navigator.geolocation.getCurrentPosition((position) => {
    let { latitude, longitude } = position.coords;

    map = L.map('map').setView([latitude, longitude], 17);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap'
    }).addTo(map);

    L.marker([latitude, longitude]).addTo(map)
        .bindPopup("Minha localização")
        .openPopup();
});

const searchInput = document.getElementById('searchInput');
const listaPesquisas = document.getElementById('listaPesquisas');

// Autocomplete enquanto digita
searchInput.addEventListener('input', async () => {
    const query = searchInput.value.trim();
    if (!query) {
        listaPesquisas.innerHTML = '';
        return;
    }

    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=5`;
    try {
        const response = await fetch(url, {
            headers: {
                'User-Agent': 'EcoRadarApp/1.0 (contato@seudominio.com)'
            }
        });
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
                marcacaoAtual = L.marker([r.lat, r.lon]).addTo(map)
                    .bindPopup(r.display_name)
                    .openPopup();
                map.setView([r.lat, r.lon], 14);

                listaPesquisas.innerHTML = '';
                searchInput.value = r.display_name;
            });

            listaPesquisas.appendChild(item);
        });
    } catch (error) {
        console.error('Erro ao buscar sugestões:', error);
    }
});

// Função de busca ao clicar no botão
document.getElementById('searchBtn').addEventListener('click', async () => {
    const query = searchInput.value.trim();
    if (!query || !map) return;

    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}`;
    try {
        const response = await fetch(url);
        const results = await response.json();

        if (results.length > 0) {
            const { lat, lon, display_name } = results[0];

            if (marcacaoAtual) {
                map.removeLayer(marcacaoAtual);
            }

            marcacaoAtual = L.marker([lat, lon]).addTo(map)
                .bindPopup(display_name)
                .openPopup();

            map.setView([lat, lon], 14);
        } else {
            alert('Nenhum local encontrado');
        }
    } catch (error) {
        console.error('Erro na busca:', error);
    }
});

searchInput.addEventListener('keydown', function (e) {
    if (e.key === 'Enter') {
        const primeiroItem = listaPesquisas.querySelector('li');
        if (primeiroItem) {
            primeiroItem.click(); // simula clique na primeira sugestão
        } else {
            document.getElementById('searchBtn').click(); // fallback
        }
    }
});
