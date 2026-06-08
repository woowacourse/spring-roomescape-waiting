// ===== my-reservations.js — 내 예약 페이지 전용 =====

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
  async put(url, body) {
    const res = await fetch(url, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    if (!res.ok) {
      const msg = await extractErrorMessage(res);
      throw new Error(msg);
    }
    return res.json();
  },
  async del(url) {
    const res = await fetch(url, { method: 'DELETE' });
    if (!res.ok) {
      const msg = await extractErrorMessage(res);
      throw new Error(msg);
    }
    return res;
  },
};

// ===== 공용 유틸 =====
function formatTime(t) {
  if (!t) return '';
  if (Array.isArray(t)) return `${String(t[0]).padStart(2,'0')}:${String(t[1]).padStart(2,'0')}`;
  return t.substring(0, 5);
}

function toISODate(date) {
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;
}

function escStr(str) {
  return (str || '').replace(/'/g, "\\'");
}

// ===== 탭 전환 =====
function initTabs() {
  document.querySelectorAll('.my-res-tab').forEach(tab => {
    tab.addEventListener('click', () => {
      document.querySelectorAll('.my-res-tab').forEach(t => t.classList.remove('active'));
      document.querySelectorAll('.my-res-panel').forEach(p => p.classList.remove('active'));
      tab.classList.add('active');
      $( tab.dataset.target ).classList.add('active');
    });
  });
}

function switchToTab(tabId) {
  document.querySelectorAll('.my-res-tab').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.my-res-panel').forEach(p => p.classList.remove('active'));
  $(tabId).classList.add('active');
  $( $(tabId).dataset.target ).classList.add('active');
}

// ===== 예약 + 대기 조회 =====
async function searchReservations() {
  const name = $('search-name').value.trim();
  const email = $('search-email').value.trim();
  if (!name) { showToast('예약자 이름을 입력해주세요.', 'error'); return; }
  if (!email) { showToast('예약자 이메일을 입력해주세요.', 'error'); return; }

  const btn = $('search-btn');
  btn.disabled = true; btn.textContent = '조회 중...';

  try {
    // API: GET /reservations?customer-name={name}&customer-email={email}
    // 응답: { reservations: [...], waitings: [...] }
    const data = await api.get(
      `/reservations?customer-name=${encodeURIComponent(name)}&customer-email=${encodeURIComponent(email)}`
    );
    const reservations = data.reservations || [];
    const waitings = data.waitings || [];

    renderReservationList(reservations);
    renderWaitingList(waitings);
    updateTabCountLabel(reservations.length, waitings.length);

    $('search-results').style.display = 'block';
    // 기본으로 예약 탭 표시
    switchToTab('tab-reservations');
  } catch (e) {
    showToast('조회에 실패했습니다. ' + e.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = '조회';
  }
}

function updateTabCountLabel(resCount, waitCount) {
  $('tab-count-label').textContent = `예약 ${resCount}건 · 대기 ${waitCount}건`;
}

// ===== 예약 목록 렌더링 =====
function renderReservationList(reservations) {
  const tbody = $('my-reservations-tbody');

  if (!reservations || reservations.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align:center;padding:32px;color:var(--text-muted)">예약 내역이 없습니다.</td></tr>`;
    return;
  }

  tbody.innerHTML = reservations.map(r => `
    <tr>
      <td>${r.id}</td>
      <td>${r.date}</td>
      <td>${formatTime(r.time.startAt)}</td>
      <td>${r.theme.name}</td>
      <td>
        <div style="display:flex;gap:8px;justify-content:flex-end">
          <button class="btn-edit"
            onclick="openEditModal(${r.id},'${escStr(r.name)}','${r.date}',${r.time.id},'${escStr(r.theme.name)}',${r.theme.id})">
            변경
          </button>
          <button class="btn-delete" onclick="cancelReservation(${r.id})">취소</button>
        </div>
      </td>
    </tr>
  `).join('');
}

// ===== 대기 목록 렌더링 =====
function renderWaitingList(waitings) {
  const tbody = $('my-waitings-tbody');

  if (!waitings || waitings.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:32px;color:var(--text-muted)">대기 내역이 없습니다.</td></tr>`;
    return;
  }

  tbody.innerHTML = waitings.map((w, idx) => `
    <tr>
      <td>${idx + 1}</td>
      <td>${w.date}</td>
      <td>${formatTime(w.startAt)}</td>
      <td>${w.theme.name}</td>
      <td>
        <span class="waiting-rank-badge">${w.rank}번째</span>
      </td>
      <td>
        <button class="btn-delete"
          onclick="openCancelWaitingModal(${w.id},'${escStr(w.date)}','${escStr(formatTime(w.startAt))}','${escStr(w.theme.name)}',${w.rank})">
          취소
        </button>
      </td>
    </tr>
  `).join('');
}

// ===== 예약 취소 =====
async function cancelReservation(id) {
  if (!confirm('이 예약을 취소하시겠습니까?')) return;
  const name = $('search-name').value.trim();
  const email = $('search-email').value.trim();
  if (!name || !email) { showToast('예약자 이름과 이메일을 입력해주세요.', 'error'); return; }

  try {
    await api.del(
      `/reservations/${id}?customer-name=${encodeURIComponent(name)}&customer-email=${encodeURIComponent(email)}`
    );
    showToast('예약이 취소되었습니다.', 'success');
    if (name) searchReservations();
  } catch (e) {
    showToast('취소에 실패했습니다. ' + e.message, 'error');
  }
}

// ===== 대기 취소 모달 =====
let pendingCancelWaitingId = null;

function openCancelWaitingModal(waitingId, date, time, themeName, rank) {
  pendingCancelWaitingId = waitingId;
  $('cwm-date').textContent   = date;
  $('cwm-theme').textContent  = themeName;
  $('cwm-time').textContent   = time;
  $('cwm-rank').textContent   = `${rank}번째`;
  $('cancel-waiting-name').value = $('search-name').value.trim();
  $('cancel-waiting-email').value = $('search-email').value.trim();
  $('cancel-waiting-modal').classList.add('open');
  setTimeout(() => $('cancel-waiting-name').focus(), 50);
}

function closeCancelWaitingModal() {
  $('cancel-waiting-modal').classList.remove('open');
  pendingCancelWaitingId = null;
}

async function submitCancelWaiting() {
  const name = $('cancel-waiting-name').value.trim();
  const email = $('cancel-waiting-email').value.trim();
  if (!name) { showToast('이름을 입력해주세요.', 'error'); return; }
  if (!email) { showToast('이메일을 입력해주세요.', 'error'); return; }

  const btn = $('confirm-cancel-waiting-btn');
  btn.disabled = true; btn.textContent = '취소 중...';

  try {
    // DELETE /waitings/{id}?customer-name={name}&customer-email={email}
    await api.del(
      `/waitings/${pendingCancelWaitingId}?customer-name=${encodeURIComponent(name)}&customer-email=${encodeURIComponent(email)}`
    );
    closeCancelWaitingModal();
    showToast('대기가 취소되었습니다.', 'success');
    const searchName = $('search-name').value.trim();
    const searchEmail = $('search-email').value.trim();
    if (searchName && searchEmail) searchReservations();
  } catch (e) {
    showToast('취소에 실패했습니다. ' + e.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = '대기 취소';
  }
}

// ===================================================================
//  예약 변경 모달 — 달력 + 시간 슬롯
// ===================================================================

// 변경 모달 전용 상태
const editState = {
  reservationId: null,
  themeId: null,
  currentTimeId: null,        // 현재 예약 시간 id (CURRENT 표시용)
  availableDates: [],          // /reservations/date-and-theme 에서 받아온 날짜 목록
  selectedDate: null,
  selectedTimeId: null,
  calYear: new Date().getFullYear(),
  calMonth: new Date().getMonth(),
};

async function openEditModal(reservationId, name, date, timeId, themeName, themeId) {
  editState.reservationId = reservationId;
  editState.themeId = themeId;
  editState.currentTimeId = timeId;
  editState.selectedDate = null;
  editState.selectedTimeId = null;

  $('edit-modal-name').textContent = name;
  $('edit-modal-theme').textContent = themeName;
  $('confirm-edit-btn').disabled = true;
  $('edit-time-slots-wrap').innerHTML = `<div class="empty-state edit-empty-state">날짜를 먼저 선택하세요.</div>`;

  // 예약 가능 날짜 로드
  try {
    const opts = await api.get('/reservations/date-and-theme');
    editState.availableDates = opts.dates || [];
  } catch (e) {
    editState.availableDates = [];
    showToast('예약 가능 날짜 조회 실패: ' + e.message, 'error');
  }

  // 달력을 현재 예약 날짜가 있는 달로 맞춤
  const [y, m] = date.split('-').map(Number);
  editState.calYear = y;
  editState.calMonth = m - 1;
  renderEditCalendar();

  $('edit-modal').classList.add('open');
}

function closeEditModal() {
  $('edit-modal').classList.remove('open');
}

// ===== 변경 모달 달력 =====
function renderEditCalendar() {
  const { calYear: y, calMonth: m, availableDates, selectedDate } = editState;

  const monthNames = ['January','February','March','April','May','June',
                      'July','August','September','October','November','December'];
  $('edit-cal-month-label').textContent = `${monthNames[m]} ${y}`;

  const grid = $('edit-calendar-grid');
  grid.querySelectorAll('.calendar-day').forEach(el => el.remove());

  const firstDay  = new Date(y, m, 1).getDay();
  const daysInMonth = new Date(y, m + 1, 0).getDate();
  const today = new Date(); today.setHours(0, 0, 0, 0);
  const availableSet = new Set(availableDates);

  // 빈 칸
  for (let i = 0; i < firstDay; i++) {
    const el = document.createElement('div');
    el.className = 'calendar-day empty';
    grid.appendChild(el);
  }

  // 날짜 셀
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
      el.addEventListener('click', () => selectEditDate(dateStr, el));
    }
    if (date.getTime() === today.getTime()) el.classList.add('today');
    if (selectedDate === dateStr) el.classList.add('selected');
    grid.appendChild(el);
  }
}

function selectEditDate(dateStr, el) {
  editState.selectedDate = dateStr;
  editState.selectedTimeId = null;
  $('confirm-edit-btn').disabled = true;

  document.querySelectorAll('#edit-calendar-grid .calendar-day').forEach(d => d.classList.remove('selected'));
  el.classList.add('selected');

  loadEditTimeSlots(dateStr);
}

// ===== 변경 모달 시간 슬롯 =====
async function loadEditTimeSlots(date) {
  const wrap = $('edit-time-slots-wrap');
  wrap.innerHTML = [1,2,3,4].map(() => `<div class="skeleton" style="height:56px;border-radius:7px"></div>`).join('');

  try {
    const times = await api.get(
      `/reservations/available-times?date=${date}&themeId=${editState.themeId}`
    );

    if (!times || times.length === 0) {
      wrap.innerHTML = `<div class="empty-state edit-empty-state"><p>⏰</p><p>예약 가능한 시간이 없습니다.</p></div>`;
      return;
    }

    const grid = document.createElement('div');
    grid.className = 'time-grid';

    times.forEach(t => {
      const label = formatTime(t.startAt);
      const slot = document.createElement('div');
      const isCurrent = t.id === editState.currentTimeId;

      // 현재 내 예약 시간: reserved=true여도 선택 가능
      const isReservedByOther = t.reserved && !isCurrent;

      slot.className = 'time-slot' + (isReservedByOther ? ' reserved' : '');
      slot.innerHTML = `
        <div>${label}</div>
        <div class="time-slot-badge">${isReservedByOther ? 'RESERVED' : isCurrent ? 'CURRENT' : 'AVAILABLE'}</div>
      `;

      if (!isReservedByOther) {
        slot.addEventListener('click', () => selectEditTime(t.id, slot));
      }
      grid.appendChild(slot);
    });

    wrap.innerHTML = '';
    wrap.appendChild(grid);
  } catch (e) {
    wrap.innerHTML = `<div class="empty-state edit-empty-state">시간대를 불러오지 못했습니다.</div>`;
    showToast('시간대 조회 실패: ' + e.message, 'error');
  }
}

function selectEditTime(id, el) {
  editState.selectedTimeId = id;
  document.querySelectorAll('#edit-time-slots-wrap .time-slot:not(.reserved)').forEach(s => s.classList.remove('selected'));
  el.classList.add('selected');
  $('confirm-edit-btn').disabled = false;
}

// ===== 변경 제출 =====
async function submitEdit() {
  if (!editState.selectedDate) { showToast('날짜를 선택해주세요.', 'error'); return; }
  if (!editState.selectedTimeId) { showToast('시간을 선택해주세요.', 'error'); return; }

  const btn = $('confirm-edit-btn');
  btn.disabled = true; btn.textContent = '변경 중...';

  try {
    const name = $('search-name').value.trim();
    const email = $('search-email').value.trim();
    if (!name || !email) { showToast('예약자 이름과 이메일을 입력해주세요.', 'error'); return; }

    await api.put(`/reservations/${editState.reservationId}?customer-name=${encodeURIComponent(name)}&customer-email=${encodeURIComponent(email)}`, {
      date: editState.selectedDate,
      timeId: editState.selectedTimeId,
    });
    closeEditModal();
    showToast('예약이 변경되었습니다! ✅', 'success');
    if (name) searchReservations();
  } catch (e) {
    showToast('변경에 실패했습니다. ' + e.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = '변경하기';
  }
}

// ===== Init =====
document.addEventListener('DOMContentLoaded', () => {
  // 탭 초기화
  initTabs();

  // 검색
  $('search-btn').addEventListener('click', searchReservations);
  $('search-name').addEventListener('keydown', e => { if (e.key === 'Enter') searchReservations(); });
  $('search-email').addEventListener('keydown', e => { if (e.key === 'Enter') searchReservations(); });

  // 변경 모달 달력 네비게이션
  $('edit-cal-prev').addEventListener('click', () => {
    editState.calMonth--;
    if (editState.calMonth < 0) { editState.calMonth = 11; editState.calYear--; }
    renderEditCalendar();
  });
  $('edit-cal-next').addEventListener('click', () => {
    editState.calMonth++;
    if (editState.calMonth > 11) { editState.calMonth = 0; editState.calYear++; }
    renderEditCalendar();
  });

  // 변경 모달 닫기
  $('edit-modal-close-btn').addEventListener('click', closeEditModal);
  $('edit-modal-cancel-btn').addEventListener('click', closeEditModal);
  $('edit-modal').addEventListener('click', e => { if (e.target === $('edit-modal')) closeEditModal(); });

  // 변경 제출
  $('confirm-edit-btn').addEventListener('click', submitEdit);

  // 대기 취소 모달
  $('cancel-waiting-close-btn').addEventListener('click', closeCancelWaitingModal);
  $('cancel-waiting-cancel-btn').addEventListener('click', closeCancelWaitingModal);
  $('cancel-waiting-modal').addEventListener('click', e => { if (e.target === $('cancel-waiting-modal')) closeCancelWaitingModal(); });
  $('confirm-cancel-waiting-btn').addEventListener('click', submitCancelWaiting);
  $('cancel-waiting-name').addEventListener('keydown', e => { if (e.key === 'Enter') submitCancelWaiting(); });
  $('cancel-waiting-email').addEventListener('keydown', e => { if (e.key === 'Enter') submitCancelWaiting(); });
});
