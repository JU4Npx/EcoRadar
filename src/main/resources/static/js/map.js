let map;
let marcacaoAtual;
let debounceTimer;

const searchInput = document.getElementById('searchInput');
const listaPesquisas = document.getElementById('listaPesquisas');

/*
|--------------------------------------------------------------------------
| INICIALIZAÇÃO DO MAPA
|--------------------------------------------------------------------------
*/

navigator.geolocation.getCurrentPosition(

    // SUCESSO
    (position) => {

        const { latitude, longitude, accuracy } = position.coords;

        // Cria mapa
        map = L.map('map').setView([latitude, longitude], 18);

        // Camada OpenStreetMap
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);

        // Marcador da localização atual
        L.marker([latitude, longitude])
            .addTo(map)
            .bindPopup("Minha localização")
            .openPopup();

        // Círculo de precisão
        L.circle([latitude, longitude], {
            radius: accuracy
        }).addTo(map);

        console.log("Latitude:", latitude);
        console.log("Longitude:", longitude);
        console.log("Precisão:", accuracy);

    },

    // ERRO
    (error) => {

        console.error("Erro ao obter localização:", error);

        alert("Não foi possível obter sua localização.");

        // Local padrão (Maceió)
        map = L.map('map').setView([-9.6658, -35.7353], 13);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);
    },

    // CONFIGURAÇÕES GPS
    {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
    }
);

/*
|--------------------------------------------------------------------------
| FUNÇÃO DE BUSCA DE SUGESTÕES
|--------------------------------------------------------------------------
*/

async function buscarSugestoes(query) {

    const url =
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}`;

    try {

        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`Erro HTTP: ${response.status}`);
        }

        const results = await response.json();

        listaPesquisas.innerHTML = '';

        results.forEach(r => {

            const item = document.createElement('li');

            item.textContent = r.display_name;

            item.classList.add(
                'list-group-item',
                'list-group-item-action'
            );

            /*
            |--------------------------------------------------------------------------
            | CLICK NA SUGESTÃO
            |--------------------------------------------------------------------------
            */

            item.addEventListener('click', () => {

                const lat = parseFloat(r.lat);
                const lon = parseFloat(r.lon);

                // Remove marcador anterior
                if (marcacaoAtual) {
                    map.removeLayer(marcacaoAtual);
                }

                // Cria novo marcador
                marcacaoAtual = L.marker([lat, lon])
                    .addTo(map)
                    .bindPopup(r.display_name)
                    .openPopup();

                // Move mapa
                map.setView([lat, lon], 16);

                // Limpa lista
                listaPesquisas.innerHTML = '';

                // Atualiza input
                searchInput.value = r.display_name;
            });

            listaPesquisas.appendChild(item);
        });

    } catch (error) {

        console.error('Erro ao buscar sugestões:', error);
    }
}

/*
|--------------------------------------------------------------------------
| AUTOCOMPLETE COM DEBOUNCE
|--------------------------------------------------------------------------
*/

searchInput.addEventListener('input', () => {

    clearTimeout(debounceTimer);

    const query = searchInput.value.trim();

    if (!query) {

        listaPesquisas.innerHTML = '';

        return;
    }

    debounceTimer = setTimeout(() => {

        buscarSugestoes(query);

    }, 400);
});

/*
|--------------------------------------------------------------------------
| BOTÃO DE PESQUISA
|--------------------------------------------------------------------------
*/

document.getElementById('searchBtn')
    .addEventListener('click', async () => {

        const query = searchInput.value.trim();

        if (!query || !map) return;

        const url =
            `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}`;

        try {

            const response = await fetch(url);

            if (!response.ok) {
                throw new Error(`Erro HTTP: ${response.status}`);
            }

            const results = await response.json();

            if (results.length > 0) {

                const { lat, lon, display_name } = results[0];

                // Remove marcador antigo
                if (marcacaoAtual) {
                    map.removeLayer(marcacaoAtual);
                }

                // Novo marcador
                marcacaoAtual = L.marker([
                    parseFloat(lat),
                    parseFloat(lon)
                ])
                    .addTo(map)
                    .bindPopup(display_name)
                    .openPopup();

                // Centraliza mapa
                map.setView([
                    parseFloat(lat),
                    parseFloat(lon)
                ], 16);

            } else {

                alert('Nenhum local encontrado');
            }

        } catch (error) {

            console.error('Erro na busca:', error);
        }
    });

/*
|--------------------------------------------------------------------------
| ENTER PARA PESQUISAR
|--------------------------------------------------------------------------
*/

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