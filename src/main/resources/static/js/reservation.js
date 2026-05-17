const API = '/reservations';
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

    dateInput.addEventListener('change', () => {
        refreshAvailableTimes(dateInput.value, themeSelect.value, timeSelect);
    });

    themeSelect.addEventListener('change', () => {
        refreshAvailableTimes(dateInput.value, themeSelect.value, timeSelect);
    });

    row.insertCell().appendChild(nameInput);
    row.insertCell().appendChild(themeSelect);
    row.insertCell().appendChild(dateInput);
    row.insertCell().appendChild(timeSelect);

    const actions = row.insertCell();
    actions.className = 'actions';

    actions.appendChild(createButton('저장', 'btn-primary', async () => {
        await saveRow(
            nameInput.value,
            dateInput.value,
            themeSelect.value,
            timeSelect.value,
            timeSelect
        );
    }));

    actions.appendChild(createButton('취소', 'btn-ghost', () => {
        row.remove();
        isEditing = false;
        refresh();
    }));
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

        let hasAvailableTime = false;

        times.forEach(time => {
            const option = document.createElement('option');

            const reserved = isReserved(time);

            option.value = time.id;
            option.textContent = reserved ? `${time.startAt} (예약 불가)` : time.startAt;
            option.disabled = reserved;
            option.className = reserved ? 'time-option-unavailable' : 'time-option-available';

            if (!reserved && !hasAvailableTime) {
                option.selected = true;
                hasAvailableTime = true;
            }

            timeSelect.appendChild(option);
        });

        timeSelect.disabled = !hasAvailableTime;

        if (!hasAvailableTime) {
            alert('선택한 날짜와 테마에 예약 가능한 시간이 없습니다.');
        }
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

async function saveRow(name, date, themeId, timeId, timeSelect) {
    if (!name.trim() || !date || !themeId || !timeId) {
        alert('모든 항목을 입력해주세요.');
        return;
    }

    const selectedOption = timeSelect.options[timeSelect.selectedIndex];
    if (selectedOption && selectedOption.disabled) {
        alert('이미 예약된 시간은 선택할 수 없습니다.');
        return;
    }

    try {
        await fetchJson(API, {
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
        refresh();
    } catch (error) {
        console.error('예약 추가 실패:', error);
        alert(getErrorMessage(error, '예약 추가에 실패했습니다.'));
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
