document.addEventListener('DOMContentLoaded', () => {
    fetch('/users/reservations') // 내 예약 목록 조회 API 호출
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
        내 예약 목록 조회 기능
              response 명세에 맞춰 값 설정
         */
        const theme = item.theme.name;
        const date = item.date;
        const time = item.time.startAt;
        const status = getStatusText(item.type, item.waitingRank);

        row.insertCell(0).textContent = theme;
        row.insertCell(1).textContent = date;
        row.insertCell(2).textContent = time;
        row.insertCell(3).textContent = status;

        if (item.type === 'WAITING') {
            const cancelCell = row.insertCell(4);
            const cancelButton = document.createElement('button');
            cancelButton.textContent = '취소';
            cancelButton.className = 'btn btn-danger';
            cancelButton.onclick = function () {
                requestDeleteWaiting(item.id).then(() => window.location.reload());
            };
            cancelCell.appendChild(cancelButton);
        } else { // 예약 완료 상태일 때
            row.insertCell(4).textContent = '';
        }
    });
}

function getStatusText(type, waitingRank) {
    if (type === 'RESERVATION') {
        return '예약';
    }
    if (type === 'WAITING') {
        return `${waitingRank}번째 예약대기`;
    }
    return '';
}

function requestDeleteWaiting(id) {
    /*
    예약 대기 기능 - 예약 대기 취소 API 호출
     */
    const WAITING_PATH = '/waitings';

    const endpoint = `${WAITING_PATH}/${id}`;
    return fetch(endpoint, {
        method: 'DELETE'
    }).then(response => {
        if (response.status === 204) return;
        throw new Error('Delete failed');
    });
}
