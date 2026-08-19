/* EcoRadar map experience */

const DEFAULT_POSITION = { latitude: -9.6658, longitude: -35.7353 };
const TYPE_META = {
    PRACA: { label: 'Praça', icon: 'bi-tree' },
    PRAIA: { label: 'Praia', icon: 'bi-water' },
    PARQUE: { label: 'Parque', icon: 'bi-tree-fill' },
    FEIRA_DE_ARTESANATO: { label: 'Feira', icon: 'bi-shop' },
    CENTRO_DE_CONVENCOES: { label: 'Convenções', icon: 'bi-building' },
    MUSEU: { label: 'Museu', icon: 'bi-bank' }
};

let map;
let markerLayer;
let userLocationLayer;
let temporaryMarker;
let debounceTimer;
let allGreenAreas = [];
let favorites = [];
let currentPosition = null;
let referencePosition = DEFAULT_POSITION;
let selectedType = 'ALL';
let maxDistance = Infinity;
let selectedAreaId = null;
const areaMarkers = new Map();

const searchInput = document.getElementById('searchInput');
const searchButton = document.getElementById('searchBtn');
const searchResults = document.getElementById('listaPesquisas');
const nearbyContainer = document.getElementById('eventList');
const nearbySubtitle = document.getElementById('nearbySubtitle');
const areaDetails = document.getElementById('areaDetails');
const detailDrawer = document.getElementById('areaDetailDrawer');
const closeAreaDetails = document.getElementById('closeAreaDetails');
const typeFilterGroup = document.getElementById('typeFilterGroup');
const distanceFilter = document.getElementById('distanceFilter');
const resultCount = document.getElementById('mapResultCount');
const locationNotice = document.getElementById('locationNotice');
const retryLocationButton = document.getElementById('retryLocationBtn');

function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function typeMeta(area) {
    return TYPE_META[area?.type] || {
        label: area?.typeLabel || 'Área verde',
        icon: 'bi-geo-alt-fill'
    };
}

function haversineDistance(lat1, lon1, lat2, lon2) {
    const toRad = value => value * Math.PI / 180;
    const earthRadius = 6371;
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const value = Math.sin(dLat / 2) ** 2
        + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;
    return earthRadius * (2 * Math.atan2(Math.sqrt(value), Math.sqrt(1 - value)));
}

function formatDistance(distance) {
    if (!Number.isFinite(distance)) return 'Sem distância';
    if (distance < 1) return `${Math.max(1, Math.round(distance * 1000))} m`;
    return `${distance.toFixed(distance < 10 ? 1 : 0).replace('.', ',')} km`;
}

function showToast(message, tone = 'success') {
    let region = document.querySelector('.eco-toast-region');
    if (!region) {
        region = document.createElement('div');
        region.className = 'eco-toast-region';
        region.setAttribute('aria-live', 'polite');
        document.body.appendChild(region);
    }

    const toast = document.createElement('div');
    toast.className = `eco-toast eco-toast-${tone}`;
    toast.innerHTML = `<i class="bi ${tone === 'error' ? 'bi-exclamation-circle' : 'bi-check-circle'}"></i><span>${escapeHtml(message)}</span>`;
    region.appendChild(toast);
    requestAnimationFrame(() => toast.classList.add('is-visible'));
    window.setTimeout(() => {
        toast.classList.remove('is-visible');
        window.setTimeout(() => toast.remove(), 220);
    }, 3200);
}

function initializeMap() {
    map = L.map('map', { zoomControl: true }).setView(
        [DEFAULT_POSITION.latitude, DEFAULT_POSITION.longitude],
        13
    );

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap contributors'
    }).addTo(map);

    markerLayer = L.layerGroup().addTo(map);
    userLocationLayer = L.layerGroup().addTo(map);
}

function setLoadingState() {
    if (nearbyContainer) {
        nearbyContainer.innerHTML = Array.from({ length: 4 }, () => '<div class="map-skeleton"></div>').join('');
    }
}

function buildTypeFilters() {
    if (!typeFilterGroup) return;

    const uniqueTypes = [...new Set(allGreenAreas.map(area => area.type).filter(Boolean))];
    const buttons = [{ type: 'ALL', label: 'Todos', icon: 'bi-grid' }]
        .concat(uniqueTypes.map(type => ({ type, ...typeMeta({ type }) })));

    typeFilterGroup.innerHTML = buttons.map(item => `
        <button type="button" class="map-filter-chip ${item.type === selectedType ? 'active' : ''}"
                data-filter-type="${escapeHtml(item.type)}" aria-pressed="${item.type === selectedType}">
            <i class="bi ${escapeHtml(item.icon)}"></i>${escapeHtml(item.label)}
        </button>
    `).join('');
}

