let editingId = null;
let editingThemeId = null;
let datePicker = null;

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('search-btn').addEventListener('click', search);
  document.getElementById('search-name').addEventListener('keydown', e => {
    if (e.key === 'Enter') search();
  });
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
});

function search() {
  const name = document.getElementById('search-name').value.trim();
  if (!name) { showToast('이름을 입력해주세요.'); return; }

  Promise.all([
    fetch(`/reservations/my?name=${encodeURIComponent(name)}`).then(r => r.json()),
    fetch(`/waitings?name=${encodeURIComponent(name)}`).then(r => r.json())
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
    const dateA = a.type === 'reservation' ? a.data.date : a.data.date;
    const dateB = b.type === 'reservation' ? b.data.date : b.data.date;
    return dateA < dateB ? -1 : dateA > dateB ? 1 : 0;
  });

  if (rows.length === 0) {
    empty.classList.remove('d-none');
    return;
  }
  empty.classList.add('d-none');

  rows.forEach(({type, data}) => {
    const tr = document.createElement('tr');
    if (type === 'reservation') {
      const isPending   = data.status === 'PAYMENT_PENDING';
      const isUncertain = data.status === 'PAYMENT_UNCERTAIN';

      let statusBadge, paymentInfo, actionButtons;

      if (isPending) {
        const checkoutUrl = `/payments/checkout?orderId=${encodeURIComponent(data.orderId)}&orderName=${encodeURIComponent(data.themeName)}`;
        statusBadge  = `<span class="status-badge" style="background:#e2e3e5;color:#383d41;border:1px solid #d6d8db;">결제 대기</span>`;
        paymentInfo  = `<div style="font-size:0.78rem;color:#6c757d;margin-top:4px;">${data.amount != null ? data.amount.toLocaleString() + '원' : ''}</div>`;
        actionButtons = `
          <a href="${checkoutUrl}" class="btn btn-primary" style="font-size:0.82rem;padding:6px 12px;margin-right:4px;">결제하기</a>
          <button class="btn btn-danger" onclick="cancelReservation(${data.id}, this)">취소</button>`;
      } else if (isUncertain) {
        statusBadge  = `<span class="status-badge" style="background:#fff3cd;color:#856404;border:1px solid #ffc107;">확인 필요</span>`;
        paymentInfo  = `<div style="font-size:0.78rem;color:#856404;margin-top:4px;">주문번호: ${data.orderId ?? '-'}</div>`;
        actionButtons = `<button class="btn btn-danger" onclick="cancelReservation(${data.id}, this)">취소</button>`;
      } else {
        statusBadge  = `<span class="status-badge status-badge--reserved">예약 확정</span>`;
        paymentInfo  = `<div style="font-size:0.78rem;color:var(--text-muted);margin-top:4px;">${data.amount != null ? data.amount.toLocaleString() + '원' : ''}</div>`;
        actionButtons = `
          <button class="btn btn-secondary" style="font-size:0.82rem;padding:6px 12px;margin-right:4px;"
            onclick="openEditModal(${data.id}, '${data.date}', ${data.themeId})">변경</button>
          <button class="btn btn-danger" onclick="cancelReservation(${data.id}, this)">취소</button>`;
      }

      tr.innerHTML = `
        <td>${data.date}</td>
        <td>${data.themeName}</td>
        <td>${formatTime(data.time.startAt)}</td>
        <td>${statusBadge}${paymentInfo}</td>
        <td style="text-align:right;">${actionButtons}</td>
      `;
    } else {
      tr.innerHTML = `
        <td>${data.date}</td>
        <td>${data.themeName}</td>
        <td>${formatTime(data.startAt)}</td>
        <td><span class="status-badge status-badge--waiting">대기 ${data.turn}순번</span></td>
        <td style="text-align:right;">
          <button class="btn btn-danger"
            onclick="cancelWaiting(${data.id}, this)">취소</button>
        </td>
      `;
    }
    tbody.appendChild(tr);
  });
}

function formatTime(value) {
  if (!value) return '';
  const parts = String(value).split(':');
  return `${parts[0]}:${parts[1]}`;
}

function cancelReservation(id, btn) {
  if (!confirm('예약을 취소하시겠습니까?')) return;
  fetch(`/reservations/${id}`, {method: 'DELETE'})
    .then(res => {
      if (res.status === 204) {
        btn.closest('tr').remove();
        showToast('예약이 취소되었습니다.', 'success');
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
        btn.closest('tr').remove();
        showToast('대기가 취소되었습니다.', 'success');
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
  fetch(`/reservations/${editingId}`, {
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
      search();
    })
    .catch(err => showToast(err.message));
}