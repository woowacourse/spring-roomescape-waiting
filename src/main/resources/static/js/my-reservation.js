(() => {
    const RESERVATIONS_API = '/reservations';
    const MY_TIMES_API = '/times';
    const LAST_SEARCH_NAME_KEY = 'roomescape:lastReservationName';

    let searchedName = '';
    let reservations = [];
    let editingReservationId = null;

    document.addEventListener('DOMContentLoaded', () => {
        const nameInput = document.getElementById('my-reservation-name-input');
        const searchButton = document.getElementById('my-reservation-search-button');
        const tableBody = document.getElementById('my-reservation-table-body');

        const lastName = localStorage.getItem(LAST_SEARCH_NAME_KEY);
        if (lastName) {
            nameInput.value = lastName;
        }

        searchButton.addEventListener('click', searchMyReservations);
        nameInput.addEventListener('keydown', event => {
            if (event.key === 'Enter') {
                searchMyReservations();
            }
        });

        showEmptyState(tableBody, 6, '예약자 이름을 입력하고 내 예약을 조회해보세요.');
    });

    async function searchMyReservations() {
        const nameInput = document.getElementById('my-reservation-name-input');
        const lookupMessage = document.getElementById('my-reservation-lookup-message');

        const name = nameInput.value.trim();

        if (!name) {
            alert('예약자 이름을 입력해주세요.');
            nameInput.focus();
            return;
        }

        try {
            searchedName = name;
            localStorage.setItem(LAST_SEARCH_NAME_KEY, name);

            editingReservationId = null;
            reservations = await fetchJson(`${RESERVATIONS_API}?name=${encodeURIComponent(name)}`);

            renderReservations();
            lookupMessage.textContent = `${name}님의 예약 ${reservations.length}건을 조회했습니다.`;
        } catch (error) {
            console.error('내 예약 조회 실패:', error);
            lookupMessage.textContent = '내 예약 조회에 실패했습니다.';
            alert(error.message);
        }
    }

    function renderReservations() {
        const tableBody = document.getElementById('my-reservation-table-body');
        tableBody.innerHTML = '';

        if (!reservations || reservations.length === 0) {
            showEmptyState(tableBody, 6, '조회된 예약이 없습니다. 예약자 이름을 다시 확인해주세요.');
            return;
        }

        reservations.forEach((reservation, index) => {
            if (reservation.id === editingReservationId) {
                renderEditRow(reservation, index);
                return;
            }

            renderReadRow(reservation, index);
        });
    }

    function renderReadRow(reservation, index) {
        const tableBody = document.getElementById('my-reservation-table-body');
        const row = tableBody.insertRow();

        row.insertCell().textContent = index + 1;
        row.insertCell().appendChild(createThemeSummary(reservation.theme));
        row.insertCell().textContent = reservation.name;
        row.insertCell().textContent = reservation.date;
        row.insertCell().textContent = reservation.time ? reservation.time.startAt : '-';

        const actions = row.insertCell();
        actions.className = 'actions';
        actions.appendChild(createButton('변경', 'btn-ghost', () => startEdit(reservation.id)));
        actions.appendChild(createButton('취소', 'btn-danger', () => cancelReservation(reservation)));
    }

    function renderEditRow(reservation, index) {
        const tableBody = document.getElementById('my-reservation-table-body');
        const row = tableBody.insertRow();

        row.insertCell().textContent = index + 1;
        row.insertCell().appendChild(createThemeSummary(reservation.theme));
        row.insertCell().textContent = reservation.name;

        const dateInput = createInput('date');
        dateInput.value = reservation.date;

        const timeSelect = createSelect();
        appendPlaceholder(timeSelect, '날짜를 선택해주세요.');

        dateInput.addEventListener('change', () => {
            refreshTimesForUpdate(reservation, dateInput.value, timeSelect);
        });

        row.insertCell().appendChild(dateInput);
        row.insertCell().appendChild(timeSelect);

        const actions = row.insertCell();
        actions.className = 'actions';
        actions.appendChild(createButton('저장', 'btn-primary', () => {
            updateReservation(reservation, dateInput.value, timeSelect.value);
        }));
        actions.appendChild(createButton('닫기', 'btn-ghost', stopEdit));

        refreshTimesForUpdate(reservation, dateInput.value, timeSelect);
    }

    function startEdit(reservationId) {
        if (editingReservationId !== null && editingReservationId !== reservationId) {
            const confirmed = confirm('작성 중인 변경 내용을 닫고 다른 예약을 변경하시겠습니까?');
            if (!confirmed) return;
        }

        editingReservationId = reservationId;
        renderReservations();
    }

    function stopEdit() {
        editingReservationId = null;
        renderReservations();
    }

    async function refreshTimesForUpdate(reservation, date, timeSelect) {
        clearSelect(timeSelect);

        if (!date) {
            appendPlaceholder(timeSelect, '날짜를 선택해주세요.');
            timeSelect.disabled = true;
            return;
        }

        if (!reservation.theme || !reservation.theme.id) {
            appendPlaceholder(timeSelect, '테마 정보를 확인할 수 없습니다.');
            timeSelect.disabled = true;
            return;
        }

        try {
            const times = await fetchJson(
                    `${MY_TIMES_API}?date=${encodeURIComponent(date)}&themeId=${encodeURIComponent(reservation.theme.id)}`
            );

            clearSelect(timeSelect);

            if (!times || times.length === 0) {
                appendPlaceholder(timeSelect, '등록된 시간이 없습니다.');
                timeSelect.disabled = true;
                return;
            }

            let hasSelectableTime = false;
            const currentTimeId = String(reservation.time.id);
            const isCurrentDate = date === reservation.date;

            times.forEach(time => {
                const option = document.createElement('option');

                const isCurrentReservationTime = isCurrentDate && String(time.id) === currentTimeId;
                const reservedByAnotherReservation = time.reserved === true && !isCurrentReservationTime;

                option.value = time.id;
                option.textContent = buildTimeOptionText(
                        time,
                        isCurrentReservationTime,
                        reservedByAnotherReservation
                );
                option.disabled = reservedByAnotherReservation;

                if (isCurrentReservationTime) {
                    option.selected = true;
                }

                if (!reservedByAnotherReservation) {
                    hasSelectableTime = true;
                }

                timeSelect.appendChild(option);
            });

            timeSelect.disabled = !hasSelectableTime;
        } catch (error) {
            console.error('변경 가능 시간 조회 실패:', error);
            clearSelect(timeSelect);
            appendPlaceholder(timeSelect, '변경 가능 시간 조회 실패');
            timeSelect.disabled = true;
            alert(error.message);
        }
    }

    function buildTimeOptionText(time, isCurrentReservationTime, reservedByAnotherReservation) {
        if (isCurrentReservationTime) {
            return `${time.startAt} (현재 예약)`;
        }

        if (reservedByAnotherReservation) {
            return `${time.startAt} (예약 불가)`;
        }

        return time.startAt;
    }

    async function updateReservation(reservation, date, timeId) {
        if (!date || !timeId) {
            alert('변경할 날짜와 시간을 선택해주세요.');
            return;
        }

        try {
            await fetchJson(
                    `${RESERVATIONS_API}/${reservation.id}?name=${encodeURIComponent(searchedName)}`,
                    {
                        method: 'PATCH',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({
                            date,
                            timeId: Number(timeId)
                        })
                    }
            );

            editingReservationId = null;
            await searchMyReservations();
        } catch (error) {
            console.error('내 예약 변경 실패:', error);
            alert(error.message);
        }
    }

    async function cancelReservation(reservation) {
        const themeName = reservation.theme ? reservation.theme.name : '선택한 테마';
        const time = reservation.time ? reservation.time.startAt : '';

        const confirmed = confirm(`${themeName} ${reservation.date} ${time} 예약을 취소하시겠습니까?`);
        if (!confirmed) return;

        try {
            await fetchJson(
                    `${RESERVATIONS_API}/${reservation.id}?name=${encodeURIComponent(searchedName)}`,
                    { method: 'DELETE' }
            );

            await searchMyReservations();
        } catch (error) {
            console.error('내 예약 취소 실패:', error);
            alert(error.message);
        }
    }

    function createThemeSummary(theme) {
        const wrapper = document.createElement('div');
        wrapper.className = 'theme-summary';

        if (theme && theme.thumbnailImageUrl) {
            const img = document.createElement('img');
            img.src = theme.thumbnailImageUrl;
            img.alt = theme.name;
            img.className = 'thumb';
            img.onerror = () => {
                img.style.display = 'none';
            };
            wrapper.appendChild(img);
        }

        const text = document.createElement('div');
        text.className = 'theme-summary-text';

        const name = document.createElement('strong');
        name.textContent = theme ? theme.name : '-';
        text.appendChild(name);

        if (theme && theme.description) {
            const description = document.createElement('span');
            description.className = 'muted';
            description.textContent = theme.description;
            text.appendChild(description);
        }

        wrapper.appendChild(text);
        return wrapper;
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
})();
