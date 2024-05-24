document.addEventListener('DOMContentLoaded', () => {
  /*
  TODO: [2단계] 내 예약 목록 조회 기능
        endpoint 설정
   */
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

    /*
    TODO: [2단계] 내 예약 목록 조회 기능
          response 명세에 맞춰 값 설정
     */
    const theme = item.theme;
    const date = item.date;
    const time = item.time;
    const status = item.status;

    row.insertCell(0).textContent = theme;
    row.insertCell(1).textContent = date;
    row.insertCell(2).textContent = time;
    row.insertCell(3).textContent = status;

    //todo 에약 상태도 취소가 가능하도록 변경
    const cancelCell = row.insertCell(4);
    const cancelButton = document.createElement('button');
    cancelButton.className = 'btn btn-danger';
    if (status !== '예약') {
      cancelButton.textContent = '대기 취소';
      cancelButton.onclick = function () {
        requestDeleteWaiting(item.id).then(() => window.location.reload());
      };
    } else {
      cancelButton.textContent = '삭제';
      cancelButton.onclick = function () {
        requestDeleteReservation(item.id).then(() => window.location.reload());
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
