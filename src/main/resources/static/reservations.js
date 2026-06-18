// ===== reservations.js — 예약 페이지 전용 =====

const state = {
  userName: null,
  selectedDate: null,
  selectedThemeId: null,
  selectedThemeName: null,
  selectedThemePrice: null,
  selectedTimeId: null,
  selectedTimeLabel: null,
  themes: [],
  availableDates: [],
  currentCalendarYear: new Date().getFullYear(),
  currentCalendarMonth: new Date().getMonth(),
  reservationId: null,
};

const $ = id => document.getElementById(id);

// ===== Error Code Mapping =====
const ERROR_MESSAGES = {
  'TIME_ALREADY_RESERVED': '선택하신 시간은 이미 예약이 마감되었습니다. 다른 시간을 선택해 주세요.',
  'DATE_ALREADY_PASSED': '지난 날짜는 예약할 수 없습니다. 오늘 이후의 날짜를 선택해 주세요.',
  'INVALID_INPUT_VALUE': '입력 정보가 올바르지 않습니다. 날짜, 시간, 이름 형식을 다시 확인해 주세요.',
  'WAITING_LIST_NOT_REQUIRED': '해당 시간에 예약이 존재하지 않기 때문에 예약 대기 불가합니다.',
  'ALREADY_ON_WAITING_LIST': '이미 해당 조건의 예약 대기 신청이 존재합니다.',
  'TIME_ALREADY_PASSED': '이미 지난 시간입니다.',
  'DEFAULT': '알 수 없는 오류가 발생했습니다. 문제가 지속되면 관리자에게 문의해주세요.'
};

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
  async request(url, options) {
    const res = await fetch(url, options);

    if (!res.ok) {
      try {
        const errorData = await res.json();
        const errorCode = errorData.code || 'DEFAULT';
        const errorMessage = ERROR_MESSAGES[errorCode] || ERROR_MESSAGES['DEFAULT'];
        throw new Error(errorMessage);
      } catch (e) {
        // JSON 파싱 실패 혹은 다른 네트워크 오류
        throw new Error(e.message || ERROR_MESSAGES['DEFAULT']);
      }
    }

    if (res.status === 204) return null;
    return res.json();
  },

  get(url) {
    return this.request(url);
  },

  post(url, body) {
    return this.request(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
  },

  del(url) {
    return this.request(url, { method: 'DELETE' });
  }
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

// ===== Themes & Dates (초기 데이터 로드) =====
async function loadThemes() {
  try {
    const [datesData, themesData] = await Promise.all([
      api.get('/reservations/available-dates'),
      api.get('/themes')
    ]);

    state.availableDates = datesData.dates || datesData || [];
    state.themes = themesData || [];

    renderCalendar();
    renderThemes();
  } catch (e) {
    showToast(e.message, 'error');
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
  state.selectedThemePrice = theme.price;
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
      slot.className = 'time-slot' + (t.reserved ? ' reserved' : '');
      const label = formatTime(t.startAt);

      if (t.reserved) {
        slot.innerHTML = `
          <div>${label}</div>
          <button class="btn-secondary" style="font-size: 12px; padding: 4px 8px; margin-top: 4px; pointer-events: auto;">예약 대기</button>
        `;
        const btn = slot.querySelector('button');
        btn.addEventListener('click', (e) => {
          e.stopPropagation();
          openWaitingListModal(t.id, label);
        });
      } else {
        slot.innerHTML = `<div>${label}</div><div class="time-slot-badge">AVAILABLE</div>`;
        slot.addEventListener('click', () => selectTime(t.id, label, slot));
      }
      grid.appendChild(slot);
    });

    container.innerHTML = '';
    container.appendChild(grid);
    updateCTAInfo();
  } catch (e) {
    showToast(e.message, 'error');
    container.innerHTML = `<div class="empty-state"><p>⚠️</p><p>시간 정보를<br>불러오지 못했습니다.</p></div>`;
  }
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
  info.innerHTML = parts.length ? parts.join(' &mdash; ') : '날짜, 테마, 시간을 선택하세요.';
  btn.disabled = !(state.selectedDate && state.selectedThemeId && state.selectedTimeId);
}

// ===== Booking Modal =====
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
  if (!name) {
    showToast('예약자 이름을 입력해 주세요.', 'error');
    return;
  }

  state.userName = name;

  const btn = $('confirm-booking-btn');
  btn.disabled = true; btn.textContent = '예약 중...';
  try {
    const result = await api.post('/reservations', {
      name, date: state.selectedDate,
      timeId: state.selectedTimeId, themeId: state.selectedThemeId,
    });
    closeBookingModal();
    state.reservationId = result.id;
    state.selectedTimeId = result.time.id; state.selectedTimeLabel = formatTime(result.time.startAt);
    loadTimeSlots(); updateCTAInfo();
    openPrePaymentModal();
  } catch (e) {
    showToast(e.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = '예약하기';
  }
}

