/* map.js - versão com botão de favoritar que persiste no banco */

let map;
let marcacaoAtual;
let debounceTimer;
let favorites = []; // armazenar favoritos globalmente

const searchInput = document.getElementById('searchInput');
const listaPesquisas = document.getElementById('listaPesquisas');
const nearbyContainer = document.getElementById('eventList'); // pode ser null em map.html fullscreen
const eventDetails = document.getElementById('eventDetails'); // pode ser null em map.html fullscreen

// haversine
function haversineDistance(lat1, lon1, lat2, lon2) {
    const toRad = v => v * Math.PI / 180;
    const R = 6371;
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
        Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
        Math.sin(dLon/2) * Math.sin(dLon/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    return R * c;
}

function escapeHtml(text) {
    if (!text) return '';
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// Render lista de áreas próximas com botão de favoritar
function renderNearbyList(userLat, userLon, areas, favorites = []) {
    // safety: if no nearby container on the page (e.g., map.html), skip DOM list rendering
    if (!areas || areas.length === 0) {
        if (nearbyContainer) nearbyContainer.innerHTML = '<div class="nearby-empty text-muted">Nenhuma área verde cadastrada.</div>';
        return;
    }

    // normalizar set de favoritos para comparação rápida
    const favSet = new Set((favorites || []).map(f => String(f)));

    areas.forEach(a => {
        if (a.latitude != null && a.longitude != null) {
            a.distanceKm = haversineDistance(userLat, userLon, a.latitude, a.longitude);
        } else {
            a.distanceKm = Number.POSITIVE_INFINITY;
        }
    });

    areas.sort((x,y) => x.distanceKm - y.distanceKm);

    if (!nearbyContainer) {
        // nothing to render in DOM (page without sidebar). caller may still add markers.
        return;
    }

    nearbyContainer.innerHTML = '';

    // Render all areas (ordenadas por proximidade) — permite scroll no painel
    for (const a of areas) {
        const item = document.createElement('div');
        item.className = 'nearby-item';

        const infoHtml = `
            <div class="nearby-info">
                <div class="nearby-title">${escapeHtml(a.name || '—')}</div>
                <div class="nearby-sub">${escapeHtml(a.address || (a.description || '').substring(0,80))}</div>
            </div>
        `;

        const distHtml = `<div class="distance-badge">${a.distanceKm === Infinity ? '-' : (a.distanceKm < 1 ? (a.distanceKm*1000|0)+' m' : a.distanceKm.toFixed(2)+' km')}</div>`;

        const isFav = favSet.has(String(a.id));
        const btnClass = isFav ? 'btn-success active' : 'btn-outline-success';
        const iconClass = isFav ? 'bi bi-heart-fill' : 'bi bi-heart';
        const titleText = isFav ? 'Remover de favoritos' : 'Favoritar';

        const favHtml = `
            <div class="nearby-fav mt-2">
                <button class="btn btn-sm ${btnClass} favorite-btn" data-area-id="${a.id}" title="${titleText}">
                    <i class="${iconClass}"></i>
                </button>
            </div>
        `;

        item.innerHTML = `<div class="d-flex align-items-start justify-content-between">${infoHtml}${distHtml}</div>${favHtml}`;

        item.addEventListener('click', (e) => {
            if (!e.target.closest('.favorite-btn')) {
                focusOnArea(a);
            }
        });

        nearbyContainer.appendChild(item);

        const btn = item.querySelector('.favorite-btn');
        if (btn) {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                handleFavoriteClick(btn, a.id);
            });
        }
    }
}

