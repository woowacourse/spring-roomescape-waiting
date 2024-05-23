document.addEventListener('DOMContentLoaded', () => {
    fetch('/admin/reservations/waiting') // 내 예약 목록 조회 API 호출
        .then(response => {
            if (response.status === 200) return response.json();
            throw new Error('Read failed');
        })
        .then(render)
        .catch(error => console.error('Error fetching reservations:', error));
});

function render(response) {
    const data = response.data;
    const tableBody = document.getElementById('table-body');
    tableBody.innerHTML = '';
    let index = 1;
    data.forEach(item => {
        const row = tableBody.insertRow();
        const id = index++;
        const name = item.name;
        const theme = item.theme;
        const date = item.date;
        const startAt = item.startAt;

        row.insertCell(0).textContent = id;            // 예약 대기 id
        row.insertCell(1).textContent = name;          // 예약자명
        row.insertCell(2).textContent = theme;         // 테마명
        row.insertCell(3).textContent = date;          // 예약 날짜
        row.insertCell(4).textContent = startAt;       // 시작 시간

        const actionCell = row.insertCell(row.cells.length);
        actionCell.appendChild(createActionButton('거절', 'btn-danger', deny, item.id));
    });
}

function deny(id) {
    const endpoint = `/admin/reservations/wait/${id}`;
    return fetch(endpoint, {
        method: 'DELETE'
    }).then(response => {
        if (response.status === 204) return;
        throw new Error('Delete failed');
    });
}

function createActionButton(label, className, eventListener, id) {
    const button = document.createElement('button');
    button.textContent = label;
    button.classList.add('btn', className, 'mr-2');
    button.onclick = function () {
        deny(id).then(r => location.reload());
    };
    return button;
}