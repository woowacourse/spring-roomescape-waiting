// ===== my-reservations.js — 내 예약 페이지 전용 =====

const state = {
  currentName: '',
  availableDates: [],
  modifyingReservationId: null,
  modifyingThemeId: null,
  modifyingThemeName: null,
  
  selectedDate: null,
  selectedTimeId: null,
  selectedTimeLabel: null,
  
  currentCalendarYear: new Date().getFullYear(),
  currentCalendarMonth: new Date().getMonth(),

  themesMap: {},
  timesMap: {},
};

const $ = id => document.getElementById(id);

// ===== Error Code Mapping =====
const ERROR_MESSAGES = {
  'USER_NAME_NOT_MATCHED': '예약자 본인이 아니면 조회, 변경 또는 취소할 수 없습니다.',
  'TIME_ALREADY_RESERVED': '선택하신 시간은 이미 예약이 마감되었습니다. 다른 시간을 선택해 주세요.',
  'DATE_ALREADY_PASSED': '지난 날짜는 선택할 수 없습니다. 오늘 이후의 날짜를 선택해 주세요.',
  'TIME_ALREADY_PASSED': '지난 시간은 선택할 수 없습니다. 이후의 시간을 선택해 주세요.',
  'THEME_NOT_FOUND': '선택하신 테마를 찾을 수 없습니다.',
  'TIME_NOT_FOUND': '선택하신 시간을 찾을 수 없습니다.',
  'RESERVATION_NOT_FOUND': '해당 예약 내역을 찾을 수 없습니다. 이미 취소되었거나 정보가 다를 수 있습니다.',
  'TIME_HAS_RESERVATION': '해당 시간에 아직 완료되지 않은 예약이 있습니다.',
  'INVALID_INPUT_VALUE': '입력 정보가 올바르지 않습니다. 다시 확인해 주세요.',
  'PERSON_NAME_NULL_OR_BLANK': '조회할 예약자의 이름을 입력해 주세요.',
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
        throw new Error(e.message || ERROR_MESSAGES['DEFAULT']);
      }
    }

    if (res.status === 204) return null;
    return res.json();
  },

  get(url) {
    return this.request(url);
  },
  
  patch(url, body) {
    return this.request(url, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
  },

  del(url, body) {
    return this.request(url, { 
      method: 'DELETE',
      headers: body ? { 'Content-Type': 'application/json' } : undefined,
      body: body ? JSON.stringify(body) : undefined,
    });
  }
};

function paymentStatusBadge(orderStatus, orderId, amount) {
  if (orderStatus === 'COMPLETED') {
    return '<span style="color:#2b8a3e;font-weight:600">결제 확정</span>';
  }
  if (orderStatus === 'UNCERTAIN') {
    return `<span style="color:#e67700;font-weight:600">확인 필요</span>
            <button class="btn-ghost" style="padding:2px 8px;font-size:11px;margin-left:4px"
              onclick="retryConfirm('${orderId}', ${amount})">재시도</button>`;
  }
  return '<span style="color:#868e96">결제 대기</span>';
}

async function retryConfirm(orderId, amount) {
  const paymentKey = prompt('결제창에서 받은 paymentKey를 입력해주세요:');
  if (!paymentKey) return;

  try {
    const res = await fetch('/payments/confirm', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ paymentKey, orderId, amount }),
    });
    const data = await res.json();
    if (!res.ok) throw new Error(data.detail || data.message || '재시도 실패');
    showToast('결제가 확정되었습니다.', 'success');
    loadMyData();
  } catch (e) {
    showToast(e.message, 'error');
  }
}

function formatTime(t) {
  if (!t) return '';
  if (Array.isArray(t)) return `${String(t[0]).padStart(2,'0')}:${String(t[1]).padStart(2,'0')}`;
  return t.substring(0, 5);
}

// ===== Load Metadata (Themes & Times) =====
async function loadMetadata() {
  try {
    const [themes, times] = await Promise.all([
      api.get('/themes'),
      api.get('/times')
    ]);
    
    themes.forEach(t => state.themesMap[t.id] = t);
    times.forEach(t => state.timesMap[t.id] = t);
  } catch(e) {
    console.error("Failed to load metadata", e);
  }
}