function calculateDistances() {
    allGreenAreas.forEach(area => {
        const lat = Number(area.latitude);
        const lon = Number(area.longitude);
        area.distanceKm = Number.isFinite(lat) && Number.isFinite(lon)
            ? haversineDistance(referencePosition.latitude, referencePosition.longitude, lat, lon)
            : Infinity;
    });
}

function filteredAreas() {
    return allGreenAreas
        .filter(area => selectedType === 'ALL' || area.type === selectedType)
        .filter(area => !Number.isFinite(maxDistance) || area.distanceKm <= maxDistance)
        .sort((first, second) => first.distanceKm - second.distanceKm);
}

function markerIcon(area) {
    const meta = typeMeta(area);
    const typeClass = String(area.type || 'area').toLowerCase().replace(/_/g, '-');
    return L.divIcon({
        className: 'eco-marker-shell',
        html: `<span class="eco-map-marker eco-marker-${escapeHtml(typeClass)}"><i class="bi ${escapeHtml(meta.icon)}"></i></span>`,
        iconSize: [38, 44],
        iconAnchor: [19, 42],
        popupAnchor: [0, -38]
    });
}

function favoriteButton(areaId, extraClass = '') {
    const active = favorites.map(String).includes(String(areaId));
    return `
        <button type="button" class="btn btn-sm ${active ? 'btn-success active' : 'btn-outline-success'} ${extraClass}"
                data-favorite-area="${areaId}" aria-label="${active ? 'Remover dos favoritos' : 'Adicionar aos favoritos'}"
                title="${active ? 'Remover dos favoritos' : 'Adicionar aos favoritos'}">
            <i class="bi ${active ? 'bi-heart-fill' : 'bi-heart'}"></i>
        </button>
    `;
}

function popupContent(area) {
    const meta = typeMeta(area);
    return `
        <div class="area-popup">
            <span class="area-type-badge"><i class="bi ${escapeHtml(meta.icon)}"></i>${escapeHtml(meta.label)}</span>
            <h3>${escapeHtml(area.name || 'Área verde')}</h3>
            <p><i class="bi bi-geo-alt"></i>${escapeHtml(area.address || 'Endereço não informado')}</p>
            <div class="area-popup-actions">
                <button type="button" class="btn btn-sm btn-primary" data-show-area="${area.id}">Ver detalhes</button>
                ${favoriteButton(area.id, 'popup-fav')}
            </div>
        </div>
    `;
}

function renderMarkers(areas) {
    markerLayer.clearLayers();
    areaMarkers.clear();

    areas.forEach(area => {
        const lat = Number(area.latitude);
        const lon = Number(area.longitude);
        if (!Number.isFinite(lat) || !Number.isFinite(lon)) return;

        const marker = L.marker([lat, lon], { icon: markerIcon(area), title: area.name || 'Área verde' })
            .bindPopup(popupContent(area), { maxWidth: 310 })
            .on('click', () => selectArea(area, false))
            .addTo(markerLayer);
        areaMarkers.set(String(area.id), marker);
    });
}

function renderNearbyList(areas) {
    if (!nearbyContainer) return;

    if (!areas.length) {
        nearbyContainer.innerHTML = `
            <div class="panel-empty compact-empty">
                <i class="bi bi-search"></i>
                <p>Nenhum local combina com estes filtros.</p>
                <button type="button" class="btn btn-sm btn-outline-success" data-clear-filters>Limpar filtros</button>
            </div>`;
        return;
    }

    nearbyContainer.innerHTML = areas.map(area => {
        const meta = typeMeta(area);
        return `
            <article class="nearby-item ${String(area.id) === String(selectedAreaId) ? 'active' : ''}" data-nearby-area="${area.id}" tabindex="0">
                <div class="nearby-item-top">
                    <span class="nearby-type-icon"><i class="bi ${escapeHtml(meta.icon)}"></i></span>
                    <div class="nearby-info">
                        <span class="nearby-kind">${escapeHtml(meta.label)}</span>
                        <div class="nearby-title">${escapeHtml(area.name || 'Área verde')}</div>
                        <div class="nearby-sub">${escapeHtml(area.address || area.description || 'Sem endereço informado')}</div>
                    </div>
                    <span class="distance-badge">${formatDistance(area.distanceKm)}</span>
                </div>
                <div class="nearby-item-actions">
                    <span>Ver detalhes <i class="bi bi-arrow-right"></i></span>
                    ${favoriteButton(area.id, 'favorite-btn')}
                </div>
            </article>`;
    }).join('');

    nearbyContainer.querySelectorAll('[data-nearby-area]').forEach(item => {
        const openArea = () => {
            const area = allGreenAreas.find(candidate => String(candidate.id) === item.dataset.nearbyArea);
            if (area) selectArea(area);
        };
        item.addEventListener('click', event => {
            if (!event.target.closest('[data-favorite-area]')) openArea();
        });
        item.addEventListener('keydown', event => {
            if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                openArea();
            }
        });
    });
}

