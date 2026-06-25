const API = '/reservations/me';
const WAITINGS_API = '/waitings/me';
const TIMES_API = '/times';

let currentName = '';
let isEditing = false;

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('search-form').addEventListener('submit', handleSubmit);
});

async function handleSubmit(event) {
    event.preventDefault();

    const name = document.getElementById('name-input').value.trim();
    if (!name) {
        alert('이름을 입력해주세요.');
        return;
    }

    currentName = name;
    await refresh();
}

async function refresh() {
    try {
        const [reservationData, waitingData] = await Promise.all([
            fetchJson(`${API}?name=${encodeURIComponent(currentName)}`),
            fetchJson(`${WAITINGS_API}?name=${encodeURIComponent(currentName)}`)
        ]);

        render(toRows(
            getResponseItems(reservationData, 'reservations'),
            getResponseItems(waitingData, 'waitings')
        ));
    } catch (error) {
        console.error('내 예약 조회 실패:', error);
        alert(getErrorMessage(error, '내 예약 조회에 실패했습니다.'));
    }
}

function getResponseItems(data, key) {
    if (!data) return [];
    if (Array.isArray(data)) return data;
    return Array.isArray(data[key]) ? data[key] : [];
}

function toRows(reservations = [], waitings = []) {
    const reservationRows = reservations.map(reservation => ({
        type: 'reservation',
        status: '예약',
        data: reservation
    }));

    const waitingRows = waitings.map(waiting => ({
        type: 'waiting',
        status: '대기',
        data: waiting
    }));

    return [...reservationRows, ...waitingRows].sort(compareRows);
}

function compareRows(first, second) {
    const firstDate = first.data.date || '';
    const secondDate = second.data.date || '';
    if (firstDate !== secondDate) {
        return secondDate.localeCompare(firstDate);
    }

    const firstTime = first.data.time ? first.data.time.startAt : '';
    const secondTime = second.data.time ? second.data.time.startAt : '';
    if (firstTime !== secondTime) {
        return firstTime.localeCompare(secondTime);
    }

    return first.type.localeCompare(second.type);
}

function render(rows) {
    const tbody = document.getElementById('table-body');
    tbody.innerHTML = '';
    isEditing = false;

    if (rows.length === 0) {
        showEmptyState(tbody, 9, '예약 및 대기 내역이 없습니다.');
        return;
    }

    rows.forEach(row => {
        renderRow(tbody, row);
    });
}

function renderRow(tbody, item) {
    const reservation = item.data;
    const row = tbody.insertRow();
    row.insertCell().appendChild(createStatusBadge(item.status, item.type));
    row.insertCell().textContent = reservation.theme ? reservation.theme.name : '-';
    row.insertCell().textContent = reservation.date;
    row.insertCell().textContent = reservation.time ? reservation.time.startAt : '-';
    row.insertCell().textContent = item.type === 'waiting' ? reservation.order : '-';

    const isReservation = item.type === 'reservation';
    const paymentCell = row.insertCell();
    paymentCell.textContent = isReservation ? paymentStatusLabel(reservation.paymentStatus) : '-';
    if (isReservation && reservation.paymentKey) {
        paymentCell.title = `결제키: ${reservation.paymentKey}`;
    }
    row.insertCell().textContent = isReservation && reservation.amount != null
        ? `${reservation.amount.toLocaleString()}원` : '-';
    row.insertCell().textContent = isReservation && reservation.orderId ? reservation.orderId : '-';

    const actions = row.insertCell();
    actions.className = 'actions';

    const startAt = reservation.time ? reservation.time.startAt : null;
    const past = isPastSlot(reservation.date, startAt);

    if (item.type === 'reservation') {
        if (past) {
            actions.appendChild(createDisabledButton('변경', 'btn-primary', '지난 예약은 변경할 수 없습니다.'));
            actions.appendChild(createDisabledButton('취소', 'btn-ghost', '지난 예약은 취소할 수 없습니다.'));
            return;
        }
        actions.appendChild(createButton('변경', 'btn-primary', () => startEdit(row, reservation)));
        actions.appendChild(createButton('취소', 'btn-ghost', () => cancelReservation(reservation.id)));
        return;
    }

    if (past) {
        actions.appendChild(createDisabledButton('취소', 'btn-ghost', '지난 예약 대기는 취소할 수 없습니다.'));
        return;
    }
    actions.appendChild(createButton('취소', 'btn-ghost', () => cancelWaiting(reservation.id)));
}

function createStatusBadge(label, type) {
    const badge = document.createElement('span');
    badge.className = `status-badge status-${type}`;
    badge.textContent = label;
    return badge;
}

