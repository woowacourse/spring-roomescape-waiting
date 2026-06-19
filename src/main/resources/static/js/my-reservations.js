let editingId = null;
let editingThemeId = null;
let datePicker = null;

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('edit-confirm').addEventListener('click', submitEdit);
  document.getElementById('edit-cancel').addEventListener('click', closeModal);
  document.getElementById('modal-overlay').addEventListener('click', e => {
    if (e.target === document.getElementById('modal-overlay')) closeModal();
  });

  datePicker = flatpickr('#edit-date', {
    dateFormat: 'Y-m-d',
    minDate: 'today',
    locale: 'ko',
    onChange: (_, dateStr) => {
      if (dateStr && editingThemeId) loadAvailableTimes(dateStr, editingThemeId);
    }
  });

  loadMyReservations();
});

function loadMyReservations() {
  Promise.all([
    fetch('/bookings').then(r => {
      if (r.status === 401) {
        showToast('로그인이 필요합니다.');
        setTimeout(() => { window.location.href = '/login'; }, 1000);
        return [];
      }
      return r.json();
    }),
    fetch('/waitings').then(r => {
      if (r.status === 401) return [];
      return r.json();
    })
  ])
    .then(([reservations, waitings]) => renderAll(reservations, waitings))
    .catch(() => showToast('조회에 실패했습니다.'));
}

function renderAll(reservations, waitings) {
  const tbody = document.getElementById('reservation-tbody');
  const empty = document.getElementById('reservation-empty');
  tbody.innerHTML = '';

  const rows = [
    ...reservations.map(r => ({type: 'reservation', data: r})),
    ...waitings.map(w => ({type: 'waiting', data: w}))
  ];

  rows.sort((a, b) => {
    const dateA = a.data.date;
    const dateB = b.data.date;
    if (dateA !== dateB) return dateB > dateA ? 1 : -1;
    const timeA = a.type === 'reservation' ? a.data.time.startAt : a.data.startAt;
    const timeB = b.type === 'reservation' ? b.data.time.startAt : b.data.startAt;
    return timeB > timeA ? 1 : -1;
  });

  if (rows.length === 0) {
    empty.classList.remove('d-none');
    return;
  }
  empty.classList.add('d-none');

  rows.forEach(({type, data}) => {
    const tr = document.createElement('tr');
    if (type === 'reservation' && data.paymentStatus === 'UNKNOWN') {
      tr.innerHTML = `
        <td>${data.date}</td>
        <td>${data.themeName}</td>
        <td>${formatTime(data.time.startAt)}</td>
        <td><span class="status-badge status-badge--waiting">확인 필요</span></td>
        <td>${paymentCell(data)}</td>
        <td style="text-align:right;">
          <button class="btn btn-primary" style="font-size:0.82rem;padding:6px 12px;margin-right:4px;"
            onclick="recheckPayment('${data.paymentKey}', '${data.orderId}', ${data.paymentAmount})">결과 확인</button>
          <button class="btn btn-danger"
            onclick="cancelReservation(${data.id})">취소</button>
        </td>
      `;
    } else if (type === 'reservation' && data.status === 'PENDING') {
      tr.innerHTML = `
        <td>${data.date}</td>
        <td>${data.themeName}</td>
        <td>${formatTime(data.time.startAt)}</td>
        <td><span class="status-badge status-badge--waiting">결제 필요</span></td>
        <td>${paymentCell(data)}</td>
        <td style="text-align:right;">
          <button class="btn btn-primary" style="font-size:0.82rem;padding:6px 12px;margin-right:4px;"
            onclick="payReservation('${data.orderId}')">결제하기</button>
          <button class="btn btn-danger"
            onclick="cancelReservation(${data.id})">취소</button>
        </td>
      `;
    } else if (type === 'reservation') {
      tr.innerHTML = `
        <td>${data.date}</td>
        <td>${data.themeName}</td>
        <td>${formatTime(data.time.startAt)}</td>
        <td><span class="status-badge status-badge--reserved">예약</span></td>
        <td>${paymentCell(data)}</td>
        <td style="text-align:right;">
          <button class="btn btn-secondary" style="font-size:0.82rem;padding:6px 12px;margin-right:4px;"
            onclick="openEditModal(${data.id}, '${data.date}', ${data.themeId})">변경</button>
          <button class="btn btn-danger"
            onclick="cancelReservation(${data.id})">취소</button>
        </td>
      `;
    } else {
      tr.innerHTML = `
        <td>${data.date}</td>
        <td>${data.themeName}</td>
        <td>${formatTime(data.startAt)}</td>
        <td><span class="status-badge status-badge--waiting">대기 ${data.turn}순번</span></td>
        <td>—</td>
        <td style="text-align:right;">
          <button class="btn btn-danger"
            onclick="cancelWaiting(${data.id})">취소</button>
        </td>
      `;
    }
    tbody.appendChild(tr);
  });
}

