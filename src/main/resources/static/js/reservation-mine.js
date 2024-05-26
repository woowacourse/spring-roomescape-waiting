document.addEventListener('DOMContentLoaded', () => {
    /*
     [2단계] 내 예약 목록 조회 기능
          endpoint 설정
     */
    fetch('/reservations-mine') // 내 예약 목록 조회 API 호출
        .then(response => {
            if (response.status === 200) return response.json();
            throw new Error('Read failed');
        })
        .then(render)
        .catch(error => {
            console.error('Error fetching reservations:', error);
            alert('예약 목록을 불러오는 데 실패했습니다.');
        });
});

function render(data) {
    const tableBody = document.getElementById('table-body');
    tableBody.innerHTML = '';

    data.forEach(item => {
        const row = tableBody.insertRow();

        const reservationId = item.reservationId;
        const theme = item.theme;
        const date = item.date;
        const time = item.time;
        const status = item.status;

        row.insertCell(0).textContent = reservationId;
        row.insertCell(1).textContent = theme;
        row.insertCell(2).textContent = date;
        row.insertCell(3).textContent = time;
        row.insertCell(4).textContent = status;

        /*
        [3단계] 예약 대기 기능 - 예약 대기 취소 기능 구현 후 활성화
         */
        if (status !== '예약') { // 예약 대기 상태일 때 예약 대기 취소 버튼 추가하는 코드, 상태 값은 변경 가능
            const cancelButton = document.createElement('button');
            cancelButton.textContent = '취소';
            cancelButton.className = 'btn btn-danger';
            cancelButton.onclick = function () {
                requestDeleteWaiting(item.reservationId).then(() => window.location.reload()).catch(error => {
                    console.error('Error deleting waiting reservation:', error);
                    alert('예약 대기를 취소하는 데 실패했습니다.');
                });
            };
            const cancelCell = row.insertCell(5); // 가장 오른쪽에 추가될 셀
            cancelCell.appendChild(cancelButton);
        } else { // 예약 완료 상태일 때
            row.insertCell(5).textContent = '';
        }
    });
}

function requestDeleteWaiting(id) {
    /*
    TODO: [3단계] 예약 대기 기능 - 예약 대기 취소 API 호출
     */
    const endpoint = '/waitings/' + id;
    return fetch(endpoint, {
        method: 'DELETE'
    }).then(response => {
        if (response.status === 204) return;
        throw new Error('Delete failed');
    });
}
