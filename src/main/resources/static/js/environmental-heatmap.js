const heatmapElement = document.getElementById('reportHeatmap');
const heatmapFilter = document.getElementById('heatmapCategory');
const heatmapCount = document.getElementById('heatmapCount');
const heatmapMap = L.map(heatmapElement).setView([-9.6658, -35.7353], 12);
let heatLayer;
let heatPoints = [];

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '© OpenStreetMap contributors'
}).addTo(heatmapMap);

function renderHeatmap() {
    if (heatLayer) heatLayer.remove();
    const category = heatmapFilter.value;
    const visiblePoints = heatPoints
        .filter(point => category === 'ALL' || point.category === category)
        .map(point => [point.latitude, point.longitude, .72]);
    heatLayer = L.heatLayer(visiblePoints, {
        radius: 32,
        blur: 24,
        maxZoom: 16,
        max: 1,
        gradient: { .25: '#65a8a0', .5: '#e5c15e', .78: '#e87c4a', 1: '#b84036' }
    }).addTo(heatmapMap);
    heatmapCount.textContent = `${visiblePoints.length} ${visiblePoints.length === 1 ? 'ocorrência' : 'ocorrências'}`;
}

heatmapFilter.addEventListener('change', renderHeatmap);

fetch('/api/environmental-reports/heatmap')
    .then(response => {
        if (!response.ok) throw new Error('Não foi possível carregar as ocorrências.');
        return response.json();
    })
    .then(points => {
        heatPoints = points;
        renderHeatmap();
    })
    .catch(() => {
        heatmapCount.textContent = 'Mapa indisponível';
    });