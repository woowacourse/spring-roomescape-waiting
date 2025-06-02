document.addEventListener('DOMContentLoaded', () => {
    Promise.all([
        fetch('/reservations/mine').then(res => {
            if (res.status === 200) return res.json();
            throw new Error('Failed to fetch reservations');
        }),
        fetch('/waitings').then(res => {
            if (res.status === 200) return res.json();
            throw new Error('Failed to fetch waitings');
        })
    ])
    .then(([reservations, waitings]) => {
        renderCombined(reservations, waitings);
    })
    .catch(error => {
        console.error('Error fetching reservation data:', error);
    });
});

function renderCombined(reservations, waitings) {
    const tableBody = document.getElementById('table-body');
    tableBody.innerHTML = '';

    const all = [
        ...reservations.map(r => ({
            id: r.id,
            theme: r.theme,
            date: r.date,
            time: r.time,
            status: r.status
        })),
        ...waitings.map(w => ({
            id: w.id,
            theme: w.theme,
            date: w.date,
            time: w.time,
            status: w.status
        }))
    ];

    all.sort((a, b) => new Date(a.date + ' ' + a.time) - new Date(b.date + ' ' + b.time));

    all.forEach(item => {
        const row = tableBody.insertRow();
        row.insertCell(0).textContent = item.theme;
        row.insertCell(1).textContent = item.date;
        row.insertCell(2).textContent = item.time;
        row.insertCell(3).textContent = item.status;

        const cancelCell = row.insertCell(4);
        if (item.status.includes('예약대기')) {
            const cancelButton = document.createElement('button');
            cancelButton.textContent = '취소';
            cancelButton.className = 'btn btn-danger';
            cancelButton.onclick = function () {
                requestDeleteWaiting(item.id).then(() => window.location.reload());
            };
            cancelCell.appendChild(cancelButton);
        } else {
            cancelCell.textContent = '';
        }
    });
}

function requestDeleteWaiting(id) {
    return fetch(`/waitings/${id}`, {
        method: 'DELETE'
    }).then(response => {
        if (response.status === 204) return;
        throw new Error('Delete failed');
    });
}
