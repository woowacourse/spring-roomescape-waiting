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
    const text = await res.text();
    return text ? JSON.parse(text) : {};
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

function escStr(str) {
  return (str || '').replace(/'/g, "\\'");
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, ch => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
  }[ch]));
}

function formatWon(amount) {
  if (amount === null || amount === undefined) return '-';

  return `${Number(amount).toLocaleString('ko-KR')}원`;
}

function optionalText(value) {
  if (value === null || value === undefined || value === '') return '-';

  return escapeHtml(value);
}

function shortText(value) {
  if (!value) return '-';

  const text = String(value);
  const short = text.length > 14 ? `${text.slice(0, 14)}...` : text;
  return `<span class="mono-cell" title="${escapeHtml(text)}">${escapeHtml(short)}</span>`;
}

function paymentStatusClass(status) {
  if (status === 'COMPLETED') return 'confirmed';
  if (status === 'REQUIRES_CONFIRMATION') return 'requires-confirmation';
  if (status === 'READY') return 'ready';

  return 'default';
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
    tbody.innerHTML = `<tr><td colspan="9" style="text-align:center;padding:32px;color:var(--text-muted)">예약 내역이 없습니다.</td></tr>`;
    return;
  }

  tbody.innerHTML = reservations.map(r => `
    <tr>
      <td>${r.id}</td>
      <td>${r.date}</td>
      <td>${formatTime(r.time.startAt)}</td>
      <td>${escapeHtml(r.theme.name)}</td>
      <td>
        <span class="payment-status-badge ${paymentStatusClass(r.paymentStatus)}">
          ${optionalText(r.paymentStatusLabel)}
        </span>
      </td>
      <td>${shortText(r.orderId)}</td>
      <td>${shortText(r.paymentKey)}</td>
      <td>${formatWon(r.amount)}</td>
      <td>
        <div style="display:flex;gap:8px;justify-content:flex-end">
          ${renderRetryButton(r)}
          <button class="btn-delete" onclick="cancelReservation(${r.id})">취소</button>
        </div>
      </td>
    </tr>
  `).join('');
}

function renderRetryButton(reservation) {
  if (
    reservation.paymentStatus !== 'REQUIRES_CONFIRMATION'
    || !reservation.orderId
    || !reservation.paymentKey
    || !reservation.amount
  ) {
    return '';
  }

  return `
    <button
      class="btn-inline"
      onclick="retryPaymentConfirmation('${escStr(reservation.orderId)}','${escStr(reservation.paymentKey)}',${reservation.amount})">
      재확인
    </button>
  `;
}

async function retryPaymentConfirmation(orderId, paymentKey, amount) {
  try {
    const result = await api.post('/payments/confirm', { paymentKey, orderId, amount });
    if (result.paymentStatus === 'REQUIRES_CONFIRMATION') {
      showToast('아직 결제 승인 응답을 확인하지 못했습니다.', 'default');
    } else {
      showToast('결제 상태가 확인되었습니다.', 'success');
    }
    searchReservations();
  } catch (e) {
    showToast('결제 재확인에 실패했습니다. ' + e.message, 'error');
  }
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

// ===== Init =====
document.addEventListener('DOMContentLoaded', () => {
  // 탭 초기화
  initTabs();

  // 검색
  $('search-btn').addEventListener('click', searchReservations);
  $('search-name').addEventListener('keydown', e => { if (e.key === 'Enter') searchReservations(); });
  $('search-email').addEventListener('keydown', e => { if (e.key === 'Enter') searchReservations(); });

  // 대기 취소 모달
  $('cancel-waiting-close-btn').addEventListener('click', closeCancelWaitingModal);
  $('cancel-waiting-cancel-btn').addEventListener('click', closeCancelWaitingModal);
  $('cancel-waiting-modal').addEventListener('click', e => { if (e.target === $('cancel-waiting-modal')) closeCancelWaitingModal(); });
  $('confirm-cancel-waiting-btn').addEventListener('click', submitCancelWaiting);
  $('cancel-waiting-name').addEventListener('keydown', e => { if (e.key === 'Enter') submitCancelWaiting(); });
  $('cancel-waiting-email').addEventListener('keydown', e => { if (e.key === 'Enter') submitCancelWaiting(); });
});