// ===== Load My Reservations and Waiting Lists =====
async function loadMyData() {
  const name = $('search-name').value.trim();
  if (!name) {
    showToast(ERROR_MESSAGES['PERSON_NAME_NULL_OR_BLANK'], 'error');
    return;
  }
  state.currentName = name;
  
  const resTbody = $('my-reservations-tbody');
  const waitTbody = $('my-waiting-tbody');
  
  resTbody.innerHTML = `<tr><td colspan="9" style="text-align:center;padding:32px;color:var(--text-muted)">조회 중입니다...</td></tr>`;
  waitTbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:32px;color:var(--text-muted)">조회 중입니다...</td></tr>`;
  
  try {
    const [reservations, waitingLists] = await Promise.all([
      api.get(`/reservations?name=${encodeURIComponent(name)}`).catch(() => []),
      api.get(`/waiting-list?name=${encodeURIComponent(name)}`).catch(() => [])
    ]);

    // Render Reservations
    if (!reservations || reservations.length === 0) {
      resTbody.innerHTML = `<tr><td colspan="9" style="text-align:center;padding:32px;color:var(--text-muted)">예약 내역이 없습니다.</td></tr>`;
    } else {
      resTbody.innerHTML = reservations.map(r => {
        const paymentBadge = paymentStatusBadge(r.orderStatus, r.orderId, r.amount);
        return `
        <tr>
          <td>${r.id}</td>
          <td>${r.date}</td>
          <td>${formatTime(r.time.startAt)}</td>
          <td>${r.theme.name}</td>
          <td style="font-size:11px;color:var(--text-muted)">${r.orderId ?? '—'}</td>
          <td style="font-size:11px;color:var(--text-muted)">${r.paymentKey ?? '—'}</td>
          <td>${paymentBadge}</td>
          <td><button class="btn-ghost" style="padding:4px 12px;font-size:11px" onclick="openModifyModal(${r.id}, '${r.theme.name}', ${r.theme.id})">변경</button></td>
          <td><button class="btn-delete" onclick="deleteReservation(${r.id})">취소</button></td>
        </tr>
      `}).join('');
    }

    // Render Waiting Lists
    if (!waitingLists || waitingLists.length === 0) {
      waitTbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:32px;color:var(--text-muted)">대기 내역이 없습니다.</td></tr>`;
    } else {
      waitTbody.innerHTML = waitingLists.map(w => {
        const themeName = state.themesMap[w.themeId] ? state.themesMap[w.themeId].name : '알 수 없음';
        const timeStart = state.timesMap[w.timeId] ? formatTime(state.timesMap[w.timeId].startAt) : '알 수 없음';
        return `
        <tr>
          <td>${w.id}</td>
          <td>${w.date}</td>
          <td>${timeStart}</td>
          <td>${themeName}</td>
          <td>${w.waitingOrder}번째</td>
          <td><button class="btn-delete" onclick="deleteWaiting(${w.id})">취소</button></td>
        </tr>
      `}).join('');
    }

  } catch(e) {
    resTbody.innerHTML = `<tr><td colspan="9" style="text-align:center;padding:32px;color:var(--text-muted)">예약 내역을 불러오지 못했습니다.</td></tr>`;
    waitTbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:32px;color:var(--text-muted)">대기 내역을 불러오지 못했습니다.</td></tr>`;
    showToast(e.message, 'error');
  }
}

async function deleteReservation(id) {
  if (!confirm('정말 이 예약을 취소하시겠습니까? 취소된 예약은 복구할 수 없습니다.')) return;
  try {
    await api.del(`/reservations/${id}?name=${encodeURIComponent(state.currentName)}`);
    showToast('예약이 성공적으로 취소되었습니다.', 'success');
    loadMyData();
  } catch (e) {
    showToast(e.message, 'error');
  }
}

async function deleteWaiting(id) {
  if (!confirm('정말 대기를 취소하시겠습니까?')) return;
  try {
    await api.del(`/waiting-list/${id}`, { name: state.currentName });
    showToast('대기가 성공적으로 취소되었습니다.', 'success');
    loadMyData();
  } catch (e) {
    showToast(e.message, 'error');
  }
}

// ===== Modify Modal =====

async function openModifyModal(reservationId, themeName, themeId) {
  state.modifyingReservationId = reservationId;
  state.modifyingThemeName = themeName;
  state.modifyingThemeId = themeId;
  state.selectedDate = null;
  state.selectedTimeId = null;
  state.selectedTimeLabel = null;
  
  $('modify-modal-theme').textContent = themeName;
  updateModifyCTAInfo();
  
  try {
    const datesData = await api.get('/reservations/available-dates');
    state.availableDates = datesData.dates || datesData || [];
    renderModifyCalendar();
    loadModifyTimeSlots(); // will show empty state
    $('modify-modal').classList.add('open');
  } catch(e) {
    showToast('예약 정보를 불러오지 못했습니다. 잠시 후 다시 시도해주세요.', 'error');
  }
}

function closeModifyModal() { 
  $('modify-modal').classList.remove('open'); 
}

function updateModifyCTAInfo() {
  const btn = $('confirm-modify-btn');
  $('modify-modal-date').textContent = state.selectedDate || '—';
  $('modify-modal-time').textContent = state.selectedTimeLabel || '—';
  btn.disabled = !(state.selectedDate && state.selectedTimeId);
}

function toISODate(date) {
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;
}

