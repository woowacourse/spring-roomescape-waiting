document.addEventListener('DOMContentLoaded', () => {
    fetch('/members/reservations')
        .then(response => {
            if (response.status === 200) return response.json();
            throw new Error('Read failed');
        })
        .then(render)
        .catch(error => console.error('Error fetching reservations:', error));
});

document.addEventListener('DOMContentLoaded', () => {
    fetch('/members/waitings')
        .then(response => {
            if (response.status === 200) return response.json();
            throw new Error('Read failed');
        })
        .then(renderWaiting)
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
        row.insertCell(3).textContent = status;
        row.insertCell(4).textContent = '';
    });
}

function renderWaiting(data) {
    const tableBody = document.getElementById('table-body-2');
    tableBody.innerHTML = '';

    data.forEach(item => {
        const row = tableBody.insertRow();

        const id = item.waitingId;
        const theme = item.theme;
        const date = item.date;
        const time = item.time;
        const waitingNumber = item.waitingNumber;

        row.insertCell(0).textContent = theme;
        row.insertCell(1).textContent = date;
        row.insertCell(2).textContent = time;
        row.insertCell(3).textContent = `${waitingNumber}` + '번째 대기';

        /*
        TODO: [3단계] 예약 대기 기능 - 예약 대기 취소 기능 구현 후 활성화
         */
        const cancelCell = row.insertCell(4);
        const cancelButton = document.createElement('button');
        cancelButton.textContent = '취소';
        cancelButton.className = 'btn btn-danger';
        console.log(item);
        cancelButton.onclick = function () {
            requestDeleteWaiting(id).then(() => window.location.reload());
        };
        cancelCell.appendChild(cancelButton);
    });
}

function requestDeleteWaiting(id) {
    /*
    TODO: [3단계] 예약 대기 기능 - 예약 대기 취소 API 호출
     */
    console.log(id);
    const endpoint = '/waitings/' + id;
    return fetch(endpoint, {
        method: 'DELETE'
    }).then(response => {
        if (response.status === 204) return;
        throw new Error('Delete failed');
    });
}
