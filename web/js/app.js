/**
 * Real Estate India - Main Application
 * Connected to Java Backend via REST API
 */

const API_BASE = '';

// ── State ──
const state = {
  properties: [],
  cities: [],
  statistics: {},
  total: 0,
  page: 1,
  totalPages: 1,
  size: 12,
  filters: {
    search: '',
    listingType: 'all',
    city: 'all',
    propertyType: 'all',
    bhk: 'all',
    minPrice: '',
    maxPrice: '',
    furnishing: 'all',
    sort: 'newest'
  },
  favorites: JSON.parse(localStorage.getItem('favorites') || '[]'),
  compareList: JSON.parse(localStorage.getItem('compareList') || '[]'),
  recentlyViewed: JSON.parse(localStorage.getItem('recentlyViewed') || '[]'),
  viewMode: 'grid',
  loading: false
};

// ── Init ──
document.addEventListener('DOMContentLoaded', () => {
  initHeader();
  loadCities();
  loadStatistics();
  loadProperties();
  renderRecentlyViewed();
  renderCompareBar();
  initEventListeners();
});

// ════════════════════════
//  API CALLS
// ════════════════════════

async function fetchAPI(endpoint) {
  try {
    const res = await fetch(`${API_BASE}${endpoint}`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    return await res.json();
  } catch (err) {
    console.error('API Error:', err);
    showToast('Failed to connect to server', 'error');
    return null;
  }
}

async function postAPI(endpoint, data) {
  try {
    const res = await fetch(`${API_BASE}${endpoint}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data)
    });
    return await res.json();
  } catch (err) {
    console.error('API Error:', err);
    showToast('Failed to submit request', 'error');
    return null;
  }
}

// ════════════════════════
//  LOAD DATA
// ════════════════════════

async function loadProperties() {
  state.loading = true;
  renderSkeletons();

  const params = new URLSearchParams();
  params.set('page', state.page);
  params.set('size', state.size);

  if (state.filters.search) params.set('search', state.filters.search);
  if (state.filters.listingType !== 'all') params.set('listingType', state.filters.listingType);
  if (state.filters.city !== 'all') params.set('city', state.filters.city);
  if (state.filters.propertyType !== 'all') params.set('propertyType', state.filters.propertyType);
  if (state.filters.bhk !== 'all') params.set('bhk', state.filters.bhk);
  if (state.filters.minPrice) params.set('minPrice', state.filters.minPrice);
  if (state.filters.maxPrice) params.set('maxPrice', state.filters.maxPrice);
  if (state.filters.furnishing !== 'all') params.set('furnishing', state.filters.furnishing);
  if (state.filters.sort) params.set('sort', state.filters.sort);

  const data = await fetchAPI(`/api/properties?${params.toString()}`);

  if (data) {
    state.properties = data.properties;
    state.total = data.total;
    state.totalPages = data.totalPages;
    renderProperties();
    renderPagination();
    updateResultsCount();
  }

  state.loading = false;
}

async function loadCities() {
  const data = await fetchAPI('/api/cities');
  if (data) {
    state.cities = data;
    renderCityChips();
    renderCityFilter();
  }
}

async function loadStatistics() {
  const data = await fetchAPI('/api/statistics');
  if (data) {
    state.statistics = data;
    renderStatistics();
    updateHeroStats();
  }
}

// ════════════════════════
//  RENDER FUNCTIONS
// ════════════════════════

function renderProperties() {
  const grid = document.getElementById('propertyGrid');
  if (!state.properties || state.properties.length === 0) {
    grid.innerHTML = `
      <div class="empty-state" style="grid-column: 1/-1;">
        <div class="icon">🏠</div>
        <h3>No properties found</h3>
        <p>Try adjusting your filters or search terms</p>
      </div>`;
    return;
  }

  grid.innerHTML = state.properties.map(p => createPropertyCard(p)).join('');
}

function createPropertyCard(property) {
  const isFav = state.favorites.includes(property.id);
  const isCompare = state.compareList.includes(property.id);
  const priceFormatted = formatPrice(property.price);
  const period = property.listingType === 'rent' ? '/month' : '';

  return `
    <div class="property-card" onclick="openPropertyDetail('${property.id}')" id="card-${property.id}">
      <div class="card-image">
        <img src="${property.image}" alt="${property.title}" loading="lazy"
             onerror="this.src='https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800'">
        <div class="card-badges">
          <span class="badge ${property.listingType}">${property.listingType === 'rent' ? 'For Rent' : 'For Sale'}</span>
          <span class="badge type">${property.propertyType}</span>
        </div>
        <div class="card-actions-overlay">
          <button class="card-action-btn ${isFav ? 'favorited' : ''}" 
                  onclick="event.stopPropagation(); toggleFavorite('${property.id}')" 
                  title="Add to favorites">
            ${isFav ? '❤️' : '🤍'}
          </button>
          <button class="card-action-btn ${isCompare ? 'favorited' : ''}" 
                  onclick="event.stopPropagation(); toggleCompare('${property.id}')"
                  title="Add to compare">
            ⚖️
          </button>
        </div>
      </div>
      <div class="card-content">
        <div class="card-price">${priceFormatted}<span class="period">${period}</span></div>
        <div class="card-title">${property.title}</div>
        <div class="card-location">📍 ${property.locality}, ${property.city}</div>
        <div class="card-specs">
          ${property.bhk > 0 ? `<div class="spec"><span class="icon">🛏️</span> ${property.bhk} BHK</div>` : ''}
          <div class="spec"><span class="icon">📐</span> ${property.area} sq.ft</div>
          <div class="spec"><span class="icon">🏗️</span> ${property.status}</div>
        </div>
      </div>
    </div>`;
}

function renderSkeletons() {
  const grid = document.getElementById('propertyGrid');
  let html = '';
  for (let i = 0; i < 6; i++) {
    html += `
      <div class="skeleton">
        <div class="skeleton-image"></div>
        <div class="skeleton-text"></div>
        <div class="skeleton-text short"></div>
        <div class="skeleton-text"></div>
      </div>`;
  }
  grid.innerHTML = html;
}

function renderCityChips() {
  const container = document.getElementById('cityChips');
  if (!container) return;

  let html = `<button class="city-chip active" onclick="filterByCity('all')">All Cities</button>`;
  state.cities.forEach(c => {
    html += `<button class="city-chip" onclick="filterByCity('${c.city}')">
      ${c.city}<span class="count">${c.count}</span>
    </button>`;
  });
  container.innerHTML = html;
}

function renderCityFilter() {
  const select = document.getElementById('filterCity');
  if (!select) return;

  let html = `<option value="all">All Cities</option>`;
  state.cities.forEach(c => {
    html += `<option value="${c.city}">${c.city} (${c.count})</option>`;
  });
  select.innerHTML = html;
}

function renderPagination() {
  const container = document.getElementById('pagination');
  if (!container || state.totalPages <= 1) {
    if (container) container.innerHTML = '';
    return;
  }

  let html = '';
  html += `<button class="page-btn ${state.page === 1 ? 'disabled' : ''}" onclick="goToPage(${state.page - 1})">‹</button>`;

  for (let i = 1; i <= state.totalPages; i++) {
    if (i === 1 || i === state.totalPages || (i >= state.page - 2 && i <= state.page + 2)) {
      html += `<button class="page-btn ${i === state.page ? 'active' : ''}" onclick="goToPage(${i})">${i}</button>`;
    } else if (i === state.page - 3 || i === state.page + 3) {
      html += `<button class="page-btn disabled">…</button>`;
    }
  }

  html += `<button class="page-btn ${state.page === state.totalPages ? 'disabled' : ''}" onclick="goToPage(${state.page + 1})">›</button>`;
  container.innerHTML = html;
}

function updateResultsCount() {
  const el = document.getElementById('resultsCount');
  if (el) {
    el.innerHTML = `Showing <span>${state.properties.length}</span> of <span>${state.total}</span> properties`;
  }
}

function updateHeroStats() {
  const stats = state.statistics;
  const totalEl = document.getElementById('heroTotal');
  const citiesEl = document.getElementById('heroCities');
  const rentEl = document.getElementById('heroRent');

  if (totalEl) totalEl.textContent = stats.totalProperties || 0;
  if (citiesEl) citiesEl.textContent = state.cities.length || 0;
  if (rentEl) rentEl.textContent = stats.rentCount || 0;
}

// ════════════════════════
//  STATISTICS
// ════════════════════════

function renderStatistics() {
  const stats = state.statistics;
  if (!stats.avgPriceByCity) return;

  // Stat cards
  document.getElementById('statTotal').textContent = stats.totalProperties || 0;
  document.getElementById('statRent').textContent = stats.rentCount || 0;
  document.getElementById('statBuy').textContent = stats.buyCount || 0;
  document.getElementById('statCities').textContent = state.cities.length;

  // City price bars
  const barsContainer = document.getElementById('cityBars');
  if (!barsContainer || !stats.avgPriceByCity) return;

  const maxPrice = Math.max(...stats.avgPriceByCity.map(c => c.avgPrice));

  let html = '';
  stats.avgPriceByCity.forEach(city => {
    const pct = (city.avgPrice / maxPrice * 100).toFixed(1);
    html += `
      <div class="city-bar-item">
        <div class="city-name">${city.city}</div>
        <div class="bar-container">
          <div class="bar" style="width: ${pct}%">
            <span class="bar-value">${formatPrice(city.avgPrice)}</span>
          </div>
        </div>
      </div>`;
  });
  barsContainer.innerHTML = html;
}

// ════════════════════════
//  FILTERS & SEARCH
// ════════════════════════

function initEventListeners() {
  // Search input
  const searchInput = document.getElementById('searchInput');
  if (searchInput) {
    let debounce;
    searchInput.addEventListener('input', (e) => {
      clearTimeout(debounce);
      debounce = setTimeout(() => {
        state.filters.search = e.target.value;
        state.page = 1;
        loadProperties();
      }, 400);
    });

    searchInput.addEventListener('keypress', (e) => {
      if (e.key === 'Enter') {
        clearTimeout(debounce);
        state.filters.search = e.target.value;
        state.page = 1;
        loadProperties();
      }
    });
  }

  // Search button
  const searchBtn = document.getElementById('searchBtn');
  if (searchBtn) {
    searchBtn.addEventListener('click', () => {
      state.filters.search = document.getElementById('searchInput').value;
      state.page = 1;
      loadProperties();
    });
  }

  // Filter controls
  const filters = ['filterCity', 'filterPropertyType', 'filterBHK', 'filterFurnishing', 'sortSelect'];
  filters.forEach(id => {
    const el = document.getElementById(id);
    if (el) {
      el.addEventListener('change', () => applyFilters());
    }
  });

  // Price range inputs
  ['filterMinPrice', 'filterMaxPrice'].forEach(id => {
    const el = document.getElementById(id);
    if (el) {
      let debounce;
      el.addEventListener('input', () => {
        clearTimeout(debounce);
        debounce = setTimeout(() => applyFilters(), 600);
      });
    }
  });

  // Clear filters
  const clearBtn = document.getElementById('clearFilters');
  if (clearBtn) {
    clearBtn.addEventListener('click', clearFilters);
  }

  // Filter toggle (mobile)
  const filterToggle = document.getElementById('filterToggle');
  if (filterToggle) {
    filterToggle.addEventListener('click', () => {
      document.getElementById('filterSidebar').classList.toggle('show');
    });
  }

  // View toggle
  document.querySelectorAll('.view-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.view-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      const mode = btn.dataset.view;
      state.viewMode = mode;
      const grid = document.getElementById('propertyGrid');
      grid.classList.toggle('list-view', mode === 'list');
    });
  });

  // Close modals on overlay click
  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) closeAllModals();
    });
  });

  // Escape key
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeAllModals();
  });

  // Scroll header effect
  window.addEventListener('scroll', () => {
    document.querySelector('.header').classList.toggle('scrolled', window.scrollY > 50);
  });
}

function applyFilters() {
  state.filters.city = document.getElementById('filterCity')?.value || 'all';
  state.filters.propertyType = document.getElementById('filterPropertyType')?.value || 'all';
  state.filters.bhk = document.getElementById('filterBHK')?.value || 'all';
  state.filters.furnishing = document.getElementById('filterFurnishing')?.value || 'all';
  state.filters.minPrice = document.getElementById('filterMinPrice')?.value || '';
  state.filters.maxPrice = document.getElementById('filterMaxPrice')?.value || '';
  state.filters.sort = document.getElementById('sortSelect')?.value || 'newest';
  state.page = 1;

  // Update city chips
  document.querySelectorAll('.city-chip').forEach(chip => {
    chip.classList.remove('active');
    const city = chip.textContent.trim().split(/\d/)[0].trim();
    if ((state.filters.city === 'all' && city === 'All Cities') || city === state.filters.city) {
      chip.classList.add('active');
    }
  });

  loadProperties();
}

function clearFilters() {
  state.filters = {
    search: '',
    listingType: 'all',
    city: 'all',
    propertyType: 'all',
    bhk: 'all',
    minPrice: '',
    maxPrice: '',
    furnishing: 'all',
    sort: 'newest'
  };
  state.page = 1;

  // Reset UI
  const searchInput = document.getElementById('searchInput');
  if (searchInput) searchInput.value = '';

  ['filterCity', 'filterPropertyType', 'filterBHK', 'filterFurnishing'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = 'all';
  });
  const sortEl = document.getElementById('sortSelect');
  if (sortEl) sortEl.value = 'newest';

  ['filterMinPrice', 'filterMaxPrice'].forEach(id => {
    const el = document.getElementById(id); if (el) el.value = '';
  });

  // Reset listing tabs
  document.querySelectorAll('.listing-tab').forEach(t => t.classList.remove('active'));
  document.querySelector('.listing-tab[data-type="all"]')?.classList.add('active');

  // Reset city chips
  document.querySelectorAll('.city-chip').forEach(c => c.classList.remove('active'));
  document.querySelector('.city-chip')?.classList.add('active');

  loadProperties();
  showToast('Filters cleared', 'info');
}

function filterByListingType(type) {
  state.filters.listingType = type;
  state.page = 1;

  document.querySelectorAll('.listing-tab').forEach(t => {
    t.classList.toggle('active', t.dataset.type === type);
  });

  loadProperties();
}

function filterByCity(city) {
  state.filters.city = city;
  state.page = 1;

  // Update chips
  document.querySelectorAll('.city-chip').forEach(chip => {
    chip.classList.remove('active');
  });
  event.target.closest('.city-chip')?.classList.add('active');

  // Update select
  const select = document.getElementById('filterCity');
  if (select) select.value = city;

  loadProperties();
}

function goToPage(page) {
  if (page < 1 || page > state.totalPages) return;
  state.page = page;
  loadProperties();
  document.getElementById('properties').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// ════════════════════════
//  PROPERTY DETAIL MODAL
// ════════════════════════

async function openPropertyDetail(id) {
  const data = await fetchAPI(`/api/properties/${id}`);
  if (!data) return;

  addToRecentlyViewed(data);

  const modal = document.getElementById('detailModal');
  const content = document.getElementById('detailContent');
  const priceFormatted = formatPrice(data.price);
  const period = data.listingType === 'rent' ? '/month' : '';
  const isFav = state.favorites.includes(data.id);

  content.innerHTML = `
    <img class="detail-image" src="${data.image}" alt="${data.title}"
         onerror="this.src='https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800'">
    <div class="detail-content">
      <div class="detail-header">
        <div>
          <div class="detail-price">${priceFormatted}<span class="period">${period}</span></div>
          <div class="detail-title">${data.title}</div>
          <div class="detail-location">📍 ${data.locality}, ${data.city}, ${data.state}</div>
        </div>
      </div>
      
      <div class="detail-tags">
        <span class="detail-tag">${data.listingType === 'rent' ? '🔑 For Rent' : '🏷️ For Sale'}</span>
        <span class="detail-tag">🏢 ${data.propertyType}</span>
        <span class="detail-tag">🛋️ ${data.furnishing}</span>
        <span class="detail-tag">📅 Posted: ${formatDate(data.postedDate)}</span>
        <span class="detail-tag">🏗️ ${data.status}</span>
      </div>

      <div class="detail-specs">
        ${data.bhk > 0 ? `<div class="detail-spec"><div class="value">${data.bhk}</div><div class="label">BHK</div></div>` : ''}
        <div class="detail-spec"><div class="value">${data.area}</div><div class="label">Sq. Ft.</div></div>
        <div class="detail-spec"><div class="value">${data.bhk > 0 ? formatPrice(Math.round(data.price / data.area)) : 'N/A'}</div><div class="label">Price/Sq.Ft</div></div>
        <div class="detail-spec"><div class="value">${data.builderName}</div><div class="label">Builder</div></div>
      </div>

      <h3 style="margin-bottom: 12px; font-size: 1rem;">About this property</h3>
      <p class="detail-description">${data.description}</p>

      <h3 style="margin-bottom: 12px; font-size: 1rem;">Amenities</h3>
      <div class="detail-amenities">
        ${data.amenities.map(a => `<span class="amenity-tag">✓ ${a}</span>`).join('')}
      </div>

      <div class="detail-actions">
        <button class="btn-primary" onclick="openInquiryForm('${data.id}', '${data.title.replace(/'/g, "\\'")}')">📩 Contact Owner</button>
        ${data.listingType === 'buy' ? `<button class="btn-secondary" onclick="openEMICalculator(${data.price})">🧮 EMI Calculator</button>` : ''}
        <button class="btn-secondary" onclick="toggleFavorite('${data.id}')">
          ${isFav ? '❤️ Remove from Favorites' : '🤍 Add to Favorites'}
        </button>
        <button class="btn-secondary" onclick="toggleCompare('${data.id}')">⚖️ Compare</button>
      </div>
    </div>`;

  openModal('detailModal');
}

// ════════════════════════
//  EMI CALCULATOR
// ════════════════════════

function openEMICalculator(price) {
  closeAllModals();
  const priceInLakh = price ? (price / 100000) : '';
  document.getElementById('emiLoanAmount').value = priceInLakh ? Math.round(priceInLakh * 80) / 100 * 100000 : '';
  document.getElementById('emiInterest').value = '8.5';
  document.getElementById('emiTenure').value = '20';

  if (price) calculateEMI();
  openModal('emiModal');
}

function calculateEMI() {
  const P = parseFloat(document.getElementById('emiLoanAmount').value) || 0;
  const annualRate = parseFloat(document.getElementById('emiInterest').value) || 0;
  const years = parseInt(document.getElementById('emiTenure').value) || 0;

  if (!P || !annualRate || !years) {
    document.getElementById('emiResult').innerHTML = '<p style="color: var(--text-muted);">Enter all values to calculate</p>';
    return;
  }

  const r = annualRate / 12 / 100; // monthly interest rate
  const n = years * 12; // total months

  const emi = (P * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
  const totalAmount = emi * n;
  const totalInterest = totalAmount - P;

  document.getElementById('emiResult').innerHTML = `
    <div class="emi-amount">${formatPrice(Math.round(emi))}</div>
    <div style="color: var(--text-muted); font-size: 0.85rem;">Monthly EMI</div>
    <div class="emi-breakdown">
      <div class="emi-breakdown-item">
        <div class="value">${formatPrice(P)}</div>
        <div class="label">Principal</div>
      </div>
      <div class="emi-breakdown-item">
        <div class="value">${formatPrice(Math.round(totalInterest))}</div>
        <div class="label">Total Interest</div>
      </div>
      <div class="emi-breakdown-item">
        <div class="value">${formatPrice(Math.round(totalAmount))}</div>
        <div class="label">Total Amount</div>
      </div>
    </div>`;
}

// ════════════════════════
//  FAVORITES
// ════════════════════════

function toggleFavorite(id) {
  const idx = state.favorites.indexOf(id);
  if (idx > -1) {
    state.favorites.splice(idx, 1);
    showToast('Removed from favorites', 'info');
  } else {
    state.favorites.push(id);
    showToast('Added to favorites! ❤️', 'success');
  }
  localStorage.setItem('favorites', JSON.stringify(state.favorites));
  renderProperties(); // Re-render to update icons
}

// ════════════════════════
//  COMPARE
// ════════════════════════

function toggleCompare(id) {
  const idx = state.compareList.indexOf(id);
  if (idx > -1) {
    state.compareList.splice(idx, 1);
    showToast('Removed from comparison', 'info');
  } else {
    if (state.compareList.length >= 3) {
      showToast('You can compare up to 3 properties', 'error');
      return;
    }
    state.compareList.push(id);
    showToast('Added to comparison ⚖️', 'success');
  }
  localStorage.setItem('compareList', JSON.stringify(state.compareList));
  renderCompareBar();
  renderProperties();
}

function renderCompareBar() {
  const bar = document.getElementById('compareBar');
  if (!bar) return;

  if (state.compareList.length === 0) {
    bar.classList.remove('show');
    return;
  }

  bar.classList.add('show');
  const itemsEl = document.getElementById('compareItems');
  itemsEl.innerHTML = state.compareList.map(id => `
    <div class="compare-item">
      <span>${id}</span>
      <button class="remove" onclick="toggleCompare('${id}')">✕</button>
    </div>
  `).join('');
}

async function openCompareModal() {
  if (state.compareList.length < 2) {
    showToast('Add at least 2 properties to compare', 'error');
    return;
  }

  closeAllModals();
  const properties = [];
  for (const id of state.compareList) {
    const data = await fetchAPI(`/api/properties/${id}`);
    if (data) properties.push(data);
  }

  const modal = document.getElementById('compareModal');
  const container = document.getElementById('compareContent');

  const rows = [
    { label: 'Image', key: 'image', format: v => `<img class="compare-img" src="${v}" onerror="this.src='https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800'">` },
    { label: 'Title', key: 'title' },
    { label: 'Price', key: 'price', format: v => formatPrice(v) },
    { label: 'Type', key: 'listingType', format: v => v === 'rent' ? 'For Rent' : 'For Sale' },
    { label: 'Location', key: 'locality', format: (v, p) => `${v}, ${p.city}` },
    { label: 'BHK', key: 'bhk', format: v => v > 0 ? `${v} BHK` : 'N/A' },
    { label: 'Area', key: 'area', format: v => `${v} sq.ft` },
    { label: 'Property Type', key: 'propertyType' },
    { label: 'Furnishing', key: 'furnishing' },
    { label: 'Status', key: 'status' },
    { label: 'Builder', key: 'builderName' },
    { label: 'Amenities', key: 'amenities', format: v => v.join(', ') }
  ];

  let html = '<table class="compare-table"><tbody>';
  rows.forEach(row => {
    html += `<tr><th>${row.label}</th>`;
    properties.forEach(p => {
      const val = row.format ? row.format(p[row.key], p) : p[row.key];
      html += `<td>${val}</td>`;
    });
    html += '</tr>';
  });
  html += '</tbody></table>';

  container.innerHTML = html;
  openModal('compareModal');
}

// ════════════════════════
//  RECENTLY VIEWED
// ════════════════════════

function addToRecentlyViewed(property) {
  // Remove if already exists
  state.recentlyViewed = state.recentlyViewed.filter(p => p.id !== property.id);
  // Add to front
  state.recentlyViewed.unshift({
    id: property.id,
    title: property.title,
    price: property.price,
    listingType: property.listingType,
    image: property.image
  });
  // Keep only last 10
  state.recentlyViewed = state.recentlyViewed.slice(0, 10);
  localStorage.setItem('recentlyViewed', JSON.stringify(state.recentlyViewed));
  renderRecentlyViewed();
}

function renderRecentlyViewed() {
  const container = document.getElementById('recentlyViewed');
  const section = document.getElementById('recentSection');
  if (!container || !section) return;

  if (state.recentlyViewed.length === 0) {
    section.style.display = 'none';
    return;
  }

  section.style.display = 'block';
  container.innerHTML = state.recentlyViewed.map(p => `
    <div class="recent-card" onclick="openPropertyDetail('${p.id}')">
      <img src="${p.image}" alt="${p.title}" loading="lazy"
           onerror="this.src='https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800'">
      <div class="info">
        <div class="price">${formatPrice(p.price)}${p.listingType === 'rent' ? '/mo' : ''}</div>
        <div class="title">${p.title}</div>
      </div>
    </div>
  `).join('');
}

// ════════════════════════
//  INQUIRY FORM
// ════════════════════════

function openInquiryForm(propertyId, propertyTitle) {
  closeAllModals();
  document.getElementById('inquiryPropertyId').value = propertyId || '';
  document.getElementById('inquiryPropertyTitle').textContent = propertyTitle || 'Property';
  document.getElementById('inquiryName').value = '';
  document.getElementById('inquiryEmail').value = '';
  document.getElementById('inquiryPhone').value = '';
  document.getElementById('inquiryMessage').value = `Hi, I'm interested in this property. Please share more details.`;
  openModal('inquiryModal');
}

async function submitInquiry() {
  const data = {
    propertyId: document.getElementById('inquiryPropertyId').value,
    name: document.getElementById('inquiryName').value,
    email: document.getElementById('inquiryEmail').value,
    phone: document.getElementById('inquiryPhone').value,
    message: document.getElementById('inquiryMessage').value
  };

  if (!data.name || !data.email) {
    showToast('Please fill in name and email', 'error');
    return;
  }

  const result = await postAPI('/api/inquiries', data);
  if (result && result.success) {
    showToast('Inquiry submitted successfully! 📩', 'success');
    closeAllModals();
  }
}

// ════════════════════════
//  MODAL HELPERS
// ════════════════════════

function openModal(id) {
  document.getElementById(id).classList.add('show');
  document.body.style.overflow = 'hidden';
}

function closeAllModals() {
  document.querySelectorAll('.modal-overlay').forEach(m => m.classList.remove('show'));
  document.body.style.overflow = '';
}

// ════════════════════════
//  HEADER / NAV
// ════════════════════════

function initHeader() {
  const toggle = document.getElementById('mobileToggle');
  const nav = document.getElementById('navLinks');

  if (toggle && nav) {
    toggle.addEventListener('click', () => {
      nav.classList.toggle('show');
    });
  }

  // Nav link active state
  document.querySelectorAll('.nav-link').forEach(link => {
    link.addEventListener('click', () => {
      document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
      link.classList.add('active');
      if (nav) nav.classList.remove('show');
    });
  });
}

function scrollToSection(id) {
  const el = document.getElementById(id);
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
}

// ════════════════════════
//  UTILITY FUNCTIONS
// ════════════════════════

function formatPrice(price) {
  if (price >= 10000000) {
    return '₹' + (price / 10000000).toFixed(2) + ' Cr';
  } else if (price >= 100000) {
    return '₹' + (price / 100000).toFixed(2) + ' L';
  } else if (price >= 1000) {
    return '₹' + (price / 1000).toFixed(1) + 'K';
  }
  return '₹' + price.toLocaleString('en-IN');
}

function formatDate(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
}

function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  if (!container) return;

  const icons = { success: '✅', error: '❌', info: 'ℹ️' };
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `<span>${icons[type] || ''}</span> ${message}`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}