// Lida com clique no botão favoritar
async function handleFavoriteClick(btn, areaId) {
    if (!window.LOGGED_USER) {
        window.location.href = '/login';
        return;
    }

    try {
        // Verificar status atual
        const statusResp = await fetch(`/api/favorites/status/${areaId}`);
        const statusData = await statusResp.json();

        if (statusData.isFavorite) {
            // Remover favorito
            const resp = await fetch(`/api/favorites/${areaId}`, { method: 'DELETE' });
            if (resp.ok) {
                btn.classList.remove('active');
                btn.innerHTML = '<i class="bi bi-heart"></i>';
                btn.title = 'Favoritar';
            } else {
                alert('Erro ao remover favorito');
            }
        } else {
            // Adicionar favorito
            const resp = await fetch(`/api/favorites/${areaId}`, { method: 'POST' });
            if (resp.ok) {
                btn.classList.add('active');
                btn.innerHTML = '<i class="bi bi-heart-fill"></i>';
                btn.title = 'Remover de favoritos';
            } else {
                alert('Erro ao adicionar favorito');
            }
        }
    } catch (e) {
        console.error('Erro ao processar favorito:', e);
        alert('Erro ao processar favorito');
    }
}

// focusOnArea e populateEventPanel (mantém comportamento anterior)
async function focusOnArea(area) {
    if (!map || !area) return;

    const lat = parseFloat(area.latitude);
    const lon = parseFloat(area.longitude);

    if (isNaN(lat) || isNaN(lon)) {
        alert('Localização da área não disponível.');
        return;
    }

    if (marcacaoAtual) {
        map.removeLayer(marcacaoAtual);
    }

    marcacaoAtual = L.marker([lat, lon]).addTo(map);

    const popupHtml =
        `<div style="max-width:300px; text-align:left">
            <h6 style="margin:0 0 6px 0">${escapeHtml(area.name || 'Área verde')}</h6>
            <div style="font-size:0.95rem;color:#5b6b59">${escapeHtml(area.address || area.description || '')}</div>
         </div>`;

    const popup = L.popup({
        maxWidth: 320,
        offset: L.point(0, -10),
        autoPan: true,
        autoPanPaddingTopLeft: [0, 120],
        autoPanPadding: [10, 10]
    }).setLatLng([lat, lon])
        .setContent(popupHtml)
        .openOn(map);

    map.setView([lat, lon], 16);

    // Only populate event panel if it exists (not in fullscreen map)
    if (eventDetails) {
        populateEventPanel(area.id);
    }
}

async function populateEventPanel(areaId) {
    if (!eventDetails) return;
    
    eventDetails.innerHTML = '<div>Carregando eventos...</div>';
    try {
        const resp = await fetch(`/api/green-areas/${areaId}/events`);
        if (!resp.ok) {
            eventDetails.innerHTML = '<div class="text-danger">Erro ao carregar eventos.</div>';
            return;
        }
        const events = await resp.json();
        if (!events || events.length === 0) {
            eventDetails.innerHTML = '<div class="alert alert-info mb-0">Nenhum evento em andamento ou próximo.</div>';
            return;
        }

        const dtf = new Intl.DateTimeFormat('pt-BR', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });

        const listHtml = events.map((ev, idx) => {
            const start = ev.startDate ? new Date(ev.startDate) : null;
            const end = ev.endDate ? new Date(ev.endDate) : null;
            const startStr = start ? dtf.format(start) : '-';
            const endStr = end ? dtf.format(end) : '-';

            return `
                <div class="event-card mb-3" data-ev-index="${idx}">
                    <div class="event-card-body">
                        <div class="event-card-title"><strong>${escapeHtml(ev.title)}</strong></div>
                        <div class="event-card-datetime text-muted" style="font-size:0.95rem;margin-top:6px">
                            <div>Início: ${escapeHtml(startStr)}</div>
                            <div>Fim: ${escapeHtml(endStr)}</div>
                        </div>
                        <div class="event-desc mt-2" style="display:none; font-size:0.95rem; color:#333;">
                            ${escapeHtml(ev.description || '')}
                        </div>
                        <button class="btn btn-sm btn-eco view-details-btn" data-ev="${idx}">Ver detalhes</button>
                    </div>
                </div>
            `;
        }).join('');

        eventDetails.innerHTML = listHtml;

        eventDetails.querySelectorAll('.view-details-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const container = btn.closest('.event-card');
                const desc = container.querySelector('.event-desc');
                if (desc.style.display === 'none' || !desc.style.display) {
                    desc.style.display = 'block';
                    btn.textContent = 'Ocultar detalhes';
                } else {
                    desc.style.display = 'none';
                    btn.textContent = 'Ver detalhes';
                }
            });
        });

    } catch (e) {
        console.error('Erro ao carregar eventos', e);
        if (eventDetails) {
            eventDetails.innerHTML = '<div class="text-danger">Erro ao carregar eventos.</div>';
        }
    }
}