function renderModifyCalendar() {
  const { currentCalendarYear: y, currentCalendarMonth: m, availableDates } = state;
  const monthNames = ['January','February','March','April','May','June',
                      'July','August','September','October','November','December'];
  $('modify-cal-month-label').textContent = `${monthNames[m]} ${y}`;

  const grid = $('modify-calendar-grid');
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
      el.addEventListener('click', () => selectModifyDate(dateStr, el));
    }
    if (date.getTime() === today.getTime()) el.classList.add('today');
    if (state.selectedDate === dateStr) el.classList.add('selected');
    grid.appendChild(el);
  }
}

function selectModifyDate(dateStr, el) {
  state.selectedDate = dateStr;
  document.querySelectorAll('#modify-calendar-grid .calendar-day').forEach(d => d.classList.remove('selected'));
  el.classList.add('selected');
  updateModifyCTAInfo();
  loadModifyTimeSlots();
}

async function loadModifyTimeSlots() {
  const { selectedDate, modifyingThemeId } = state;
  const container = $('modify-time-slots-container');

  if (!selectedDate || !modifyingThemeId) {
    container.innerHTML = `<div class="empty-state"><p>🗓</p><p>날짜를<br>먼저 선택해 주세요.</p></div>`;
    return;
  }

  container.innerHTML = `<div class="time-grid">${[1,2,3,4].map(() =>
    `<div class="skeleton" style="height:60px"></div>`).join('')}</div>`;

  try {
    const times = await api.get(`/reservations/available-times?date=${selectedDate}&themeId=${modifyingThemeId}`);
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
      slot.innerHTML = `<div>${label}</div><div class="time-slot-badge">${t.reserved ? 'RESERVED' : 'AVAILABLE'}</div>`;
      if (!t.reserved) slot.addEventListener('click', () => selectModifyTime(t.id, label, slot));
      grid.appendChild(slot);
    });

    container.innerHTML = '';
    container.appendChild(grid);
  } catch(e) {
    container.innerHTML = `<div class="empty-state"><p>⚠️</p><p>시간대를<br>불러오지 못했습니다.</p></div>`;
    showToast(e.message, 'error');
  }
  updateModifyCTAInfo();
}

function selectModifyTime(id, label, el) {
  state.selectedTimeId = id;
  state.selectedTimeLabel = label;
  document.querySelectorAll('#modify-time-slots-container .time-slot:not(.reserved)').forEach(s => s.classList.remove('selected'));
  el.classList.add('selected');
  updateModifyCTAInfo();
}

async function submitModifyBooking() {
  if (!state.selectedDate || !state.selectedTimeId) {
    showToast('변경할 날짜와 시간을 먼저 선택해 주세요.', 'error');
    return;
  }

  const btn = $('confirm-modify-btn');
  btn.disabled = true; btn.textContent = '변경 중...';
  try {
    const requestBody = {
      reservationId: state.modifyingReservationId,
      name: state.currentName,
      date: state.selectedDate,
      timeId: state.selectedTimeId,
      themeId: state.modifyingThemeId
    };
    await api.patch(`/reservations/${state.modifyingReservationId}`, requestBody);
    closeModifyModal();
    showToast('예약이 성공적으로 변경되었습니다! 🎉', 'success');
    loadMyData();
  } catch (e) {
    showToast(e.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = '변경하기';
  }
}

// ===== Tabs =====
function setupTabs() {
  document.querySelectorAll('.content-tab').forEach(btn => {
    btn.addEventListener('click', (e) => {
      document.querySelectorAll('.content-tab').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
      e.target.classList.add('active');
      $(`${e.target.dataset.tab}-tab`).classList.add('active');
    });
  });
}

// ===== Init =====
document.addEventListener('DOMContentLoaded', () => {
  loadMetadata();
  setupTabs();
  
  $('btn-search-reservation').addEventListener('click', loadMyData);
  
  // 엔터로 조회
  $('search-name').addEventListener('keyup', (e) => {
    if (e.key === 'Enter') loadMyData();
  });

  // Calendar nav
  $('modify-cal-prev').addEventListener('click', () => {
    state.currentCalendarMonth--;
    if (state.currentCalendarMonth < 0) { state.currentCalendarMonth = 11; state.currentCalendarYear--; }
    renderModifyCalendar();
  });
  $('modify-cal-next').addEventListener('click', () => {
    state.currentCalendarMonth++;
    if (state.currentCalendarMonth > 11) { state.currentCalendarMonth = 0; state.currentCalendarYear++; }
    renderModifyCalendar();
  });

  // Modal
  $('modify-modal-close-btn').addEventListener('click', closeModifyModal);
  $('modify-modal-cancel-btn').addEventListener('click', closeModifyModal);
  $('modify-modal').addEventListener('click', e => { if (e.target === $('modify-modal')) closeModifyModal(); });
  $('confirm-modify-btn').addEventListener('click', submitModifyBooking);
});