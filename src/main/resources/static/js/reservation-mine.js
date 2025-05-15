document.addEventListener('DOMContentLoaded', () => {
    /*
    TODO: [2�ܰ�] �� ���� ��� ��ȸ ���
          endpoint ����
     */
    fetch('/reservations/mine') // �� ���� ��� ��ȸ API ȣ��
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
        TODO: [2�ܰ�] �� ���� ��� ��ȸ ���
              response ���� ���� �� ����
         */
        const theme = item.theme.name;
        const date = item.date;
        const time = item.time.startAt;
        const status = item.status;

        row.insertCell(0).textContent = theme;
        row.insertCell(1).textContent = date;
        row.insertCell(2).textContent = time;
        row.insertCell(3).textContent = status;

        /*
        TODO: [3�ܰ�] ���� ��� ��� - ���� ��� ��� ��� ���� �� Ȱ��ȭ
         */
        if (status !== '����') { // ���� ��� ������ �� ���� ��� ��� ��ư �߰��ϴ� �ڵ�, ���� ���� ���� ����
            const cancelCell = row.insertCell(4);
            const cancelButton = document.createElement('button');
            cancelButton.textContent = '���';
            cancelButton.className = 'btn btn-danger';
            cancelButton.onclick = function () {
                requestDeleteWaiting(item.id).then(() => window.location.reload());
            };
            cancelCell.appendChild(cancelButton);
        } else { // ���� �Ϸ� ������ ��
            row.insertCell(4).textContent = '';
        }
    });
}

function requestDeleteWaiting(id) {
    /*
    TODO: [3�ܰ�] ���� ��� ��� - ���� ��� ��� API ȣ��
     */
    const endpoint = '';
    return fetch(endpoint, {
        method: 'DELETE'
    }).then(response => {
        if (response.status === 204) return;
        throw new Error('Delete failed');
    });
}