function applyFilters() {
    calculateDistances();
    const areas = filteredAreas();
    renderMarkers(areas);
    renderNearbyList(areas);

    if (resultCount) {
        resultCount.textContent = `${areas.length} ${areas.length === 1 ? 'local' : 'locais'}`;
    }
}

function directionsUrl(area) {
    return `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(`${area.latitude},${area.longitude}`)}`;
}

function selectArea(area, moveMap = true) {
    if (!area) return;
    const lat = Number(area.latitude);
    const lon = Number(area.longitude);
    if (!Number.isFinite(lat) || !Number.isFinite(lon)) {
        showToast('A localização deste espaço ainda não está disponível.', 'error');
        return;
    }

    selectedAreaId = area.id;
    if (moveMap) map.flyTo([lat, lon], 16, { duration: .7 });
    const marker = areaMarkers.get(String(area.id));
    if (marker) marker.openPopup();
    renderNearbyList(filteredAreas());
    renderAreaDetails(area);
}

function renderAreaDetails(area) {
    if (!areaDetails) return;
    const meta = typeMeta(area);
    const isFavorite = favorites.map(String).includes(String(area.id));

    areaDetails.innerHTML = `
        <div class="area-detail-hero ${area.primaryPhotoUrl ? 'has-photo' : ''}">
            ${area.primaryPhotoUrl ? `<img src="${escapeHtml(area.primaryPhotoUrl)}" alt="Foto de ${escapeHtml(area.name || 'área verde')}" loading="lazy">` : ''}
            <span class="area-detail-icon"><i class="bi ${escapeHtml(meta.icon)}"></i></span>
            <span class="area-type-badge">${escapeHtml(meta.label)}</span>
        </div>
        <div class="area-detail-copy">
            <h2>${escapeHtml(area.name || 'Área verde')}</h2>
            <p class="area-detail-address"><i class="bi bi-geo-alt"></i>${escapeHtml(area.address || 'Endereço não informado')}</p>
            <p class="area-detail-description">${escapeHtml(area.description || 'Este espaço ainda não possui uma descrição detalhada.')}</p>
            ${area.openingHours ? `<div class="area-detail-hours"><i class="bi bi-clock"></i><span><small>Funcionamento</small><strong>${escapeHtml(area.openingHours)}</strong></span></div>` : ''}
            ${Array.isArray(area.amenities) && area.amenities.length ? `<div class="area-detail-amenities">${area.amenities.slice(0, 4).map(amenity => `<span><i class="bi ${escapeHtml(amenity.icon)}"></i>${escapeHtml(amenity.label)}</span>`).join('')}</div>` : ''}
            <div class="area-detail-actions">
                <a class="btn btn-primary btn-sm" href="${directionsUrl(area)}" target="_blank" rel="noopener noreferrer">
                    <i class="bi bi-sign-turn-right"></i>Como chegar
                </a>
                <button type="button" class="btn btn-outline-success btn-sm" data-share-area="${area.id}">
                    <i class="bi bi-share"></i>Compartilhar
                </button>
                <button type="button" class="btn btn-outline-success btn-sm detail-favorite ${isFavorite ? 'active' : ''}" data-favorite-area="${area.id}">
                    <i class="bi ${isFavorite ? 'bi-heart-fill' : 'bi-heart'}"></i><span>${isFavorite ? 'Salvo' : 'Salvar'}</span>
                </button>
                <a class="btn btn-quiet btn-sm area-full-page-link" href="/areas-verdes/${area.id}"><i class="bi bi-arrow-up-right-square"></i>Página completa</a>
            </div>
            <section class="area-events-section">
                <div class="area-events-heading"><span><i class="bi bi-calendar-event"></i>Agenda verde</span><small>Próximos eventos</small></div>
                <div id="eventDetails" class="event-details-list"><div class="map-skeleton"></div><div class="map-skeleton small"></div></div>
            </section>
        </div>`;

    if (detailDrawer) {
        detailDrawer.classList.add('is-open');
        detailDrawer.setAttribute('aria-hidden', 'false');
    }
    loadAreaEvents(area.id);
}

