const reportMap = L.map('reportMap').setView([-9.6658, -35.7353], 12);
const reportMarkers = L.layerGroup().addTo(reportMap);
let selectedReportMarker;

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '© OpenStreetMap contributors'
}).addTo(reportMap);

function markReportLocation(latitude, longitude, zoom = 16) {
    const position = [latitude, longitude];
    if (selectedReportMarker) selectedReportMarker.remove();
    selectedReportMarker = L.marker(position).addTo(reportMap);
    document.getElementById('reportLatitude').value = latitude.toFixed(7);
    document.getElementById('reportLongitude').value = longitude.toFixed(7);
    document.getElementById('reportMapHint').innerHTML = '<i class="bi bi-check-circle"></i> Localização marcada. Você pode arrastar o marcador para ajustar.';
    selectedReportMarker.dragging.enable();
    selectedReportMarker.on('dragend', event => {
        const point = event.target.getLatLng();
        document.getElementById('reportLatitude').value = point.lat.toFixed(7);
        document.getElementById('reportLongitude').value = point.lng.toFixed(7);
    });
    if (zoom) reportMap.flyTo(position, zoom, { duration: .5 });
}

reportMap.on('click', event => markReportLocation(event.latlng.lat, event.latlng.lng, 0));

document.getElementById('useReportLocation').addEventListener('click', () => {
    if (!navigator.geolocation) {
        document.getElementById('reportMapHint').textContent = 'Seu navegador não oferece geolocalização. Marque o ponto no mapa.';
        return;
    }
    navigator.geolocation.getCurrentPosition(
        position => markReportLocation(position.coords.latitude, position.coords.longitude),
        () => { document.getElementById('reportMapHint').textContent = 'Não foi possível obter sua localização. Marque o ponto no mapa.'; },
        { enableHighAccuracy: true, timeout: 10000 }
    );
});

document.querySelectorAll('.tracked-report[data-lat][data-lng]').forEach(report => {
    const latitude = Number(report.dataset.lat);
    const longitude = Number(report.dataset.lng);
    if (Number.isFinite(latitude) && Number.isFinite(longitude)) {
        L.circleMarker([latitude, longitude], { radius: 7, color: '#b6534a', fillColor: '#dc7768', fillOpacity: .8, weight: 2 })
            .bindPopup(report.dataset.title)
            .addTo(reportMarkers);
    }
    report.querySelector('[data-center-report]').addEventListener('click', () => {
        if (Number.isFinite(latitude) && Number.isFinite(longitude)) reportMap.flyTo([latitude, longitude], 16);
    });
});

document.querySelector('.report-form').addEventListener('submit', event => {
    if (!document.getElementById('reportLatitude').value || !document.getElementById('reportLongitude').value) {
        event.preventDefault();
        document.getElementById('reportMapHint').textContent = 'Marque a localização da ocorrência antes de enviar.';
        document.getElementById('reportMap').scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
});