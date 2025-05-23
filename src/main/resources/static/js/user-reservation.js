const THEME_API_ENDPOINT = '/themes';

document.addEventListener('DOMContentLoaded', () => {
    requestRead(THEME_API_ENDPOINT)
        .then(renderTheme)
        .catch(error => console.error('Error fetching themes:', error));

    flatpickr("#datepicker", {
        inline: true,
        onChange: function (selectedDates, dateStr, instance) {
            if (dateStr === '') return;
            checkDate();
        }
    });

    document.getElementById('theme-slots').addEventListener('click', event => {
        if (event.target.classList.contains('theme-slot')) {
            document.querySelectorAll('.theme-slot').forEach(slot => slot.classList.remove('active'));
            event.target.classList.add('active');
            checkDateAndTheme();
        }
    });

    document.getElementById('time-slots').addEventListener('click', event => {
        if (event.target.classList.contains('time-slot')) {
            document.querySelectorAll('.time-slot').forEach(slot => slot.classList.remove('active'));
            event.target.classList.add('active');
            checkDateAndThemeAndTime();
        }
    });

    document.getElementById('reserve-button').addEventListener('click', onReservationButtonClick);
});

function renderTheme(themes) {
    const themeSlots = document.getElementById('theme-slots');
    themeSlots.innerHTML = '';
    themes.forEach(theme => {
        const name = theme.name;
        const themeId = theme.id;
        themeSlots.appendChild(createSlot('theme', name, themeId));
    });
}

function createSlot(type, text, id, booked = false) {
    const div = document.createElement('div');
    div.className = `${type}-slot cursor-pointer bg-light border rounded p-3 mb-2`;
    if (type === 'time' && booked) {
        div.classList.add('booked');
    }
    div.textContent = text;
    div.setAttribute(`data-${type}-id`, id);
    if (type === 'time') {
        div.setAttribute('data-time-booked', booked);
    }
    return div;
}

function checkDate() {
    const selectedDate = document.getElementById("datepicker").value;
    if (selectedDate) {
        const themeSection = document.getElementById("theme-section");
        if (themeSection.classList.contains("disabled")) {
            themeSection.classList.remove("disabled");
        }
        const timeSlots = document.getElementById('time-slots');
        timeSlots.innerHTML = '';

        requestRead(THEME_API_ENDPOINT)
            .then(renderTheme)
            .catch(error => console.error('Error fetching times:', error));
    }
}

function checkDateAndTheme() {
    const selectedDate = document.getElementById("datepicker").value;
    const selectedThemeElement = document.querySelector('.theme-slot.active');
    if (selectedDate && selectedThemeElement) {
        const selectedThemeId = selectedThemeElement.getAttribute('data-theme-id');
        fetchAvailableTimes(selectedDate, selectedThemeId);
    }
}

function fetchAvailableTimes(date, themeId) {
    fetch(`/times/available?date=${date}&themeId=${themeId}`, { // 예약 가능 시간 조회 API endpoint
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    }).then(response => {
        if (response.ok) return response.json();
        throw new Error('Read failed');
    }).then(renderAvailableTimes)
        .catch(error => console.error("Error fetching available times:", error));
}

function renderAvailableTimes(times) {
    const timeSection = document.getElementById("time-section");
    if (timeSection.classList.contains("disabled")) {
        timeSection.classList.remove("disabled");
    }

    const timeSlots = document.getElementById('time-slots');
    timeSlots.innerHTML = '';
    if (times.length === 0) {
        timeSlots.innerHTML = '<div class="no-times">선택할 수 있는 시간이 없습니다.</div>';
        return;
    }
    times.forEach(time => {
        const startAt = time.startAt;
        const timeId = time.timeId;
        const alreadyBooked = time.alreadyBooked;

        const div = createSlot('time', startAt, timeId, alreadyBooked); // createSlot('time', 시작 시간, time id, 예약 여부)
        timeSlots.appendChild(div);
    });
}

function checkDateAndThemeAndTime() {
    const selectedDate = document.getElementById("datepicker").value;
    const selectedThemeElement = document.querySelector('.theme-slot.active');
    const selectedTimeElement = document.querySelector('.time-slot.active');
    const reserveButton = document.getElementById("reserve-button");

    if (selectedDate && selectedThemeElement && selectedTimeElement) {
            reserveButton.classList.remove("disabled");

    } else {
        reserveButton.classList.add("disabled");
    }
}

function onReservationButtonClick() {
    const selectedDate = document.getElementById("datepicker").value;
    const selectedThemeId = document.querySelector('.theme-slot.active')?.getAttribute('data-theme-id');
    const selectedTimeId = document.querySelector('.time-slot.active')?.getAttribute('data-time-id');

    if (selectedDate && selectedThemeId && selectedTimeId) {
        const reservationData = {
            date: selectedDate,
            themeId: selectedThemeId,
            timeId: selectedTimeId,
        };

        fetch('/reservations', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(reservationData)
        })
            .then(response => {
                if (!response.ok) throw new Error('Reservation failed');
                return response.json();
            })
            .then(data => {
                if (data.reservedStatus === '예약') {
                    alert("Reservation successful!");
                    location.reload();
                } else {
                    alert("이미 예약이 있어, 대기 목록에 등록되었습니다.");
                }
                location.reload();
            })
            .catch(error => {
                alert("An error occurred while making the reservation.");
                console.error(error);
            });
    } else {
        alert("Please select a date, theme, and time before making a reservation waiting.");
    }
}


function requestRead(endpoint) {
    return fetch(endpoint)
        .then(response => {
            if (response.status === 200) return response.json();
            throw new Error('Read failed');
        });
}
