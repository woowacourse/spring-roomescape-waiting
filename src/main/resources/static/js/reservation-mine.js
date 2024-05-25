document.addEventListener('DOMContentLoaded', () => {
  fetch('/reservations/mine') // 내 예약 목록 조회 API 호출
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

  data.forEach(item => {
    const row = tableBody.insertRow();

    const theme = item.theme;
    const date = item.date;
    const time = item.time;
    const status = item.status;
    const id = item.id;

    row.insertCell(0).textContent = id;
    row.insertCell(1).textContent = theme;
    row.insertCell(2).textContent = date;
    row.insertCell(3).textContent = time;
    row.insertCell(4).textContent = status;

    const cancelCell = row.insertCell(5);
    const cancelButton = document.createElement('button');
    cancelButton.className = 'btn btn-danger';
    if (status !== '예약') {
      cancelButton.textContent = '대기 취소';
      cancelButton.onclick = function () {
        requestDeleteWaiting(id).then(() => window.location.reload());
      };
    } else {
      cancelButton.textContent = '삭제';
      cancelButton.onclick = function () {
        requestDeleteReservation(id).then(() => window.location.reload());
      };
    }
    cancelCell.appendChild(cancelButton);
  });
}

function requestDeleteWaiting(id) {
  const endpoint = '/reservations/waiting/' + id;
  return fetch(endpoint, {
    method: 'DELETE'
  }).then(response => {
    if (response.status === 204) return;
    throw new Error('Delete failed');
  });
}

function requestDeleteReservation(id) {
  const endpoint = '/reservations/' + id;
  return fetch(endpoint, {
    method: 'DELETE'
  }).then(response => {
    if (response.status === 204) return;
    throw new Error('Delete failed');
  });
}