async function loadAreaEvents(areaId) {
    const container = document.getElementById('eventDetails');
    if (!container) return;

    try {
        const response = await fetch(`/api/green-areas/${areaId}/events`);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const events = await response.json();

        if (!events.length) {
            container.innerHTML = '<div class="event-empty"><i class="bi bi-calendar2-heart"></i><span>Nenhum evento próximo neste local.</span></div>';
            return;
        }

        const dateFormatter = new Intl.DateTimeFormat('pt-BR', {
            day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
        });

        container.innerHTML = events.map(event => {
            const start = event.startDate ? dateFormatter.format(new Date(event.startDate)) : 'Data a confirmar';
            return `
                <details class="event-card">
                    <summary>
                        <span class="event-date-icon"><i class="bi bi-calendar3"></i></span>
                        <span><strong>${escapeHtml(event.title || 'Evento')}</strong><small>${escapeHtml(start)}</small></span>
                        <i class="bi bi-chevron-down"></i>
                    </summary>
                    <p>${escapeHtml(event.description || 'Sem descrição adicional.')}</p>
                </details>`;
        }).join('');
    } catch (error) {
        console.error('Erro ao carregar eventos:', error);
        container.innerHTML = '<div class="event-empty error"><i class="bi bi-exclamation-circle"></i><span>Não foi possível carregar a agenda.</span></div>';
    }
}

function syncFavoriteButtons(areaId) {
    const active = favorites.map(String).includes(String(areaId));
    document.querySelectorAll(`[data-favorite-area="${CSS.escape(String(areaId))}"]`).forEach(button => {
        button.classList.toggle('active', active);
        button.classList.toggle('btn-success', active);
        button.classList.toggle('btn-outline-success', !active);
        button.title = active ? 'Remover dos favoritos' : 'Adicionar aos favoritos';
        button.setAttribute('aria-label', button.title);
        const icon = button.querySelector('i');
        if (icon) icon.className = `bi ${active ? 'bi-heart-fill' : 'bi-heart'}`;
        const label = button.querySelector('span');
        if (label) label.textContent = active ? 'Salvo' : 'Salvar';
    });
}

async function toggleFavorite(areaId) {
    if (!window.LOGGED_USER) {
        window.location.href = '/login';
        return;
    }

    const active = favorites.map(String).includes(String(areaId));
    try {
        const response = await fetch(`/api/favorites/${areaId}`, { method: active ? 'DELETE' : 'POST' });
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        favorites = active
            ? favorites.filter(id => String(id) !== String(areaId))
            : [...favorites, Number(areaId)];
        syncFavoriteButtons(areaId);
        showToast(active ? 'Local removido dos favoritos.' : 'Local salvo nos favoritos!');
    } catch (error) {
        console.error('Erro ao atualizar favorito:', error);
        showToast('Não foi possível atualizar seus favoritos.', 'error');
    }
}

async function shareArea(area) {
    const shareUrl = `${window.location.origin}/areas-verdes/${encodeURIComponent(area.id)}`;
    const shareData = {
        title: `${area.name} — EcoRadar`,
        text: `Conheça ${area.name} no EcoRadar.`,
        url: shareUrl
    };

    try {
        if (navigator.share) {
            await navigator.share(shareData);
        } else if (navigator.clipboard) {
            await navigator.clipboard.writeText(shareUrl);
            showToast('Link copiado para a área de transferência!');
        } else {
            showToast('Copie o endereço desta página para compartilhar.', 'error');
        }
    } catch (error) {
        if (error.name !== 'AbortError') showToast('Não foi possível compartilhar agora.', 'error');
    }
}

function setUserLocation(position) {
    currentPosition = { latitude: position.coords.latitude, longitude: position.coords.longitude };
    referencePosition = currentPosition;
    userLocationLayer.clearLayers();

    L.circleMarker([currentPosition.latitude, currentPosition.longitude], {
        radius: 8, color: '#ffffff', weight: 3, fillColor: '#176b49', fillOpacity: 1
    }).bindPopup('Sua localização').addTo(userLocationLayer);

    L.circle([currentPosition.latitude, currentPosition.longitude], {
        radius: Math.min(position.coords.accuracy || 0, 1500),
        color: '#176b49', weight: 1, fillColor: '#69bd83', fillOpacity: .1
    }).addTo(userLocationLayer);

    if (distanceFilter) distanceFilter.disabled = false;
    if (nearbySubtitle) nearbySubtitle.textContent = 'Ordenadas pela sua distância';
    if (locationNotice) locationNotice.hidden = true;
    map.flyTo([currentPosition.latitude, currentPosition.longitude], 14, { duration: .8 });
    applyFilters();
}

