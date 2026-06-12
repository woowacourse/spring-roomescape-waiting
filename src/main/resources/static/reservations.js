// ===== reservations.js — 신규 예약 페이지 전용 =====

const state = {
  selectedDate: null,
  selectedThemeId: null,
  selectedThemeName: null,
  selectedTimeId: null,
  selectedTimeLabel: null,
  waitingMode: false,
  themes: [],
  availableDates: [],
  currentCalendarYear: new Date().getFullYear(),
  currentCalendarMonth: new Date().getMonth(),
};

const $ = id => document.getElementById(id);

// ===== Toast =====
function showToast(msg, type = 'default') {
  const el = document.createElement('div');
  el.className = `toast ${type}`;
  el.textContent = msg;
  $('toast-container').appendChild(el);
  setTimeout(() => el.remove(), 3100);
}

// ===== API =====
async function extractErrorMessage(res) {
  try {
    const text = await res.text();
    if (!text) return `요청이 실패했습니다. (${res.status})`;
    try {
      const json = JSON.parse(text);
      return json.message || json.detail || json.error || json.title
        || text || `요청이 실패했습니다. (${res.status})`;
    } catch {
      return text || `요청이 실패했습니다. (${res.status})`;
    }
  } catch {
    return `요청이 실패했습니다. (${res.status})`;
  }
}

const api = {
  async get(url) {
    const res = await fetch(url);
    if (!res.ok) {
      const msg = await extractErrorMessage(res);
      throw new Error(msg);
    }
    return res.json();
  },
  async post(url, body) {
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    if (!res.ok) {
      const msg = await extractErrorMessage(res);
      throw new Error(msg);
    }
    // 201 Created 는 body가 있을 수도 없음
    const text = await res.text();
    return text ? JSON.parse(text) : {};
  },
};

// ===== Calendar =====
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
    const el = document.createElement('div');
    el.className = 'calendar-day';
    el.textContent = d;
    el.dataset.date = dateStr;

    if (date < today || !availableSet.has(dateStr)) {
      el.classList.add('disabled');
    } else {
      el.classList.add('available');
      el.addEventListener('click', () => selectDate(dateStr, el));
    }
    if (date.getTime() === today.getTime()) el.classList.add('today');
    if (state.selectedDate === dateStr) el.classList.add('selected');
    grid.appendChild(el);
  }
}

function selectDate(dateStr, el) {
  state.selectedDate = dateStr;
  document.querySelectorAll('.calendar-day').forEach(d => d.classList.remove('selected'));
  el.classList.add('selected');
  updateCTAInfo();
  loadTimeSlots();
}

function toISODate(date) {
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;
}

// ===== Themes =====
async function loadThemes() {
  try {
    const opts = await api.get('/reservations/date-and-theme');
    state.availableDates = opts.dates || [];
    state.themes = opts.themes || [];
    renderCalendar();
    renderThemes();
  } catch (e) {
    showToast('테마 정보를 불러오지 못했습니다. ' + e.message, 'error');
  }
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
  card.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  updateCTAInfo();
  loadTimeSlots();
}

// ===== Time Slots =====
async function loadTimeSlots() {
  const { selectedDate, selectedThemeId } = state;
  const container = $('time-slots-container');

  if (!selectedDate || !selectedThemeId) {
    container.innerHTML = `<div class="empty-state"><p>🗓</p><p>날짜와 테마를<br>먼저 선택해주세요.</p></div>`;
    return;
  }

  container.innerHTML = `<div class="time-grid">${[1,2,3,4].map(() =>
    `<div class="skeleton" style="height:60px"></div>`).join('')}</div>`;

  try {
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
      const label = formatTime(t.startAt);
      if (isPastTimeSlot(selectedDate, t.startAt)) {
        slot.className = 'time-slot unavailable';
        slot.innerHTML = `<div>${label}</div><div class="time-slot-badge">CLOSED</div>`;
      } else if (t.reserved) {
        // 예약된 슬롯: 대기 신청 가능
        slot.className = 'time-slot reserved waitingable';
        slot.innerHTML = `<div>${label}</div><div class="time-slot-badge">WAITING</div>`;
        slot.style.cursor = 'pointer';
        slot.style.color = 'var(--text-secondary)';
        slot.addEventListener('click', () => selectWaitingTime(t.id, label, slot));
      } else {
        slot.className = 'time-slot';
        slot.innerHTML = `<div>${label}</div><div class="time-slot-badge">AVAILABLE</div>`;
        slot.addEventListener('click', () => selectTime(t.id, label, slot));
      }
      grid.appendChild(slot);
    });

    container.innerHTML = '';
    container.appendChild(grid);
    updateCTAInfo();
  } catch (e) {
    container.innerHTML = `<div class="empty-state"><p>⚠️</p><p>시간대를 불러오지<br>못했습니다.</p></div>`;
    showToast('시간대 조회 실패: ' + e.message, 'error');
  }
}

function selectTime(id, label, el) {
  state.selectedTimeId = id;
  state.selectedTimeLabel = label;
  state.waitingMode = false;
  document.querySelectorAll('.time-slot:not(.reserved)').forEach(s => s.classList.remove('selected'));
  document.querySelectorAll('.time-slot.waitingable').forEach(s => s.classList.remove('selected'));
  el.classList.add('selected');
  updateCTAInfo();
}

