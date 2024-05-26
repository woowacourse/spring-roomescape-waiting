document.addEventListener('DOMContentLoaded', () => {
  fetch('/admin/reservations/waitings') // 내 예약 목록 조회 API 호출
      .then(response => {
        if (response.status === 200) return response.json();
        throw new Error('Read failed');
      })
      .then(render)
      .catch(error => console.error('Error fetching reservations:', error));
});

function render(data) {
  const tableBody = document.getElementById('table-body');
    tableBody.innerHTML = '';

    data.data.reservations.forEach(item => {
      const row = tableBody.insertRow();

      row.insertCell(0).textContent = item.id;              // 예약 id
      row.insertCell(1).textContent = item.member.name;     // 사용자 name
      row.insertCell(2).textContent = item.theme.name;      // 테마 name
      row.insertCell(3).textContent = item.date;            // date
      row.insertCell(4).textContent = item.time.startAt;    // 예약 시간 startAt

      const acceptCell = row.insertCell(5);
      const acceptButton = document.createElement('button');
      acceptButton.textContent = '승인';
      acceptButton.className = 'btn btn-primary';
      acceptButton.onclick = function () {
        requestApproveWaiting(item.id).then(() => window.location.reload());
      };
      acceptCell.appendChild(acceptButton);

      const cancelCell = row.insertCell(6);
      const cancelButton = document.createElement('button');
      cancelButton.textContent = '거절';
      cancelButton.className = 'btn btn-danger';
      cancelButton.onclick = function () {
        requestDeleteWaiting(item.id).then(() => window.location.reload());
      };
      cancelCell.appendChild(cancelButton);
    });
}

function requestDeleteWaiting(reservationId) {
  const endpoint = `/reservations/waitings/${reservationId}`;
  return fetch(endpoint, {
    method: 'DELETE'
  }).then(response => {
    if (response.status === 204) return;
    throw new Error('Delete failed');
  });
}

function requestApproveWaiting(reservationId) {
  const endpoint = `/admin/reservations/waitings/${reservationId}`;
  return fetch(endpoint, {
    method: 'PATCH'
  }).then(response => {
    if (response.status === 200) return;
    throw new Error('Approve failed');
  });
}
