(() => {
    const RESERVATIONS_API = '/reservations';
    const TIMES_API = '/times';
    const THEMES_API = '/themes';
    const LAST_SEARCH_NAME_KEY = 'roomescape:lastReservationName';

    document.addEventListener('DOMContentLoaded', () => {
        const nameInput = document.getElementById('reservation-name-input');
        const dateInput = document.getElementById('reservation-date-input');
        const themeSelect = document.getElementById('reservation-theme-select');
        const timeSelect = document.getElementById('reservation-time-select');
        const submitButton = document.getElementById('reservation-submit-button');

        const lastName = localStorage.getItem(LAST_SEARCH_NAME_KEY);
        if (lastName) {
            nameInput.value = lastName;
        }

        dateInput.min = getToday();
        appendPlaceholder(themeSelect, '테마를 불러오는 중입니다.');
        appendPlaceholder(timeSelect, '날짜와 테마를 먼저 선택해주세요.');
        timeSelect.disabled = true;

        dateInput.addEventListener('change', refreshTimes);
        themeSelect.addEventListener('change', refreshTimes);
        submitButton.addEventListener('click', submitReservation);

        loadThemes();
    });

    async function loadThemes() {
        const themeSelect = document.getElementById('reservation-theme-select');

        try {
            const themes = await fetchJson(THEMES_API);
            clearSelect(themeSelect);

            if (!themes || themes.length === 0) {
                appendPlaceholder(themeSelect, '등록된 테마가 없습니다.');
                themeSelect.disabled = true;
                setMessage('등록된 테마가 없습니다. 관리자에게 테마 등록을 요청해주세요.', true);
                return;
            }

            appendPlaceholder(themeSelect, '테마를 선택해주세요.');
            themes.forEach(theme => {
                const option = document.createElement('option');
                option.value = theme.id;
                option.textContent = theme.name;
                themeSelect.appendChild(option);
            });
            themeSelect.disabled = false;
        } catch (error) {
            console.error('테마 조회 실패:', error);
            clearSelect(themeSelect);
            appendPlaceholder(themeSelect, '테마 조회 실패');
            themeSelect.disabled = true;
            setMessage(error.message, true);
        }
    }

    async function refreshTimes() {
        const dateInput = document.getElementById('reservation-date-input');
        const themeSelect = document.getElementById('reservation-theme-select');
        const timeSelect = document.getElementById('reservation-time-select');

        clearSelect(timeSelect);

        if (!dateInput.value || !themeSelect.value) {
            appendPlaceholder(timeSelect, '날짜와 테마를 먼저 선택해주세요.');
            timeSelect.disabled = true;
            setMessage('날짜와 테마를 선택하면 신청 가능한 시간이 표시됩니다.', false);
            return;
        }

        try {
            const times = await fetchJson(
                    `${TIMES_API}?date=${encodeURIComponent(dateInput.value)}&themeId=${encodeURIComponent(themeSelect.value)}`
            );

            clearSelect(timeSelect);

            if (!times || times.length === 0) {
                appendPlaceholder(timeSelect, '등록된 시간이 없습니다.');
                timeSelect.disabled = true;
                setMessage('선택한 날짜와 테마에 등록된 시간이 없습니다.', true);
                return;
            }

            times.forEach((time, index) => {
                const option = document.createElement('option');
                const reserved = time.reserved === true;

                option.value = time.id;
                option.textContent = reserved ? `${time.startAt} (대기 신청)` : `${time.startAt} (예약 가능)`;
                option.className = reserved ? 'time-option-waiting' : 'time-option-available';
                option.dataset.reserved = String(reserved);

                if (index === 0) {
                    option.selected = true;
                }

                timeSelect.appendChild(option);
            });

            timeSelect.disabled = false;
            setMessage('예약 가능 시간은 바로 예약되고, 이미 예약된 시간은 대기로 신청됩니다.', false);
        } catch (error) {
            console.error('시간 조회 실패:', error);
            clearSelect(timeSelect);
            appendPlaceholder(timeSelect, '시간 조회 실패');
            timeSelect.disabled = true;
            setMessage(error.message, true);
        }
    }

    async function submitReservation() {
        const nameInput = document.getElementById('reservation-name-input');
        const dateInput = document.getElementById('reservation-date-input');
        const themeSelect = document.getElementById('reservation-theme-select');
        const timeSelect = document.getElementById('reservation-time-select');

        const name = nameInput.value.trim();
        const date = dateInput.value;
        const themeId = themeSelect.value;
        const timeId = timeSelect.value;

        if (!name || !date || !themeId || !timeId) {
            alert('예약자 이름, 날짜, 테마, 시간을 모두 입력해주세요.');
            return;
        }

        try {
            const response = await fetchJson(RESERVATIONS_API, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name,
                    date,
                    themeId: Number(themeId),
                    timeId: Number(timeId)
                })
            });

            localStorage.setItem(LAST_SEARCH_NAME_KEY, name);
            setCreationMessage(response);
            fillLookupName(name);
        } catch (error) {
            console.error('예약 신청 실패:', error);
            setMessage(error.message, true);
            alert(error.message);
        }
    }

    function setCreationMessage(response) {
        if (response && response.status === 'WAITING') {
            setMessage(`${response.name}님은 대기로 등록되었습니다. 내 예약 조회에서 현재 순번을 확인해주세요.`, false);
            return;
        }

        setMessage(`${response.name}님의 예약이 확정되었습니다. 내 예약 조회에서 확인할 수 있습니다.`, false);
    }

    function fillLookupName(name) {
        const lookupInput = document.getElementById('my-reservation-name-input');
        if (lookupInput) {
            lookupInput.value = name;
        }
    }

    function setMessage(message, isError) {
        const messageElement = document.getElementById('reservation-form-message');
        messageElement.textContent = message;
        messageElement.classList.toggle('form-message-error', isError);
        messageElement.classList.toggle('muted', !isError);
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

    function getToday() {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }
})();
