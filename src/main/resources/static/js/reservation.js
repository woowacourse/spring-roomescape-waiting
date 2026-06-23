const API = '/reservations';
const RESERVATION_ORDERS_API = '/reservation-orders';
const WAITINGS_API = '/waitings';
const TIMES_API = '/times';
const THEMES_API = '/themes';

let isEditing = false;
let cachedThemes = [];

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('add-button').addEventListener('click', startAdd);
    refresh();
});

async function refresh() {
    try {
        const [reservationData, themeData] = await Promise.all([
            fetchJson(API),
            fetchJson(THEMES_API)
        ]);

        cachedThemes = themeData.themes;
        render(reservationData.reservations);
    } catch (error) {
        console.error('예약 조회 실패:', error);
    }
}

function render(reservations) {
    const tbody = document.getElementById('table-body');
    tbody.innerHTML = '';

    if (reservations.length === 0) {
        showEmptyState(tbody, 6, '등록된 예약이 없습니다. "예약 추가" 버튼으로 시작해보세요.');
        return;
    }

    reservations.forEach((reservation, index) => {
        const row = tbody.insertRow();

        row.insertCell().textContent = index + 1;
        row.insertCell().textContent = reservation.name;
        row.insertCell().textContent = reservation.theme ? reservation.theme.name : '-';
        row.insertCell().textContent = reservation.date;
        row.insertCell().textContent = reservation.time ? reservation.time.startAt : '-';

        const actions = row.insertCell();
        actions.className = 'actions';
        actions.appendChild(createButton('삭제', 'btn-danger', () => deleteRow(reservation.id)));
    });
}

function startAdd() {
    if (isEditing) return;

    if (cachedThemes.length === 0) {
        alert('등록된 테마가 없습니다. "테마 관리" 페이지에서 먼저 등록해주세요.');
        return;
    }

    isEditing = true;

    const tbody = document.getElementById('table-body');
    const emptyRow = tbody.querySelector('.empty-state');
    if (emptyRow) emptyRow.closest('tr').remove();

    const row = tbody.insertRow(0);
    row.insertCell().textContent = '';

    const nameInput = createInput('text', '예: 민욱');
    const themeSelect = createSelect();
    const dateInput = createInput('date');
    const timeSelect = createSelect();

    appendPlaceholder(themeSelect, '테마를 선택해주세요.');
    cachedThemes.forEach(theme => {
        const option = document.createElement('option');
        option.value = theme.id;
        option.textContent = theme.name;
        themeSelect.appendChild(option);
    });

    appendPlaceholder(timeSelect, '날짜와 테마를 먼저 선택해주세요.');
    timeSelect.disabled = true;

    row.insertCell().appendChild(nameInput);
    row.insertCell().appendChild(themeSelect);
    row.insertCell().appendChild(dateInput);
    row.insertCell().appendChild(timeSelect);

    const actions = row.insertCell();
    actions.className = 'actions';

    const saveButton = createButton('예약 등록', 'btn-primary', async () => {
        await saveRow(
            nameInput.value,
            dateInput.value,
            themeSelect.value,
            timeSelect.value,
            timeSelect
        );
    });
    actions.appendChild(saveButton);

    actions.appendChild(createButton('취소', 'btn-ghost', () => {
        row.remove();
        isEditing = false;
        refresh();
    }));

    const refreshTimesAndButton = async () => {
        await refreshAvailableTimes(dateInput.value, themeSelect.value, timeSelect);
        updateSaveButtonLabel(timeSelect, saveButton);
    };

    dateInput.addEventListener('change', refreshTimesAndButton);
    themeSelect.addEventListener('change', refreshTimesAndButton);
    timeSelect.addEventListener('change', () => updateSaveButtonLabel(timeSelect, saveButton));
}

async function refreshAvailableTimes(date, themeId, timeSelect) {
    clearSelect(timeSelect);

    if (!date || !themeId) {
        appendPlaceholder(timeSelect, '날짜와 테마를 먼저 선택해주세요.');
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

        let hasSelectedTime = false;

        times.forEach(time => {
            const option = document.createElement('option');

            const reserved = isReserved(time);

            option.value = time.id;
            option.dataset.reserved = String(reserved);
            option.textContent = reserved ? `${time.startAt} (대기 가능)` : time.startAt;
            option.className = reserved ? 'time-option-unavailable' : 'time-option-available';

            if (!reserved && !hasSelectedTime) {
                option.selected = true;
                hasSelectedTime = true;
            }

            timeSelect.appendChild(option);
        });

        if (!hasSelectedTime) {
            timeSelect.options[0].selected = true;
        }

        timeSelect.disabled = false;
    } catch (error) {
        console.error('예약 가능 시간 조회 실패:', error);
        clearSelect(timeSelect);
        appendPlaceholder(timeSelect, '예약 가능 시간 조회 실패');
        timeSelect.disabled = true;
    }
}

function isReserved(time) {
    return time.reserved === true;
}

function updateSaveButtonLabel(timeSelect, saveButton) {
    const selectedOption = timeSelect.options[timeSelect.selectedIndex];
    const isWaiting = selectedOption && selectedOption.dataset.reserved === 'true';
    saveButton.textContent = isWaiting ? '대기 신청' : '예약 등록';
}

async function saveRow(name, date, themeId, timeId, timeSelect) {
    if (!name.trim() || !date || !themeId || !timeId) {
        alert('모든 항목을 입력해주세요.');
        return;
    }

    const selectedOption = timeSelect.options[timeSelect.selectedIndex];
    const isWaiting = selectedOption && selectedOption.dataset.reserved === 'true';
    const endpoint = isWaiting ? WAITINGS_API : RESERVATION_ORDERS_API;

    try {
        const result = await fetchJson(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                name,
                date,
                themeId: Number(themeId),
                timeId: Number(timeId)
            })
        });

        isEditing = false;
        if (isWaiting) {
            alert('대기 신청이 완료되었습니다. 내 예약에서 대기 순번을 확인할 수 있습니다.');
            refresh();
            return;
        }
        window.location.href = `/payments/checkout?orderId=${encodeURIComponent(result.orderId)}`;
    } catch (error) {
        console.error(isWaiting ? '대기 신청 실패:' : '예약 주문 생성 실패:', error);
        alert(getErrorMessage(error, isWaiting ? '대기 신청에 실패했습니다.' : '예약 주문 생성에 실패했습니다.'));
    }
}

async function deleteRow(id) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
        await fetchJson(`${API}/${id}`, { method: 'DELETE' });
        refresh();
    } catch (error) {
        console.error('예약 삭제 실패:', error);
        alert(getErrorMessage(error, '예약 삭제에 실패했습니다.'));
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
