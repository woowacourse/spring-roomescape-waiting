const WAITING_API = '/admin/waitings';

document.addEventListener('DOMContentLoaded', fetchWaitings);

function fetchWaitings() {
  fetch(WAITING_API)
    .then(res => {
      if (res.status === 401 || res.status === 403) {
        showToast('관리자만 접근할 수 있습니다.');
        setTimeout(() => { window.location.href = '/login'; }, 1000);
        return [];
      }
      return res.json();
    })
    .then(renderWaitings)
    .catch(() => showToast('대기 조회에 실패했습니다.'));
}

function renderWaitings(waitings) {
  const tbody = document.getElementById('waiting-tbody');
  const empty = document.getElementById('waiting-empty');
  tbody.innerHTML = '';

  if (!waitings || waitings.length === 0) {
    empty.classList.remove('d-none');
    return;
  }
  empty.classList.add('d-none');

  waitings.forEach(w => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${w.id}</td>
      <td>${w.memberName}</td>
      <td>${w.themeName}</td>
      <td>${w.date}</td>
      <td>${formatTime(w.startAt)}</td>
      <td style="text-align:right;">
        <button class="btn btn-danger" onclick="cancelWaiting(${w.id})">취소</button>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

function formatTime(value) {
  if (!value) return '';
  const parts = String(value).split(':');
  return `${parts[0]}:${parts[1]}`;
}

function cancelWaiting(id) {
  if (!confirm('이 대기를 취소하시겠습니까?')) return;
  fetch(`${WAITING_API}/${id}`, {method: 'DELETE'})
    .then(res => {
      if (res.status === 204) {
        showToast('대기가 취소되었습니다.', 'success');
        fetchWaitings();
        return;
      }
      return res.json().then(b => { throw new Error(b.message || '취소에 실패했습니다.'); });
    })
    .catch(err => showToast(err.message));
}