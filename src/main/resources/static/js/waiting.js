document.addEventListener('DOMContentLoaded', () => {
    /*
    TODO: [4�ܰ�] ���� ��� ���� ���
          ���� ��� ��� ��ȸ endpoint ����
     */
    fetch('/reservations/waiting') // �� ���� ��� ��ȸ API ȣ��
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
        TODO: [4�ܰ�] ���� ��� ���� ���
              ���� ��� ��� ��ȸ response ���� ���� �� ����
         */
        const id = item.id;
        const name = item.name;
        const theme = item.themeName;
        const date = item.date;
        const startAt = item.time;

        row.insertCell(0).textContent = id;            // ���� ��� id
        row.insertCell(1).textContent = name;          // �����ڸ�
        row.insertCell(2).textContent = theme;         // �׸���
        row.insertCell(3).textContent = date;          // ���� ��¥
        row.insertCell(4).textContent = startAt;       // ���� �ð�

        const actionCell = row.insertCell(row.cells.length);

        /*
        TODO: [4�ܰ�] ���� ��� ���� ���
              ���� ��� ����/���� ��ư�� �ʿ��� ��� Ȱ��ȭ�Ͽ� ���
         */
        actionCell.appendChild(createActionButton('����', 'btn-primary', approve));
        actionCell.appendChild(createActionButton('����', 'btn-danger', deny));
    });
}

function approve(event) {
    const row = event.target.closest('tr');
    const id = row.cells[0].textContent;

    /*
    TODO: [4�ܰ�] ���� ��� ��� ���� ���
          ���� ��� ���� API ȣ��
     */
    const endpoint = '/reservations/waiting/apply';
    return fetch(endpoint, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({id})
    }).then(response => {
        if (response.status === 200) return;
        throw new Error('Delete failed');
    }).then(() => location.reload());
}

function deny(event) {
    const row = event.target.closest('tr');
    const id = row.cells[0].textContent;

    /*
    TODO: [4�ܰ�] ���� ��� ��� ���� ���
          ���� ��� ���� API ȣ��
     */
    const endpoint = `/reservations/waiting/${id}`;
    return fetch(endpoint, {
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