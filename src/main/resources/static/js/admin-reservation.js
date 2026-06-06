(() => {
    const RESERVATIONS_API = '/reservations';

    document.addEventListener('DOMContentLoaded', () => {
        document.getElementById('refresh-button').addEventListener('click', refresh);
        refresh();
    });

    async function refresh() {
        try {
            const reservations = await fetchJson(RESERVATIONS_API);
            render(reservations);
        } catch (error) {
            console.error('예약 조회 실패:', error);
            alert(error.message);
        }
    }

    function render(reservations) {
        const tbody = document.getElementById('admin-reservation-table-body');
        tbody.innerHTML = '';

        if (!reservations || reservations.length === 0) {
            showEmptyState(tbody, 8, '예약과 대기가 없습니다.');
            return;
        }

        reservations.forEach((reservation, index) => {
            const row = tbody.insertRow();

            row.insertCell().textContent = index + 1;
            row.insertCell().textContent = reservation.name;
            row.insertCell().textContent = reservation.theme ? reservation.theme.name : '-';
            row.insertCell().textContent = reservation.date;
            row.insertCell().textContent = reservation.time ? reservation.time.startAt : '-';
            row.insertCell().textContent = getStatusText(reservation);
            row.insertCell().textContent = reservation.waitingOrder || '-';

            const actions = row.insertCell();
            actions.className = 'actions';
            if (reservation.status !== 'WAITING') {
                actions.appendChild(createButton('삭제', 'btn-danger', () => deleteReservation(reservation)));
            }
        });
    }

    function getStatusText(reservation) {
        if (reservation.status === 'WAITING') {
            return '대기';
        }

        return '예약';
    }

    async function deleteReservation(reservation) {
        const themeName = reservation.theme ? reservation.theme.name : '선택한 테마';
        const time = reservation.time ? reservation.time.startAt : '';
        const confirmed = confirm(`${reservation.name}님의 ${themeName} ${reservation.date} ${time} 예약을 삭제하시겠습니까?`);
        if (!confirmed) return;

        try {
            await fetchJson(`${RESERVATIONS_API}/${reservation.id}`, { method: 'DELETE' });
            await refresh();
        } catch (error) {
            console.error('예약 삭제 실패:', error);
            alert(error.message);
        }
    }
})();