function showLocationFallback() {
    currentPosition = null;
    referencePosition = DEFAULT_POSITION;
    maxDistance = Infinity;
    if (distanceFilter) {
        distanceFilter.value = 'all';
        distanceFilter.disabled = true;
        distanceFilter.title = 'Ative sua localização para filtrar por distância';
    }
    if (nearbySubtitle) nearbySubtitle.textContent = 'A partir do centro de Maceió';
    if (locationNotice) locationNotice.hidden = false;
    applyFilters();
}

function requestUserLocation(fromButton = false) {
    if (!navigator.geolocation) {
        showLocationFallback();
        return;
    }

    if (fromButton && retryLocationButton) retryLocationButton.disabled = true;
    navigator.geolocation.getCurrentPosition(
        position => {
            if (retryLocationButton) retryLocationButton.disabled = false;
            setUserLocation(position);
            if (fromButton) showToast('Localização atualizada!');
        },
        error => {
            console.info('Localização indisponível:', error.message);
            if (retryLocationButton) retryLocationButton.disabled = false;
            showLocationFallback();
            if (fromButton) showToast('Autorize a localização no navegador para usar este recurso.', 'error');
        },
        { enableHighAccuracy: true, timeout: 9000, maximumAge: 60000 }
    );
}

async function loadMapData() {
    setLoadingState();
    try {
        const requests = [fetch('/api/green-areas')];
        if (window.LOGGED_USER) requests.push(fetch('/api/favorites'));
        const responses = await Promise.all(requests);
        if (!responses[0].ok) throw new Error(`HTTP ${responses[0].status}`);

        allGreenAreas = await responses[0].json();
        if (responses[1]?.ok) favorites = await responses[1].json();
        buildTypeFilters();
        applyFilters();

        const requestedId = new URLSearchParams(window.location.search).get('area');
        const requestedArea = allGreenAreas.find(area => String(area.id) === String(requestedId));
        if (requestedArea) window.setTimeout(() => selectArea(requestedArea), 250);
    } catch (error) {
        console.error('Erro ao carregar áreas verdes:', error);
        if (nearbyContainer) {
            nearbyContainer.innerHTML = '<div class="panel-empty"><i class="bi bi-wifi-off"></i><p>Não foi possível carregar os locais agora.</p></div>';
        }
        showToast('Não foi possível carregar as áreas verdes.', 'error');
    }
}

function clearFilters() {
    selectedType = 'ALL';
    maxDistance = Infinity;
    if (distanceFilter) distanceFilter.value = 'all';
    buildTypeFilters();
    applyFilters();
}

async function searchPlaces(query) {
    if (!query) return [];
    const url = `https://nominatim.openstreetmap.org/search?format=json&countrycodes=br&limit=5&accept-language=pt-BR&q=${encodeURIComponent(query)}`;
    const response = await fetch(url);
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return response.json();
}

function selectExternalPlace(place) {
    const lat = Number(place.lat);
    const lon = Number(place.lon);
    if (temporaryMarker) map.removeLayer(temporaryMarker);
    temporaryMarker = L.marker([lat, lon]).addTo(map).bindPopup(escapeHtml(place.display_name)).openPopup();
    map.flyTo([lat, lon], 16, { duration: .7 });
    searchInput.value = place.display_name;
    searchResults.innerHTML = '';
}

