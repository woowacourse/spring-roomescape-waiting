document.addEventListener('DOMContentLoaded', () => {
    fetch('/admin/waitings') // 내 예약 목록 조회 API 호출
        .then(response => {
            if (response.status === 200) return response.json();
            throw new Error('Read failed');
        })
        .then(data => data.data)
        .then(render)
        .catch(error => console.error('Error fetching reservations:', error));
});

function render(data) {
    const tableBody = document.getElementById('table-body');
    tableBody.innerHTML = '';

    data.forEach(item => {
        const row = tableBody.insertRow();

        row.insertCell(0).textContent = item.id;
        row.insertCell(1).textContent = item.member.name;
        row.insertCell(2).textContent = item.theme.name;
        row.insertCell(3).textContent = item.date;
        row.insertCell(4).textContent = item.time.startAt;

        const actionCell = row.insertCell(row.cells.length);

        actionCell.appendChild(createActionButton('거절', 'btn-danger', deny));
    });
}

function deny(event) {
    const row = event.target.closest('tr');
    const id = row.cells[0].textContent;

    return fetch('/admin/waiting/'+id, {
        method: 'DELETE'
    }).then(response => {
        if (response.status === 204) return;
        throw new Error('Delete failed');
    }).then(() => location.reload());
}

function createActionButton(label, className, eventListener) {
    const button = document.createElement('button');
    button.textContent = label;
    button.classList.add('btn', className, 'mr-2');
    button.addEventListener('click', eventListener);
    return button;
}
