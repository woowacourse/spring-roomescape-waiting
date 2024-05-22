const RESERVATION_API_ENDPOINT = '/reservations'

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

        row.insertCell(0).textContent = theme;
        row.insertCell(1).textContent = date;
        row.insertCell(2).textContent = time;

        if (status === '예약') {
            row.insertCell(3).textContent = status;
        } else {
            row.insertCell(3).textContent = `${item.rank}번째 예약대기`;
        }

        if (status !== '예약') {
            const cancelCell = row.insertCell(4);
            const cancelButton = document.createElement('button');
            cancelButton.textContent = '대기 취소';
            cancelButton.className = 'btn btn-danger';
            cancelButton.onclick = function () {
                requestDeleteWaiting(item.reservationId).then(() => window.location.reload());
            };
            cancelCell.appendChild(cancelButton);
        } else { // 예약 완료 상태일 때
            const cancelCell = row.insertCell(4);
            const cancelButton = document.createElement('button');
            cancelButton.textContent = '예약 취소';
            cancelButton.className = 'btn btn-danger';
            cancelButton.style.backgroundColor = "#3265c1";
            cancelButton.style.borderColor = "#3265c1";
            cancelButton.onclick = function () {
                requestDelete(item.reservationId).then(() => window.location.reload());
            };
            cancelCell.appendChild(cancelButton);
        }
    });
}

function requestDeleteWaiting(id) {
    const endpoint = `${RESERVATION_API_ENDPOINT}/${id}`;
    return fetch(endpoint, {
        method: 'DELETE'
    }).then(response => {
        if (!response.ok) {
            return response.text().then(errorResponse => {
                throw new Error(errorResponse);
            })
        }
        alert("예약 대기가 취소 되었습니다.")
    }).catch(error => {
        alert(error.message);
    });
}

function requestDelete(id) {
    const endpoint = `${RESERVATION_API_ENDPOINT}/${id}`;
    return fetch(endpoint, {
        method: 'POST'
    }).then(response => {
        if (!response.ok) {
            return response.text().then(errorResponse => {
                throw new Error(errorResponse);
            })
        }
        alert("예약이 취소 되었습니다.")
    }).catch(error => {
        alert(error.message);
    });
}