// Inicialização do mapa
navigator.geolocation.getCurrentPosition(
    async (position) => {
        const { latitude, longitude, accuracy } = position.coords;

        map = L.map('map').setView([latitude, longitude], 15);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);

        L.marker([latitude, longitude])
            .addTo(map)
            .bindPopup("Minha localização")
            .openPopup();

        L.circle([latitude, longitude], {
            radius: accuracy
        }).addTo(map);

        try {
            const resp = await fetch('/api/green-areas');
            if (resp.ok) {
                const areas = await resp.json();
                allGreenAreas = areas; // armazenar globalmente para busca

                // Buscar favoritos do usuário para já marcar os botões
                try {
                    const favResp = await fetch('/api/favorites');
                    if (favResp.ok) {
                        favorites = await favResp.json();
                    }
                } catch (fe) {
                    console.error('Erro ao buscar favoritos', fe);
                }

                renderNearbyList(latitude, longitude, areas, favorites);

                // preparar set rápido de favoritos para ajustar popups
                const favSetMain = new Set((favorites || []).map(f => String(f)));

                areas.forEach(a => {
                    if (a.latitude != null && a.longitude != null) {
                        const marker = L.circleMarker([a.latitude, a.longitude], {
                            radius: 6,
                            color: '#198754',
                            fillColor: '#66BB6A',
                            fillOpacity: 0.9
                        }).addTo(map);
                        marker.on('click', () => focusOnArea(a));

                        // popup com botão de favoritar
                        const isFav = favSetMain.has(String(a.id));
                        const favBtnHtml = `<button class="btn btn-sm ${isFav ? 'btn-success active' : 'btn-outline-success'} popup-fav" data-area-id="${a.id}" title="${isFav ? 'Remover de favoritos' : 'Favoritar'}"><i class="${isFav ? 'bi bi-heart-fill' : 'bi bi-heart'}"></i></button>`;
                        const popupHtml = `<div style="max-width:300px; text-align:left"><h6 style="margin:0 0 6px 0">${escapeHtml(a.name || 'Área verde')}</h6><div style="font-size:0.95rem;color:#5b6b59">${escapeHtml(a.address || a.description || '')}</div><div class="mt-2">${favBtnHtml}</div></div>`;

                        marker.bindPopup(popupHtml);

                        marker.on('popupopen', function(e){
                            try {
                                const popupEl = e.popup.getElement();
                                const btn = popupEl.querySelector('.popup-fav');
                                if (btn) {
                                    btn.addEventListener('click', function(ev){
                                        ev.stopPropagation();
                                        handleFavoriteClick(btn, a.id);
                                    });
                                }
                            } catch (err) {
                                console.error('Erro ao anexar listener de favorito no popup', err);
                            }
                        });
                    }
                });
            } else {
                if (nearbyContainer) {
                    nearbyContainer.innerHTML = '<div class="nearby-empty text-danger">Erro ao carregar áreas.</div>';
                }
            }
        } catch (e) {
            console.error('Erro ao buscar áreas verdes', e);
            if (nearbyContainer) {
                nearbyContainer.innerHTML = '<div class="nearby-empty text-danger">Erro ao carregar áreas.</div>';
            }
        }

    },
    (error) => {
        console.error("Erro ao obter localização:", error);
        // não alertar automaticamente para melhor UX — deixar mapa carregando
        // alert("Não foi possível obter sua localização. Você ainda pode usar o campo de busca.");

        const defaultLat = -9.6658;
        const defaultLon = -35.7353;

        map = L.map('map').setView([defaultLat, defaultLon], 13);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '© OpenStreetMap contributors'
        }).addTo(map);

        // buscar áreas mesmo sem geolocalização para popular o mapa e a lista
        (async function(){
            try {
                const resp = await fetch('/api/green-areas');
                if (resp.ok) {
                    const areas = await resp.json();
                    allGreenAreas = areas; // armazenar globalmente para busca

                    // calcular distâncias usando ponto padrão
                    areas.forEach(a => {
                        if (a.latitude != null && a.longitude != null) {
                            a.distanceKm = haversineDistance(defaultLat, defaultLon, a.latitude, a.longitude);
                        } else {
                            a.distanceKm = Number.POSITIVE_INFINITY;
                        }
                    });

                    // ordenar por proximidade
                    areas.sort((x,y) => x.distanceKm - y.distanceKm);

                    // tentar obter favoritos (silencioso)
                    try {
                        const favResp = await fetch('/api/favorites');
                        if (favResp.ok) favorites = await favResp.json();
                    } catch (fe) { console.error('Erro ao buscar favoritos', fe); }

                    // renderizar lista e marcadores
                    renderNearbyList(defaultLat, defaultLon, areas, favorites);

                    // preparar set rápido de favoritos para ajustar popups
                    const favSetFallback = new Set((favorites || []).map(f => String(f)));

                    areas.forEach(a => {
                        if (a.latitude != null && a.longitude != null) {
                            const marker = L.circleMarker([a.latitude, a.longitude], {
                                radius: 6,
                                color: '#198754',
                                fillColor: '#66BB6A',
                                fillOpacity: 0.9
                            }).addTo(map);
                            marker.on('click', () => focusOnArea(a));

                            const isFav = favSetFallback.has(String(a.id));
                            const favBtnHtml = `<button class="btn btn-sm ${isFav ? 'btn-success active' : 'btn-outline-success'} popup-fav" data-area-id="${a.id}" title="${isFav ? 'Remover de favoritos' : 'Favoritar'}"><i class="${isFav ? 'bi bi-heart-fill' : 'bi bi-heart'}"></i></button>`;
                            const popupHtml = `<div style="max-width:300px; text-align:left"><h6 style="margin:0 0 6px 0">${escapeHtml(a.name || 'Área verde')}</h6><div style="font-size:0.95rem;color:#5b6b59">${escapeHtml(a.address || a.description || '')}</div><div class="mt-2">${favBtnHtml}</div></div>`;

                            marker.bindPopup(popupHtml);

                            marker.on('popupopen', function(e){
                                try {
                                    const popupEl = e.popup.getElement();
                                    const btn = popupEl.querySelector('.popup-fav');
                                    if (btn) {
                                        btn.addEventListener('click', function(ev){
                                            ev.stopPropagation();
                                            handleFavoriteClick(btn, a.id);
                                        });
                                    }
                                } catch (err) {
                                    console.error('Erro ao anexar listener de favorito no popup', err);
                                }
                            });
                        }
                    });
                } else {
                    if (nearbyContainer) {
                        nearbyContainer.innerHTML = '<div class="nearby-empty text-danger">Erro ao carregar áreas.</div>';
                    }
                }
            } catch (e) {
                console.error('Erro ao buscar áreas verdes', e);
                if (nearbyContainer) {
                    nearbyContainer.innerHTML = '<div class="nearby-empty text-danger">Erro ao carregar áreas.</div>';
                }
            }
        })();
    },
    {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
    }
);

