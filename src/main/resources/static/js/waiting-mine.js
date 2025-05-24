document.addEventListener('DOMContentLoaded', () => {
    /*
    [2단계] 내 예약 목록 조회 기능
          endpoint 설정
     */
    fetch('/waitings/mine') // 내 예약 대기 목록 조회 API 호출
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

        const themeName = item.themeName;
        const date = item.date;
        const time = item.time;
        const status = item.status;

        row.insertCell(0).textContent = themeName;
        row.insertCell(1).textContent = date;
        row.insertCell(2).textContent = time;
        row.insertCell(3).textContent = status;

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
    [3단계] 예약 대기 기능 - 예약 대기 취소 API 호출
     */
    const endpoint = `/waitings/${id}`;
    return fetch(endpoint, {
        method: 'DELETE'
    }).then(response => {
        if (response.status === 204) return;
        throw new Error('Delete failed');
    });
}
