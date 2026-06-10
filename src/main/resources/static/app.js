// ===== State =====
const state = {
  selectedDate: null,
  selectedThemeId: null,
  selectedThemeName: null,
  selectedTimeId: null,
  selectedTimeLabel: null,
  themes: [],
  availableDates: [],
  currentCalendarYear: new Date().getFullYear(),
  currentCalendarMonth: new Date().getMonth(),
};

// ===== DOM Helpers =====
const $ = id => document.getElementById(id);
const qs = (sel, ctx = document) => ctx.querySelector(sel);

// ===== Toast =====
function showToast(msg, type = 'default') {
  const el = document.createElement('div');
  el.className = `toast ${type}`;
  el.textContent = msg;
  $('toast-container').appendChild(el);
  setTimeout(() => el.remove(), 3100);
}

// ===== API =====
const api = {
  async get(url) {
    const res = await fetch(url);
    if (!res.ok) throw new Error(`GET ${url} failed: ${res.status}`);
    return res.json();
  },
  async post(url, body) {
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || `POST ${url} failed: ${res.status}`);
    }
    return res.json();
  },
  async del(url) {
    const res = await fetch(url, { method: 'DELETE' });
    if (!res.ok) throw new Error(`DELETE ${url} failed: ${res.status}`);
    return res;
  },
};

// ===== Navigation =====
function switchPage(pageId) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.nav-tab').forEach(t => {
    t.classList.remove('active');
    t.setAttribute('aria-selected', 'false');
  });
  $(pageId).classList.add('active');
  const tab = document.querySelector(`[data-page="${pageId}"]`);
  if (tab) { tab.classList.add('active'); tab.setAttribute('aria-selected', 'true'); }

  if (pageId === 'home-page') loadPopularThemes();
  if (pageId === 'admin-page') initAdminPage();
}

// ===== HOME PAGE: Popular Themes =====
let _detailTheme = null;

async function loadPopularThemes() {
  const grid = $('popular-grid');
  try {
    const themes = await api.get('/themes/popular');
    if (!themes || themes.length === 0) {
      grid.innerHTML = `<p style="color:var(--text-muted);grid-column:1/-1;text-align:center;padding:40px 0">등록된 인기 테마가 없습니다.</p>`;
      return;
    }
    grid.innerHTML = '';
    themes.slice(0, 10).forEach((theme, idx) => {
      const card = document.createElement('div');
      card.className = 'popular-card';
      card.innerHTML = `
        <div class="popular-card-img-wrap">
          <img src="${theme.thumbnailUrl}" alt="${theme.name}" onerror="this.style.display='none'">
          <div class="popular-card-rank">#${idx + 1}</div>
        </div>
        <div class="popular-card-body">
          <div class="popular-card-name">${theme.name}</div>
          <div class="popular-card-desc">${theme.description}</div>
          <span class="popular-card-cta">테마 상세보기 →</span>
        </div>
      `;
      card.addEventListener('click', () => openThemeDetail(theme));
      grid.appendChild(card);
    });
  } catch (e) {
    grid.innerHTML = `<p style="color:var(--text-muted);grid-column:1/-1;padding:40px 0">테마를 불러오지 못했습니다.</p>`;
  }
}

function openThemeDetail(theme) {
  _detailTheme = theme;
  $('theme-detail-img').src = theme.thumbnailUrl;
  $('theme-detail-img').alt = theme.name;
  $('theme-detail-title').textContent = theme.name;
  $('theme-detail-desc').textContent = theme.description;
  $('theme-detail-modal').classList.add('open');
}

function closeThemeDetail() {
  $('theme-detail-modal').classList.remove('open');
}

function bookFromDetail() {
  closeThemeDetail();
  switchPage('user-page');
  if (_detailTheme && state.themes.length > 0) {
    const match = state.themes.find(t => t.id === _detailTheme.id);
    if (match) {
      setTimeout(() => {
        const cardEl = document.querySelector(`.theme-card[data-id="${_detailTheme.id}"]`);
        if (cardEl) selectTheme(match, cardEl);
      }, 80);
    }
  }
}