function paymentStatusLabel(status) {
    switch (status) {
        case 'CONFIRMED':
            return '확정';
        case 'PENDING':
            return '결제 대기';
        case 'UNCERTAIN':
            return '확인 필요';
        case 'FAILED':
            return '실패';
        default:
            return '-';
    }
}

function startEdit(row, reservation) {
    if (isEditing) return;
    if (!reservation.theme || !reservation.time) {
        alert('예약 정보가 올바르지 않아 변경할 수 없습니다.');
        return;
    }

    isEditing = true;

    const dateCell = row.cells[2];
    const timeCell = row.cells[3];
    const actions = row.cells[8];

    const dateInput = createInput('date');
    dateInput.value = reservation.date;

    const timeSelect = createSelect();

    dateCell.innerHTML = '';
    dateCell.appendChild(dateInput);
    timeCell.innerHTML = '';
    timeCell.appendChild(timeSelect);

    refreshAvailableTimes(dateInput.value, reservation.theme.id, timeSelect, reservation.time.id);

    dateInput.addEventListener('change', () => {
        refreshAvailableTimes(dateInput.value, reservation.theme.id, timeSelect, reservation.time.id);
    });

    actions.innerHTML = '';
    actions.appendChild(createButton('저장', 'btn-primary', () => {
        saveEdit(reservation.id, dateInput.value, timeSelect.value);
    }));
    actions.appendChild(createButton('취소', 'btn-ghost', () => {
        refresh();
    }));
}

async function refreshAvailableTimes(date, themeId, timeSelect, currentTimeId) {
    clearSelect(timeSelect);

    if (!date || !themeId) {
        appendPlaceholder(timeSelect, '날짜를 먼저 선택해주세요.');
        timeSelect.disabled = true;
        return;
    }

    try {
        const data = await fetchJson(
            `${TIMES_API}/availability?date=${encodeURIComponent(date)}&themeId=${encodeURIComponent(themeId)}`
        );
        const times = data.times;

        clearSelect(timeSelect);

        if (!times || times.length === 0) {
            appendPlaceholder(timeSelect, '등록된 시간이 없습니다.');
            timeSelect.disabled = true;
            return;
        }

        let hasSelected = false;

        times.forEach(time => {
            const option = document.createElement('option');
            const isCurrent = String(time.id) === String(currentTimeId);
            const reserved = time.reserved === true && !isCurrent;

            option.value = time.id;
            option.textContent = reserved ? `${time.startAt} (예약 불가)` : time.startAt;
            option.disabled = reserved;

            if (isCurrent) {
                option.selected = true;
                hasSelected = true;
            } else if (!reserved && !hasSelected) {
                option.selected = true;
                hasSelected = true;
            }

            timeSelect.appendChild(option);
        });

        timeSelect.disabled = !hasSelected;
    } catch (error) {
        console.error('예약 가능 시간 조회 실패:', error);
        clearSelect(timeSelect);
        appendPlaceholder(timeSelect, '예약 가능 시간 조회 실패');
        timeSelect.disabled = true;
    }
}

async function saveEdit(id, date, timeId) {
    if (!date || !timeId) {
        alert('날짜와 시간을 모두 선택해주세요.');
        return;
    }

    try {
        await fetchJson(`${API}/${id}?name=${encodeURIComponent(currentName)}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ date, timeId: Number(timeId) })
        });
        await refresh();
    } catch (error) {
        console.error('예약 변경 실패:', error);
        alert(getErrorMessage(error, '예약 변경에 실패했습니다.'));
    }
}

async function cancelReservation(id) {
    if (!confirm('예약을 취소하시겠습니까?')) return;

    try {
        await fetchJson(`${API}/${id}?name=${encodeURIComponent(currentName)}`, { method: 'DELETE' });
        await refresh();
    } catch (error) {
        console.error('예약 취소 실패:', error);
        alert(getErrorMessage(error, '예약 취소에 실패했습니다.'));
    }
}

async function cancelWaiting(id) {
    if (!confirm('대기 신청을 취소하시겠습니까?')) return;

    try {
        await fetchJson(`${WAITINGS_API}/${id}?name=${encodeURIComponent(currentName)}`, { method: 'DELETE' });
        await refresh();
    } catch (error) {
        console.error('대기 취소 실패:', error);
        alert(getErrorMessage(error, '대기 취소에 실패했습니다.'));
    }
}

function appendPlaceholder(select, message) {
    const option = document.createElement('option');
    option.value = '';
    option.textContent = message;
    select.appendChild(option);
}

function clearSelect(select) {
    select.innerHTML = '';
}
