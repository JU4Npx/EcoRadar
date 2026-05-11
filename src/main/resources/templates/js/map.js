const map = L.map('map').setView([ -9.66625, -35.7351], 14);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors'
}).addTo(map);

let marcacaoAtual = null;

document.getElementById('searchBtn').addEventListener('click', async () => {
    const query = document.getElementById('searchInput').value.trim();
    if (!query) return;

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
        alert('Nenhum local encontrado');
    }
});