// ===== CALENDAR =====
function renderCalendar() {
  const { currentCalendarYear: y, currentCalendarMonth: m, availableDates } = state;
  const monthNames = ['January','February','March','April','May','June',
                      'July','August','September','October','November','December'];
  $('cal-month-label').textContent = `${monthNames[m]} ${y}`;

  const grid = $('calendar-grid');
  grid.querySelectorAll('.calendar-day').forEach(el => el.remove());

  const firstDay = new Date(y, m, 1).getDay();
  const daysInMonth = new Date(y, m + 1, 0).getDate();
  const today = new Date(); today.setHours(0, 0, 0, 0);
  const availableSet = new Set(availableDates);

  for (let i = 0; i < firstDay; i++) {
    const el = document.createElement('div');
    el.className = 'calendar-day empty';
    grid.appendChild(el);
  }

  for (let d = 1; d <= daysInMonth; d++) {
    const date = new Date(y, m, d);
    const dateStr = toISODate(date);
    const isToday = date.getTime() === today.getTime();
    const isPast  = date < today;
    const isAvailable = availableSet.has(dateStr);

    const el = document.createElement('div');
    el.className = 'calendar-day';
    el.textContent = d;
    el.dataset.date = dateStr;

    if (isPast || !isAvailable) {
      el.classList.add('disabled');
    } else {
      el.classList.add('available');
      el.addEventListener('click', () => selectDate(dateStr, el));
    }
    if (isToday) el.classList.add('today');
    if (state.selectedDate === dateStr) el.classList.add('selected');
    grid.appendChild(el);
  }
}

function selectDate(dateStr, el) {
  if (el.classList.contains('disabled')) return;
  state.selectedDate = dateStr;
  document.querySelectorAll('.calendar-day').forEach(d => d.classList.remove('selected'));
  el.classList.add('selected');
  updateCTAInfo();
  loadTimeSlots();
}