async function renderSearchSuggestions(query) {
    try {
        const normalized = query.toLocaleLowerCase('pt-BR');
        const localAreas = allGreenAreas.filter(area =>
            area.name?.toLocaleLowerCase('pt-BR').includes(normalized)
            || area.address?.toLocaleLowerCase('pt-BR').includes(normalized)
        ).slice(0, 5);
        const places = await searchPlaces(query);

        searchResults.innerHTML = [
            ...localAreas.map(area => {
                const meta = typeMeta(area);
                return `<li class="list-group-item search-area-result" data-search-area="${area.id}"><i class="bi ${escapeHtml(meta.icon)}"></i><span><strong>${escapeHtml(area.name)}</strong><small>${escapeHtml(area.address || meta.label)}</small></span></li>`;
            }),
            ...(localAreas.length && places.length ? ['<li class="list-group-item search-separator">Outros endereços</li>'] : []),
            ...places.map((place, index) => `<li class="list-group-item search-place-result" data-place-index="${index}"><i class="bi bi-geo-alt"></i><span>${escapeHtml(place.display_name)}</span></li>`)
        ].join('');

        searchResults.querySelectorAll('[data-search-area]').forEach(item => {
            item.addEventListener('click', () => {
                const area = allGreenAreas.find(candidate => String(candidate.id) === item.dataset.searchArea);
                if (area) {
                    selectArea(area);
                    searchInput.value = area.name;
                    searchResults.innerHTML = '';
                }
            });
        });
        searchResults.querySelectorAll('[data-place-index]').forEach(item => {
            item.addEventListener('click', () => selectExternalPlace(places[Number(item.dataset.placeIndex)]));
        });
    } catch (error) {
        console.error('Erro ao buscar sugestões:', error);
        searchResults.innerHTML = '<li class="list-group-item search-message">Não foi possível buscar endereços agora.</li>';
    }
}

async function submitSearch() {
    const query = searchInput.value.trim();
    if (!query) return;

    const localArea = allGreenAreas.find(area => area.name?.toLocaleLowerCase('pt-BR').includes(query.toLocaleLowerCase('pt-BR')));
    if (localArea) {
        selectArea(localArea);
        searchResults.innerHTML = '';
        return;
    }

    searchButton.disabled = true;
    try {
        const places = await searchPlaces(query);
        if (places.length) selectExternalPlace(places[0]);
        else showToast('Nenhum local encontrado para essa busca.', 'error');
    } catch (error) {
        console.error('Erro na busca:', error);
        showToast('A busca está indisponível no momento.', 'error');
    } finally {
        searchButton.disabled = false;
    }
}

document.addEventListener('click', event => {
    const favorite = event.target.closest('[data-favorite-area]');
    if (favorite) {
        event.preventDefault();
        event.stopPropagation();
        toggleFavorite(favorite.dataset.favoriteArea);
        return;
    }

    const showArea = event.target.closest('[data-show-area]');
    if (showArea) {
        const area = allGreenAreas.find(candidate => String(candidate.id) === showArea.dataset.showArea);
        if (area) selectArea(area, false);
        return;
    }

    const share = event.target.closest('[data-share-area]');
    if (share) {
        const area = allGreenAreas.find(candidate => String(candidate.id) === share.dataset.shareArea);
        if (area) shareArea(area);
        return;
    }

    if (event.target.closest('[data-clear-filters]')) clearFilters();
});

typeFilterGroup?.addEventListener('click', event => {
    const button = event.target.closest('[data-filter-type]');
    if (!button) return;
    selectedType = button.dataset.filterType;
    typeFilterGroup.querySelectorAll('[data-filter-type]').forEach(filterButton => {
        const active = filterButton === button;
        filterButton.classList.toggle('active', active);
        filterButton.setAttribute('aria-pressed', String(active));
    });
    applyFilters();
});

distanceFilter?.addEventListener('change', () => {
    maxDistance = distanceFilter.value === 'all' ? Infinity : Number(distanceFilter.value);
    applyFilters();
});

retryLocationButton?.addEventListener('click', () => requestUserLocation(true));

closeAreaDetails?.addEventListener('click', () => {
    detailDrawer.classList.remove('is-open');
    detailDrawer.setAttribute('aria-hidden', 'true');
});

searchInput?.addEventListener('input', () => {
    window.clearTimeout(debounceTimer);
    const query = searchInput.value.trim();
    if (!query) {
        searchResults.innerHTML = '';
        return;
    }
    debounceTimer = window.setTimeout(() => renderSearchSuggestions(query), 450);
});

searchInput?.addEventListener('keydown', event => {
    if (event.key === 'Enter') {
        event.preventDefault();
        const firstResult = searchResults.querySelector('[data-search-area], [data-place-index]');
        if (firstResult) firstResult.click();
        else submitSearch();
    } else if (event.key === 'Escape') {
        searchResults.innerHTML = '';
    }
});

searchButton?.addEventListener('click', submitSearch);

document.addEventListener('click', event => {
    if (!event.target.closest('.map-search')) searchResults.innerHTML = '';
});

initializeMap();
loadMapData();
requestUserLocation();
