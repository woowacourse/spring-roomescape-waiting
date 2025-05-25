document.addEventListener('DOMContentLoaded', () => {
    Promise.all([
        fetch('/reservations-mine')
            .then(response => {
                if (response.status === 200) return response.json();
                throw new Error('Reservation read failed');
            }),
        fetch('/waiting/mine')
            .then(response => {
                if (response.status === 200) return response.json();
                throw new Error('Waiting read failed');
            })
    ])
        .then(([reservations, waitings]) => {
            const combined = [
                ...reservations.map(r => ({
                    id: r.id,
                    theme: r.theme,
                    date: r.date,
                    time: r.time,
                    status: '예약'
                })),
                ...waitings.map(w => ({
                    id: w.id,
                    theme: w.theme,
                    date: w.date,
                    time: w.time,
                    status: '대기',
                    order: w.order
                }))
            ].sort((a, b) => new Date(a.date) - new Date(b.date));

            render(combined);
        })
});

function render(data) {
    const tableBody = document.getElementById('table-body');
    tableBody.innerHTML = '';

    data.forEach(item => {
        const row = tableBody.insertRow();

        row.insertCell(0).textContent = item.theme;
        row.insertCell(1).textContent = item.date;
        row.insertCell(2).textContent = item.time;

        const statusText = item.status === '대기'
            ? `${item.order}번째로 대기중`
            : '예약';

        row.insertCell(3).textContent = statusText;

        if (item.status === '대기') {
            const cancelCell = row.insertCell(4);
            const cancelButton = document.createElement('button');
            cancelButton.textContent = '취소';
            cancelButton.className = 'btn btn-danger';
            cancelButton.onclick = function () {
                console.log('hi')
                requestDeleteWaiting(item.id).then(() => window.location.reload());
            };
            cancelCell.appendChild(cancelButton);
        } else {
            row.insertCell(4).textContent = '';
        }
    });

    function requestDeleteWaiting(id) {
        return fetch(`/waiting/${id}`, {method: 'DELETE'})
            .then(response => {
                if (!response.ok) throw new Error('Delete failed');
                return response;
            });
    }
}