// Buscar / autocomplete / enter (mantido + adição de áreas verdes)
let allGreenAreas = []; // armazenar áreas verdes globalmente para filtro

async function buscarSugestoes(query) {
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}`;

    try {
        const response = await fetch(url);

        if (!response.ok) {
            throw new Error(`Erro HTTP: ${response.status}`);
        }

        const results = await response.json();

        listaPesquisas.innerHTML = '';

        // Filtrar áreas verdes por nome/endereço
        const queryLower = query.toLowerCase();
        const matchingAreas = allGreenAreas.filter(a => 
            (a.name && a.name.toLowerCase().includes(queryLower)) ||
            (a.address && a.address.toLowerCase().includes(queryLower))
        );

        // Adicionar áreas verdes primeiro (destacadas em verde)
        matchingAreas.forEach(area => {
            const item = document.createElement('li');
            item.innerHTML = `<strong style="color: #198754;">📍 ${escapeHtml(area.name)}</strong><br><small style="color: #666;">${escapeHtml(area.address || '')}</small>`;
            item.classList.add('list-group-item', 'list-group-item-action');
            item.style.backgroundColor = '#f0fff0';
            item.style.borderLeft = '4px solid #198754';
            item.style.cursor = 'pointer';
            item.addEventListener('click', () => {
                const lat = parseFloat(area.latitude);
                const lon = parseFloat(area.longitude);

                if (marcacaoAtual) {
                    map.removeLayer(marcacaoAtual);
                }

                const isFav = new Set((favorites || []).map(f => String(f))).has(String(area.id));
                const favBtnHtml = `<button class="btn btn-sm ${isFav ? 'btn-success active' : 'btn-outline-success'} popup-fav" data-area-id="${area.id}" title="${isFav ? 'Remover de favoritos' : 'Favoritar'}"><i class="${isFav ? 'bi bi-heart-fill' : 'bi bi-heart'}"></i></button>`;
                const popupHtml = `<div style="max-width:300px; text-align:left"><h6 style="margin:0 0 6px 0">${escapeHtml(area.name || 'Área verde')}</h6><div style="font-size:0.95rem;color:#5b6b59">${escapeHtml(area.address || area.description || '')}</div><div class="mt-2">${favBtnHtml}</div></div>`;

                marcacaoAtual = L.marker([lat, lon])
                    .addTo(map)
                    .bindPopup(popupHtml)
                    .openPopup();

                // Anexar listener de favoritar ao popup
                marcacaoAtual.on('popupopen', function(e){
                    try {
                        const popupEl = e.popup.getElement();
                        const btn = popupEl.querySelector('.popup-fav');
                        if (btn) {
                            btn.addEventListener('click', function(ev){
                                ev.stopPropagation();
                                handleFavoriteClick(btn, area.id);
                            });
                        }
                    } catch (err) {
                        console.error('Erro ao anexar listener de favorito no popup', err);
                    }
                });

                map.setView([lat, lon], 16);

                listaPesquisas.innerHTML = '';
                searchInput.value = area.name;
            });

            listaPesquisas.appendChild(item);
        });

        // Adicionar separador se houver ambos
        if (matchingAreas.length > 0 && results.length > 0) {
            const separator = document.createElement('li');
            separator.classList.add('list-group-item');
            separator.style.textAlign = 'center';
            separator.style.color = '#999';
            separator.style.fontSize = '0.9rem';
            separator.textContent = '— Outros locais —';
            separator.style.pointerEvents = 'none';
            listaPesquisas.appendChild(separator);
        }

        // Adicionar resultados do OpenStreetMap
        results.forEach(r => {
            const item = document.createElement('li');
            item.textContent = r.display_name;
            item.classList.add('list-group-item','list-group-item-action');
            item.addEventListener('click', () => {
                const lat = parseFloat(r.lat);
                const lon = parseFloat(r.lon);

                if (marcacaoAtual) {
                    map.removeLayer(marcacaoAtual);
                }

                marcacaoAtual = L.marker([lat, lon])
                    .addTo(map)
                    .bindPopup(r.display_name)
                    .openPopup();

                map.setView([lat, lon], 16);

                listaPesquisas.innerHTML = '';
                searchInput.value = r.display_name;
            });

            listaPesquisas.appendChild(item);
        });

    } catch (error) {
        console.error('Erro ao buscar sugestões:', error);
    }
}

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
            marcacaoAtual = L.marker([parseFloat(lat), parseFloat(lon)])
                .addTo(map)
                .bindPopup(display_name)
                .openPopup();
            map.setView([parseFloat(lat), parseFloat(lon)], 16);
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
            primeiroItem.click();
        } else {
            document.getElementById('searchBtn').click();
        }
    }
});
