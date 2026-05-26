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
  if (!name) {
    showToast('이름을 입력해주세요.');
    return;
  }
  fetch(`/reservations?name=${encodeURIComponent(name)}`)
    .then(res => res.json())
    .then(renderReservations)
    .catch(() => showToast('예약 조회에 실패했습니다.'));
}

function renderReservations(data) {
  const tbody = document.getElementById('reservation-tbody');
  const empty = document.getElementById('reservation-empty');
  tbody.innerHTML = '';

  if (!data || data.length === 0) {
    empty.classList.remove('d-none');
    return;
  }
  empty.classList.add('d-none');

  data.forEach(r => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${r.date}</td>
      <td>${r.themeName}</td>
      <td>${formatTime(r.time.startAt)}</td>
      <td style="text-align:right;">
        <button class="btn btn-secondary" style="font-size:0.82rem;padding:6px 12px;margin-right:4px;"
          onclick="openEditModal(${r.id}, '${r.date}', ${r.themeId})">변경</button>
        <button class="btn btn-danger"
          onclick="cancelReservation(${r.id}, this)">취소</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function formatTime(value) {
  if (!value) return '';
  const [h, m] = value.split(':');
  return `${h}:${m}`;
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
    .catch(() => {
      select.innerHTML = '<option value="">시간 조회에 실패했습니다.</option>';
    });
}

function submitEdit() {
  const date = datePicker.input.value;
  const timeId = document.getElementById('edit-time').value;
  if (!date || !timeId) {
    showToast('날짜와 시간을 선택해주세요.');
    return;
  }
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