function toISODate(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

// ===== THEMES =====
async function loadThemes() {
  const opts = await api.get('/reservations/date-and-theme');
  state.availableDates = opts.dates || [];
  state.themes = opts.themes || [];
  renderCalendar();
  renderThemes();
}

function renderThemes() {
  const list = $('theme-list');
  list.innerHTML = '';
  state.themes.forEach(theme => {
    const isSelected = state.selectedThemeId === theme.id;
    const card = document.createElement('div');
    card.className = 'theme-card' + (isSelected ? ' selected' : '');
    card.dataset.id = theme.id;
    card.innerHTML = `
      <img class="theme-card-img" src="${theme.thumbnailUrl}" alt="${theme.name}" onerror="this.style.background='var(--surface-3)'">
      <div class="theme-card-check">✓</div>
      <div class="theme-card-body">
        <div class="theme-card-name">${theme.name}</div>
        <div class="theme-card-desc">${theme.description}</div>
      </div>
    `;
    card.addEventListener('click', () => selectTheme(theme, card));
    list.appendChild(card);
  });
}

function selectTheme(theme, card) {
  state.selectedThemeId = theme.id;
  state.selectedThemeName = theme.name;
  document.querySelectorAll('.theme-card').forEach(c => c.classList.remove('selected'));
  card.classList.add('selected');
  // scroll into view inside scrollable column
  card.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  updateCTAInfo();
  loadTimeSlots();
}

// ===== TIME SLOTS =====
async function loadTimeSlots() {
  const { selectedDate, selectedThemeId } = state;
  const container = $('time-slots-container');

  if (!selectedDate || !selectedThemeId) {
    container.innerHTML = `<div class="empty-state"><p>🗓</p><p>날짜와 테마를<br>먼저 선택해주세요.</p></div>`;
    return;
  }

  container.innerHTML = `<div class="time-grid">${[1,2,3,4].map(() =>
    `<div class="skeleton" style="height:60px"></div>`).join('')}</div>`;

  const times = await api.get(`/reservations/available-times?date=${selectedDate}&themeId=${selectedThemeId}`);
  state.selectedTimeId = null;
  state.selectedTimeLabel = null;

  if (!times || times.length === 0) {
    container.innerHTML = `<div class="empty-state"><p>⏰</p><p>예약 가능한<br>시간대가 없습니다.</p></div>`;
    return;
  }

  const grid = document.createElement('div');
  grid.className = 'time-grid';

  times.forEach(t => {
    const slot = document.createElement('div');
    slot.className = 'time-slot' + (t.reserved ? ' reserved' : '');
    slot.dataset.id = t.id;
    const timeLabel = formatTime(t.startAt);
    slot.innerHTML = `<div>${timeLabel}</div><div class="time-slot-badge">${t.reserved ? 'RESERVED' : 'AVAILABLE'}</div>`;
    if (!t.reserved) {
      slot.addEventListener('click', () => selectTime(t.id, timeLabel, slot));
    }
    grid.appendChild(slot);
  });

  container.innerHTML = '';
  container.appendChild(grid);
  updateCTAInfo();
}

function selectTime(id, label, el) {
  state.selectedTimeId = id;
  state.selectedTimeLabel = label;
  document.querySelectorAll('.time-slot:not(.reserved)').forEach(s => s.classList.remove('selected'));
  el.classList.add('selected');
  updateCTAInfo();
}

function formatTime(t) {
  if (!t) return '';
  if (Array.isArray(t)) return `${String(t[0]).padStart(2,'0')}:${String(t[1]).padStart(2,'0')}`;
  return t.substring(0, 5);
}

// ===== CTA =====
function updateCTAInfo() {
  const info = $('cta-info');
  const btn  = $('book-btn');
  const parts = [];
  if (state.selectedDate)      parts.push(`<strong>${state.selectedDate}</strong>`);
  if (state.selectedThemeName) parts.push(`<strong>${state.selectedThemeName}</strong>`);
  if (state.selectedTimeLabel) parts.push(`<strong>${state.selectedTimeLabel}</strong>`);

  info.innerHTML = parts.length > 0 ? parts.join(' &mdash; ') : '날짜, 테마, 시간을 선택하세요.';
  btn.disabled = !(state.selectedDate && state.selectedThemeId && state.selectedTimeId);
}

// ===== MODAL =====
function openBookingModal() {
  $('modal-date').textContent  = state.selectedDate;
  $('modal-theme').textContent = state.selectedThemeName;
  $('modal-time').textContent  = state.selectedTimeLabel;
  $('booking-name').value = '';
  $('booking-modal').classList.add('open');
  setTimeout(() => $('booking-name').focus(), 50);
}
function closeBookingModal() { $('booking-modal').classList.remove('open'); }

async function submitBooking() {
  const name = $('booking-name').value.trim();
  if (!name) { showToast('이름을 입력해주세요.', 'error'); return; }

  const btn = $('confirm-booking-btn');
  btn.disabled = true; btn.textContent = '예약 중...';
  try {
    await api.post('/reservations', {
      name, date: state.selectedDate,
      timeId: state.selectedTimeId, themeId: state.selectedThemeId,
    });
    closeBookingModal();
    showToast('예약이 완료되었습니다! 🎉', 'success');
    state.selectedTimeId = null; state.selectedTimeLabel = null;
    loadTimeSlots(); updateCTAInfo();
  } catch (e) {
    showToast('예약에 실패했습니다. ' + e.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = '예약하기';
  }
}

// ===== ADMIN =====
function initAdminPage() { loadAdminReservations(); }

function switchAdminPanel(panel) {
  document.querySelectorAll('.admin-panel').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.admin-tab').forEach(t => t.classList.remove('active'));
  $(`admin-panel-${panel}`).classList.add('active');
  document.querySelector(`[data-panel="${panel}"]`).classList.add('active');
  if (panel === 'reservations') loadAdminReservations();
  if (panel === 'times')        loadAdminTimes();
  if (panel === 'themes')       loadAdminThemes();
}

async function loadAdminReservations() {
  const tbody = $('admin-reservations-tbody');
  tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:24px;color:var(--text-muted)">불러오는 중...</td></tr>`;
  const data = await api.get('/admin/reservations');
  if (!data.length) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:24px;color:var(--text-muted)">예약 내역이 없습니다.</td></tr>`;
    return;
  }
  tbody.innerHTML = data.map(r => `
    <tr>
      <td>${r.id}</td><td>${r.name}</td><td>${r.date}</td>
      <td>${formatTime(r.time.startAt)}</td><td>${r.theme.name}</td>
      <td><button class="btn-delete" onclick="deleteReservation(${r.id})">삭제</button></td>
    </tr>
  `).join('');
}

async function deleteReservation(id) {
  if (!confirm('이 예약을 삭제하시겠습니까?')) return;
  await api.del(`/admin/reservations/${id}`);
  showToast('예약이 삭제되었습니다.', 'success');
  loadAdminReservations();
}

async function loadAdminTimes() {
  const tbody = $('admin-times-tbody');
  tbody.innerHTML = `<tr><td colspan="3" style="text-align:center;padding:24px;color:var(--text-muted)">불러오는 중...</td></tr>`;
  const data = await api.get('/times');
  if (!data.length) {
    tbody.innerHTML = `<tr><td colspan="3" style="text-align:center;padding:24px;color:var(--text-muted)">등록된 시간대가 없습니다.</td></tr>`;
    return;
  }
  tbody.innerHTML = data.map(t => `
    <tr>
      <td>${t.id}</td><td>${formatTime(t.startAt)}</td>
      <td><button class="btn-delete" onclick="deleteTime(${t.id})">삭제</button></td>
    </tr>
  `).join('');
}

async function addTime() {
  const startAt = $('new-time-start').value;
  if (!startAt) { showToast('시작 시간을 입력해주세요.', 'error'); return; }
  await api.post('/times', { startAt });
  showToast('시간대가 추가되었습니다.', 'success');
  $('new-time-start').value = '';
  loadAdminTimes();
}

async function deleteTime(id) {
  if (!confirm('이 시간대를 삭제하시겠습니까?')) return;
  await api.del(`/times/${id}`);
  showToast('시간대가 삭제되었습니다.', 'success');
  loadAdminTimes();
}

async function loadAdminThemes() {
  const tbody = $('admin-themes-tbody');
  tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;padding:24px;color:var(--text-muted)">불러오는 중...</td></tr>`;
  const data = await api.get('/themes/popular');
  if (!data.length) {
    tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;padding:24px;color:var(--text-muted)">등록된 테마가 없습니다.</td></tr>`;
    return;
  }
  tbody.innerHTML = data.map(t => `
    <tr>
      <td>${t.id}</td>
      <td><img class="theme-mini-thumb" src="${t.thumbnailUrl}" alt="${t.name}" onerror="this.style.display='none'">${t.name}</td>
      <td style="max-width:300px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${t.description}</td>
      <td><button class="btn-delete" onclick="deleteTheme(${t.id})">삭제</button></td>
    </tr>
  `).join('');
}

async function addTheme() {
  const name         = $('new-theme-name').value.trim();
  const description  = $('new-theme-desc').value.trim();
  const thumbnailUrl = $('new-theme-thumb').value.trim();
  if (!name || !description || !thumbnailUrl) { showToast('모든 항목을 입력해주세요.', 'error'); return; }
  await api.post('/themes', { name, description, thumbnailUrl });
  showToast('테마가 추가되었습니다.', 'success');
  $('new-theme-name').value = ''; $('new-theme-desc').value = ''; $('new-theme-thumb').value = '';
  loadAdminThemes();
}

async function deleteTheme(id) {
  if (!confirm('이 테마를 삭제하시겠습니까?')) return;
  await api.del(`/themes/${id}`);
  showToast('테마가 삭제되었습니다.', 'success');
  loadAdminThemes();
}

// ===== INIT =====
document.addEventListener('DOMContentLoaded', () => {
  // Logo → home
  $('logo-btn').addEventListener('click', () => switchPage('home-page'));

  // Nav tabs
  document.querySelectorAll('.nav-tab').forEach(btn => {
    btn.addEventListener('click', () => switchPage(btn.dataset.page));
  });

  // Home → 예약 CTA
  $('home-reserve-btn').addEventListener('click', () => switchPage('user-page'));

  // Theme detail modal
  $('theme-detail-close-btn').addEventListener('click', closeThemeDetail);
  $('theme-detail-cancel-btn').addEventListener('click', closeThemeDetail);
  $('theme-detail-book-btn').addEventListener('click', bookFromDetail);
  $('theme-detail-modal').addEventListener('click', e => {
    if (e.target === $('theme-detail-modal')) closeThemeDetail();
  });

  // Calendar nav
  $('cal-prev').addEventListener('click', () => {
    state.currentCalendarMonth--;
    if (state.currentCalendarMonth < 0) { state.currentCalendarMonth = 11; state.currentCalendarYear--; }
    renderCalendar();
  });
  $('cal-next').addEventListener('click', () => {
    state.currentCalendarMonth++;
    if (state.currentCalendarMonth > 11) { state.currentCalendarMonth = 0; state.currentCalendarYear++; }
    renderCalendar();
  });

  // Book button
  $('book-btn').addEventListener('click', openBookingModal);

  // Modal
  $('modal-close-btn').addEventListener('click', closeBookingModal);
  $('modal-cancel-btn').addEventListener('click', closeBookingModal);
  $('booking-modal').addEventListener('click', e => { if (e.target === $('booking-modal')) closeBookingModal(); });
  $('confirm-booking-btn').addEventListener('click', submitBooking);

  // Admin tabs
  document.querySelectorAll('.admin-tab').forEach(btn => {
    btn.addEventListener('click', () => switchAdminPanel(btn.dataset.panel));
  });
  $('btn-add-time').addEventListener('click', addTime);
  $('btn-add-theme').addEventListener('click', addTheme);

  // Load initial data
  loadPopularThemes();   // home page
  loadThemes();          // booking page (calendar + themes)
  updateCTAInfo();
});
