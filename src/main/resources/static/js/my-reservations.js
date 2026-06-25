const RESERVATION_API = '/reservations';
const MY_RESERVATIONS_API = '/reservations/mine';
const MY_WAITINGS_API = '/reservations/waiting/mine';
const TIMES_API = '/times';

let timesCache = [];
let currentName = '';

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('search-btn').addEventListener('click', searchReservations);
  document.getElementById('search-name').addEventListener('keydown', e => {
    if (e.key === 'Enter') searchReservations();
  });
  apiFetch(TIMES_API).then(data => { timesCache = data || []; }).catch(showError);
});

function searchReservations() {
  const name = document.getElementById('search-name').value.trim();
  if (!name) {
    alert('예약자 이름을 입력해주세요.');
    return;
  }
  currentName = name;
  document.getElementById('reservation-hint').classList.add('d-none');

  Promise.all([
    apiFetch(`${MY_RESERVATIONS_API}?name=${encodeURIComponent(name)}`),
    apiFetch(`${MY_WAITINGS_API}?name=${encodeURIComponent(name)}`)
  ])
    .then(([reservationData, waitingData]) => {
      renderReservations(reservationData.reservations || []);
      renderWaitings(waitingData.waitings || []);
    })
    .catch(showError);
}

function renderReservations(reservations) {
  const tbody = document.getElementById('reservation-table-body');
  const empty = document.getElementById('reservation-empty');
  tbody.innerHTML = '';

  if (reservations.length === 0) {
    empty.classList.remove('d-none');
    return;
  }
  empty.classList.add('d-none');

  reservations.forEach(r => tbody.appendChild(buildRow(r)));
}

function renderWaitings(waitings) {
  const tbody = document.getElementById('waiting-table-body');
  const empty = document.getElementById('waiting-empty');
  tbody.innerHTML = '';

  if (waitings.length === 0) {
    empty.classList.remove('d-none');
    return;
  }
  empty.classList.add('d-none');

  waitings.forEach(waiting => tbody.appendChild(buildWaitingRow(waiting)));
}

function buildRow(r) {
  const row = document.createElement('tr');
  row.dataset.id = r.id;

  row.appendChild(cell(r.id));
  row.appendChild(cell(r.themeName || '-'));
  row.appendChild(cell(r.date));
  row.appendChild(cell(formatTime(r.time)));
  row.appendChild(paymentStatusCell(r.paymentStatus));
  row.appendChild(cell(r.orderId || '-'));
  row.appendChild(cell(r.paymentKey || '-'));
  row.appendChild(cell(formatAmount(r.amount)));

  const actions = document.createElement('td');
  actions.className = 'actions-cell';
  if (r.paymentStatus === 'REQUIRES_CONFIRMATION') {
    actions.appendChild(button('결제 재확인', 'btn btn-success btn-sm', () => retryPayment(r)));
  }
  actions.appendChild(button('수정', 'btn btn-primary btn-sm', () => startEdit(row, r)));
  actions.appendChild(button('삭제', 'btn btn-danger btn-sm', () => deleteReservation(r.id, row)));
  row.appendChild(actions);

  return row;
}

function buildWaitingRow(waiting) {
  const row = document.createElement('tr');
  row.dataset.id = waiting.id;

  row.appendChild(cell(waiting.id));
  row.appendChild(cell(waiting.themeName || '-'));
  row.appendChild(cell(waiting.date));
  row.appendChild(cell(formatTime(waiting.time)));
  row.appendChild(cell(`${waiting.waitingNumber}번째`));

  const actions = document.createElement('td');
  actions.className = 'actions-cell';
  actions.appendChild(button('삭제', 'btn btn-danger btn-sm', () => deleteWaiting(waiting.id, row)));
  row.appendChild(actions);

  return row;
}

function startEdit(row, r) {
  const cells = row.children;
  cells[2].innerHTML = '';
  const dateInput = document.createElement('input');
  dateInput.type = 'date';
  dateInput.className = 'form-control';
  dateInput.value = r.date;
  cells[2].appendChild(dateInput);

  cells[3].innerHTML = '';
  const timeSelect = document.createElement('select');
  timeSelect.className = 'form-control';
  timesCache.forEach(t => {
    const opt = document.createElement('option');
    opt.value = t.id;
    opt.textContent = formatTime(t.startAt);
    if (t.startAt === r.time) opt.selected = true;
    timeSelect.appendChild(opt);
  });
  cells[3].appendChild(timeSelect);

  const actions = cells[8];
  actions.innerHTML = '';
  actions.className = 'actions-cell';
  actions.appendChild(button('저장', 'btn btn-success btn-sm', () => {
    saveEdit(row, r.id, dateInput.value, parseInt(timeSelect.value));
  }));
  actions.appendChild(button('취소', 'btn btn-ghost btn-sm', () => searchReservations()));
}

function saveEdit(row, id, date, timeId) {
  if (!date || !timeId) {
    alert('날짜와 시간을 선택해주세요.');
    return;
  }
  apiFetch(`/reservation/${id}`, {
    method: 'PATCH',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({ name: currentName, date, timeId })
  })
    .then(() => {
      alert('예약이 수정되었습니다.');
      searchReservations();
    })
    .catch(showError);
}

function deleteReservation(id, row) {
  if (!confirm('이 예약을 삭제하시겠습니까?')) return;
  apiFetch(`/reservations/${id}`, { method: 'DELETE' })
    .then(() => {
      row.remove();
      const tbody = document.getElementById('reservation-table-body');
      if (!tbody.children.length) {
        document.getElementById('reservation-empty').classList.remove('d-none');
      }
    })
    .catch(showError);
}

function retryPayment(reservation) {
  apiFetch('/payments/confirm', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({
      paymentKey: reservation.paymentKey,
      orderId: reservation.orderId,
      amount: reservation.amount
    })
  })
    .then(() => {
      alert('결제 결과가 확인되었습니다.');
      searchReservations();
    })
    .catch(showError);
}

function deleteWaiting(id, row) {
  if (!confirm('이 예약 대기를 삭제하시겠습니까?')) return;
  apiFetch(`/reservations/waiting/${id}`, { method: 'DELETE' })
    .then(() => {
      row.remove();
      const tbody = document.getElementById('waiting-table-body');
      if (!tbody.children.length) {
        document.getElementById('waiting-empty').classList.remove('d-none');
      }
    })
    .catch(showError);
}

function cell(text) {
  const td = document.createElement('td');
  td.textContent = text;
  return td;
}

function paymentStatusCell(status) {
  const labels = {
    PENDING: '결제 대기',
    CONFIRMED: '확정',
    FAILED: '실패',
    REQUIRES_CONFIRMATION: '확인 필요'
  };
  const td = document.createElement('td');
  const badge = document.createElement('span');
  badge.className = `payment-status payment-status-${(status || 'PENDING').toLowerCase()}`;
  badge.textContent = labels[status] || labels.PENDING;
  td.appendChild(badge);
  return td;
}

function formatAmount(amount) {
  if (amount === null || amount === undefined) return '-';
  return `${Number(amount).toLocaleString('ko-KR')}원`;
}

function button(label, className, handler) {
  const b = document.createElement('button');
  b.textContent = label;
  b.className = className;
  b.addEventListener('click', handler);
  return b;
}

function formatTime(value) {
  if (!value) return '';
  const [h, m] = value.split(':');
  return `${h}:${m}`;
}