function selectWaitingTime(id, label, el) {
  state.selectedTimeId = id;
  state.selectedTimeLabel = label;
  state.waitingMode = true;
  document.querySelectorAll('.time-slot:not(.reserved)').forEach(s => s.classList.remove('selected'));
  document.querySelectorAll('.time-slot.waitingable').forEach(s => s.classList.remove('selected'));
  el.classList.add('selected');
  updateCTAInfo();
}

function formatTime(t) {
  if (!t) return '';
  if (Array.isArray(t)) return `${String(t[0]).padStart(2,'0')}:${String(t[1]).padStart(2,'0')}`;
  return t.substring(0, 5);
}

function isPastTimeSlot(date, startAt) {
  const [year, month, day] = date.split('-').map(Number);
  const [hours, minutes, seconds = 0] = Array.isArray(startAt)
    ? startAt
    : startAt.split(':').map(Number);
  const slotStartAt = new Date(year, month - 1, day, hours, minutes, seconds);

  return slotStartAt < new Date();
}

// ===== CTA =====
function updateCTAInfo() {
  const info = $('cta-info');
  const btn  = $('book-btn');
  const parts = [];
  if (state.selectedDate)      parts.push(`<strong>${state.selectedDate}</strong>`);
  if (state.selectedThemeName) parts.push(`<strong>${state.selectedThemeName}</strong>`);
  if (state.selectedTimeLabel) parts.push(`<strong>${state.selectedTimeLabel}</strong>`);
  info.innerHTML = parts.length ? parts.join(' &mdash; ') : '날짜, 테마, 시간을 선택하세요.';

  const allSelected = state.selectedDate && state.selectedThemeId && state.selectedTimeId;
  btn.disabled = !allSelected;
  if (allSelected && state.waitingMode) {
    btn.textContent = '대기 신청';
    btn.style.background = 'var(--accent)';
    btn.style.color = 'var(--bg)';
  } else {
    btn.textContent = '예약하기';
    btn.style.background = '';
    btn.style.color = '';
  }
}

// ===== Booking Modal (예약) =====
function openModal() {
  $('modal-date').textContent  = state.selectedDate;
  $('modal-theme').textContent = state.selectedThemeName;
  $('modal-time').textContent  = state.selectedTimeLabel;
  $('booking-name').value = '';
  $('booking-email').value = '';
  $('modal-title-text').textContent = '예약 확인';
  $('confirm-booking-btn').textContent = '예약하기';
  $('booking-modal').classList.add('open');
  setTimeout(() => $('booking-name').focus(), 50);
}

// ===== Waiting Modal (대기 신청) =====
function openWaitingModal() {
  $('modal-date').textContent  = state.selectedDate;
  $('modal-theme').textContent = state.selectedThemeName;
  $('modal-time').textContent  = state.selectedTimeLabel;
  $('booking-name').value = '';
  $('booking-email').value = '';
  $('modal-title-text').textContent = '대기 신청';
  $('confirm-booking-btn').textContent = '대기 신청하기';
  $('booking-modal').classList.add('open');
  setTimeout(() => $('booking-name').focus(), 50);
}

function closeModal() { $('booking-modal').classList.remove('open'); }

async function submitBooking() {
  const name = $('booking-name').value.trim();
  const email = $('booking-email').value.trim();
  if (!name) { showToast('이름을 입력해주세요.', 'error'); return; }
  if (!email) { showToast('이메일을 입력해주세요.', 'error'); return; }

  const btn = $('confirm-booking-btn');
  const isWaiting = state.waitingMode;

  btn.disabled = true;
  btn.textContent = isWaiting ? '대기 신청 중...' : '예약 중...';

  try {
    if (isWaiting) {
      // POST /waitings
      await api.post('/waitings', {
        name,
        email,
        date: state.selectedDate,
        timeId: state.selectedTimeId,
        themeId: state.selectedThemeId,
      });
      closeModal();
      showToast('대기가 신청되었습니다! 📄', 'success');
    } else {
      // POST /reservations
      await api.post('/reservations', {
        name, email, date: state.selectedDate,
        timeId: state.selectedTimeId, themeId: state.selectedThemeId,
      });
      closeModal();
      showToast('예약이 완료되었습니다! 🎉', 'success');
    }
    state.selectedTimeId = null; state.selectedTimeLabel = null; state.waitingMode = false;
    loadTimeSlots(); updateCTAInfo();
  } catch (e) {
    showToast((isWaiting ? '대기 신청' : '예약') + '에 실패했습니다. ' + e.message, 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = isWaiting ? '대기 신청하기' : '예약하기';
  }
}

// ===== Init =====
document.addEventListener('DOMContentLoaded', () => {
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

  $('book-btn').addEventListener('click', () => {
    if (state.waitingMode) openWaitingModal();
    else openModal();
  });
  $('modal-close-btn').addEventListener('click', closeModal);
  $('modal-cancel-btn').addEventListener('click', closeModal);
  $('booking-modal').addEventListener('click', e => { if (e.target === $('booking-modal')) closeModal(); });
  $('confirm-booking-btn').addEventListener('click', submitBooking);

  loadThemes();
  updateCTAInfo();
});
