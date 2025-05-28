document.addEventListener('DOMContentLoaded', () => {

    // TODO: [2단계] url 수정

    fetch('/reservations/state') // 내 예약 목록 조회 API 호출
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

    const reservations = data.reservationResponses;
    reservations.forEach(item => {
            const row = tableBody.insertRow();

            /*
            TODO: [2단계] 내 예약 목록 조회 기능
                  response 명세에 맞춰 값 설정
             */

            const theme = item.theme.name;
            const date = item.date;
            const time = item.time.startAt;

            row.insertCell(0).textContent = theme;
            row.insertCell(1).textContent = date;
            row.insertCell(2).textContent = time;
            row.insertCell(3).textContent = '예약';
            row.insertCell(4).textContent = '';
    });

    const waitingWithRanks = data.waitingWithRankResponses;
    waitingWithRanks.forEach(item => {
        const row = tableBody.insertRow();

        /*
        TODO: [2단계] 내 예약 목록 조회 기능
              response 명세에 맞춰 값 설정
         */

        const theme = item.theme.name;
        const date = item.date;
        const time = item.time.startAt;
        const rank = item.rank;

        row.insertCell(0).textContent = theme;
        row.insertCell(1).textContent = date;
        row.insertCell(2).textContent = time;
        row.insertCell(3).textContent = rank + '번째 대기';

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
    const endpoint = '/waiting/mine/' + id;
    return fetch(endpoint, {
        method: 'DELETE'
    }).then(response => {
        if (response.status === 204) return;
        throw new Error('Delete failed');
    });
}