// ===== Pre-Payment Modal =====
function openPrePaymentModal() {
  $('pre-payment-modal-date').textContent  = state.selectedDate;
  $('pre-payment-modal-theme').textContent = state.selectedThemeName;
  $('pre-payment-modal-time').textContent  = state.selectedTimeLabel;
  $('pre-payment-booking-name').textContent = state.userName;
  $('pre-payment-price').textContent = state.selectedThemePrice;
  $('pre-payment-modal').classList.add('open');
}
async function closePrePaymentModal() {
  try {
    await api.delete(`/reservations/${state.reservationId}`, {
      name : state.userName
    });
  } catch (e) {
    showToast(e.message, 'error');
  } finally {
    $('pre-payment-modal').classList.remove('open');
  }
}

async function submitPrePayment() {
  const btn = $('confirm-pre-payment-btn');
  btn.disabled = true; btn.textContent = '결제 화면 불러오는 중...';

  try {
    const result = await api.post('/payments', {
      reservationId: state.reservationId,
      price: state.selectedThemePrice
    });
    closeBookingModal();
    showToast('예약이 접수되었습니다. 결제 페이지로 이동합니다.', 'success');
    state.selectedTimeId = null; state.selectedTimeLabel = null;

    setTimeout(() => {
      window.location.href = `/page/payments/checkout?orderId=${result.orderId}&reservationId=${result.reservationId}&amount=${result.amount}`;
    }, 1000);
  } catch (e) {
    showToast(e.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = '결제하기';
  }
}

// ===== Waiting List Modal =====
let waitingTimeId = null;

function openWaitingListModal(timeId, timeLabel) {
  waitingTimeId = timeId;
  $('waiting-modal-date').textContent  = state.selectedDate;
  $('waiting-modal-theme').textContent = state.selectedThemeName;
  $('waiting-modal-time').textContent  = timeLabel;
  $('waiting-name').value = '';
  $('waiting-list-modal').classList.add('open');
  setTimeout(() => $('waiting-name').focus(), 50);
}

function closeWaitingListModal() {
  $('waiting-list-modal').classList.remove('open');
  waitingTimeId = null;
}

function closeWaitingResultModal() {
  $('waiting-result-modal').classList.remove('open');
}

async function submitWaitingList() {
  const name = $('waiting-name').value.trim();
  if (!name) {
    showToast('예약 대기자 이름을 입력해 주세요.', 'error');
    return;
  }

  const btn = $('confirm-waiting-btn');
  btn.disabled = true; btn.textContent = '신청 중...';
  try {
    const result = await api.post('/waiting-list', {
      name, date: state.selectedDate,
      timeId: waitingTimeId, themeId: state.selectedThemeId,
    });
    closeWaitingListModal();
    $('waiting-result-message').innerHTML = `예약 대기가 완료되었습니다.<br><br>대기 순번은 <strong>${result.waitingOrder}번</strong> 입니다.`;
    $('waiting-result-modal').classList.add('open');
  } catch (e) {
    showToast(e.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = '예약 대기 신청';
  }
}

// ===== Init =====
document.addEventListener('DOMContentLoaded', () => {
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

  // Booking Modal
  $('book-btn').addEventListener('click', openBookingModal);
  $('modal-close-btn').addEventListener('click', closeBookingModal);
  $('modal-cancel-btn').addEventListener('click', closeBookingModal);
  $('booking-modal').addEventListener('click', e => { if (e.target === $('booking-modal')) closeBookingModal(); });
  $('confirm-booking-btn').addEventListener('click', submitBooking);

  // Pre-Payment Modal
  $('pre-payment-modal-close-btn').addEventListener('click', closePrePaymentModal);
  $('pre-payment-modal-cancel-btn').addEventListener('click', closePrePaymentModal);
  $('pre-payment-modal').addEventListener('click', e => { if (e.target === $('pre-payment-modal')) closePrePaymentModal(); });
  $('confirm-pre-payment-btn').addEventListener('click', submitPrePayment);

  // Waiting List Modal
  $('waiting-list-modal-close-btn').addEventListener('click', closeWaitingListModal);
  $('waiting-list-modal-cancel-btn').addEventListener('click', closeWaitingListModal);
  $('waiting-list-modal').addEventListener('click', e => { if (e.target === $('waiting-list-modal')) closeWaitingListModal(); });
  $('confirm-waiting-btn').addEventListener('click', submitWaitingList);

  // Waiting Result Modal
  $('waiting-result-modal-close-btn').addEventListener('click', closeWaitingResultModal);
  $('waiting-result-confirm-btn').addEventListener('click', closeWaitingResultModal);
  $('waiting-result-modal').addEventListener('click', e => { if (e.target === $('waiting-result-modal')) closeWaitingResultModal(); });

  loadThemes();
  updateCTAInfo();
});