function paymentCell(data) {
  const amount = data.paymentAmount != null ? data.paymentAmount.toLocaleString() + '원' : '';
  const orderLine = data.orderId
    ? `<div style="font-size:0.68rem;color:#aaa;word-break:break-all;">주문 ${data.orderId}</div>` : '';
  if (data.paymentStatus === 'CONFIRMED') {
    return `<span class="status-badge status-badge--reserved">확정</span>`
      + `<div style="font-size:0.72rem;color:#888;margin-top:4px;">${amount}</div>`
      + orderLine
      + `<div style="font-size:0.68rem;color:#aaa;word-break:break-all;">결제키 ${data.paymentKey || ''}</div>`;
  }
  if (data.paymentStatus === 'UNKNOWN') {
    return `<span class="status-badge status-badge--waiting">확인 필요</span>`
      + orderLine;
  }
  if (data.paymentStatus === 'PENDING') {
    return `<span class="status-badge status-badge--waiting">결제 대기</span>`
      + `<div style="font-size:0.72rem;color:#888;margin-top:4px;">${amount}</div>`
      + orderLine;
  }
  return '—';
}

function formatTime(value) {
  if (!value) return '';
  const parts = String(value).split(':');
  return `${parts[0]}:${parts[1]}`;
}

function payReservation(orderId) {
  window.location.href = `/payments/checkout?orderId=${orderId}`;
}

function recheckPayment(paymentKey, orderId, amount) {
  window.location.href = `/payments/success?paymentKey=${paymentKey}&orderId=${orderId}&amount=${amount}`;
}

function cancelReservation(id, btn) {
  if (!confirm('예약을 취소하시겠습니까?')) return;
  fetch(`/bookings/${id}`, {method: 'DELETE'})
    .then(res => {
      if (res.status === 204) {
        showToast('예약이 취소되었습니다.', 'success');
        loadMyReservations();
        return;
      }
      return res.json().then(b => { throw new Error(b.message || '취소에 실패했습니다.'); });
    })
    .catch(err => showToast(err.message));
}

function cancelWaiting(id, btn) {
  if (!confirm('대기를 취소하시겠습니까?')) return;
  fetch(`/waitings/${id}`, {method: 'DELETE'})
    .then(res => {
      if (res.status === 204) {
        showToast('대기가 취소되었습니다.', 'success');
        loadMyReservations();
        return;
      }
      return res.json().then(b => { throw new Error(b.message || '취소에 실패했습니다.'); });
    })
    .catch(err => showToast(err.message));
}

function openEditModal(id, currentDate, themeId) {
  editingId = id;
  editingThemeId = themeId;
  datePicker.setDate(currentDate);
  document.getElementById('modal-overlay').classList.remove('d-none');
  loadAvailableTimes(currentDate, themeId);
}

function closeModal() {
  document.getElementById('modal-overlay').classList.add('d-none');
  editingId = null;
  editingThemeId = null;
  datePicker.clear();
}

function loadAvailableTimes(date, themeId) {
  const select = document.getElementById('edit-time');
  select.innerHTML = '<option value="">불러오는 중...</option>';
  fetch(`/times/available?date=${date}&themeId=${themeId}`)
    .then(res => res.json())
    .then(times => {
      select.innerHTML = '';
      if (!times.length) {
        select.innerHTML = '<option value="">예약 가능한 시간이 없습니다.</option>';
        return;
      }
      times.forEach(t => {
        const opt = document.createElement('option');
        opt.value = t.id;
        opt.textContent = formatTime(t.startAt);
        select.appendChild(opt);
      });
    })
    .catch(() => { select.innerHTML = '<option value="">시간 조회에 실패했습니다.</option>'; });
}

function submitEdit() {
  const date = datePicker.input.value;
  const timeId = document.getElementById('edit-time').value;
  if (!date || !timeId) { showToast('날짜와 시간을 선택해주세요.'); return; }
  fetch(`/bookings/${editingId}`, {
    method: 'PATCH',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({date, timeId: parseInt(timeId)})
  })
    .then(res => {
      if (res.ok) return res.json();
      return res.json().then(b => { throw new Error(b.message || '변경에 실패했습니다.'); });
    })
    .then(() => {
      showToast('예약이 변경되었습니다.', 'success');
      closeModal();
      loadMyReservations();
    })
    .catch(err => showToast(err.message));
}
