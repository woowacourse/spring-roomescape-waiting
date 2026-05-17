const API = '/reservations/me';
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
        const data = await fetchJson(`${API}?name=${encodeURIComponent(currentName)}`);
        render(data.reservations);
    } catch (error) {
        console.error('내 예약 조회 실패:', error);
        alert(getErrorMessage(error, '내 예약 조회에 실패했습니다.'));
    }
}

function render(reservations) {
    const tbody = document.getElementById('table-body');
    tbody.innerHTML = '';
    isEditing = false;

    if (reservations.length === 0) {
        showEmptyState(tbody, 5, '예약 내역이 없습니다.');
        return;
    }

    reservations.forEach((reservation, index) => {
        renderRow(tbody, reservation, index);
    });
}

function renderRow(tbody, reservation, index) {
    const row = tbody.insertRow();
    row.insertCell().textContent = index + 1;
    row.insertCell().textContent = reservation.theme ? reservation.theme.name : '-';
    row.insertCell().textContent = reservation.date;
    row.insertCell().textContent = reservation.time ? reservation.time.startAt : '-';

    const actions = row.insertCell();
    actions.className = 'actions';
    actions.appendChild(createButton('변경', 'btn-primary', () => startEdit(row, reservation)));
    actions.appendChild(createButton('취소', 'btn-ghost', () => cancelReservation(reservation.id)));
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
    const actions = row.cells[4];

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

function appendPlaceholder(select, message) {
    const option = document.createElement('option');
    option.value = '';
    option.textContent = message;
    select.appendChild(option);
}

function clearSelect(select) {
    select.innerHTML = '';
}
