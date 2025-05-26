document.addEventListener('DOMContentLoaded', () => {
    /*
    TODO: [4단계] 예약 대기 관리 기능
          예약 대기 목록 조회 endpoint 설정
     */
    fetch('/reservations/waitings') // 내 예약 목록 조회 API 호출
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

        const reservationId = item.reservationId;
        const member = item.member;
        const theme = item.theme;
        const date = item.date;
        const time = item.time;

        row.insertCell(0).textContent = reservationId;  // 예약 대기 id
        row.insertCell(1).textContent = member;
        row.insertCell(2).textContent = theme;         // 테마명
        row.insertCell(3).textContent = date;          // 예약 날짜
        row.insertCell(4).textContent = time;          // 시작 시간

        const actionCell = row.insertCell(5);
        actionCell.appendChild(createActionButton('승인', 'btn-primary', approve));
        actionCell.appendChild(createActionButton('거절', 'btn-danger', deny));
    });
}

function approve(event) {
    const row = event.target.closest('tr');
    const id = row.cells[0].textContent;

    return fetch(`/reservations/waiting/${id}/approve`, {
        method: 'POST'
    }).then(response => {
        if (response.status === 200) return;
        throw new Error('Approve failed');
    }).then(() => location.reload());
}

function deny(event) {
    const row = event.target.closest('tr');
    const id = row.cells[0].textContent;

    return fetch(`/reservations/waiting/${id}/deny`, {
        method: 'POST'
    }).then(response => {
        if (response.status === 200) return;
        throw new Error('Deny failed');
    }).then(() => location.reload());
}

function createActionButton(label, className, eventListener) {
    const button = document.createElement('button');
    button.textContent = label;
    button.classList.add('btn', className, 'mr-2');
    button.addEventListener('click', eventListener);
    return button;
}
