document.addEventListener('DOMContentLoaded', () => {
    /*
    TODO: [2단계] 내 예약 목록 조회 기능
          endpoint 설정
     */
    fetch('/api/reservations/my') // 내 예약 목록 조회 API 호출
        .then(response => {
            if (response.status === 200) return response.json();
            throw new Error('Read failed');
        })
        .then(renderReservation)
        .catch(error => console.error('Error fetching reservations:', error));

    fetch('/api/waiting/my') // 내 예약 목록 조회 API 호출
        .then(response => {
            if (response.status === 200) return response.json();
            throw new Error('Read failed');
        })
        .then(renderWaiting)
        .catch(error => console.error('Error fetching reservations:', error));
});

function renderReservation(data) {
    const tableBody = document.getElementById('reservation-table-body');
    tableBody.innerHTML = '';

    data.forEach(item => {
        const row = tableBody.insertRow();

        /*
        TODO: [2단계] 내 예약 목록 조회 기능
              response 명세에 맞춰 값 설정
         */
        const theme = item.themeName;
        const date = item.date;
        const time = item.time.startAt;
        const status = "예약";

        row.insertCell(0).textContent = theme;
        row.insertCell(1).textContent = date;
        row.insertCell(2).textContent = time;
        row.insertCell(3).textContent = status;
        row.insertCell(4).textContent = '';
    });
}

function renderWaiting(data) {
    const tableBody = document.getElementById('waiting-table-body');

    tableBody.innerHTML = '';

    data.forEach(item => {
        const row = tableBody.insertRow();

        /*
        TODO: [2단계] 내 예약 목록 조회 기능
              response 명세에 맞춰 값 설정
         */
        const theme = item.themeName;
        const date = item.date;
        const time = item.time.startAt;
        const status = item.rank + "번째 예약 대기";

        row.insertCell(0).textContent = theme;
        row.insertCell(1).textContent = date;
        row.insertCell(2).textContent = time;
        row.insertCell(3).textContent = status;

        /*
        TODO: [3단계] 예약 대기 기능 - 예약 대기 취소 기능 구현 후 활성화
         */
        const cancelCell = row.insertCell(4);
        const cancelButton = document.createElement('button');
        cancelButton.textContent = '취소';
        cancelButton.className = 'btn btn-danger';
        cancelButton.onclick = function () {
            requestDeleteWaiting(item.id).then(() => window.location.reload());
        };
        cancelCell.appendChild(cancelButton);
    });
}

function requestDeleteWaiting(id) {
    /*
    TODO: [3단계] 예약 대기 기능 - 예약 대기 취소 API 호출
     */
    const endpoint = `/api/waiting/${id}`;
    return fetch(endpoint, {
        method: 'DELETE'
    }).then(response => {
        if (response.status === 204) return;
        throw new Error('Delete failed');
    });
}