const THEME_API_ENDPOINT = '/themes';

document.addEventListener('DOMContentLoaded', () => {
    requestRead(THEME_API_ENDPOINT)
        .then(renderTheme)
        .catch(error => console.error('Error fetching times:', error));

    flatpickr("#datepicker", {
        inline: true,
        onChange: function (selectedDates, dateStr) {
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
        if (event.target.classList.contains('time-slot') && !event.target.classList.contains('disabled')) {
            document.querySelectorAll('.time-slot').forEach(slot => slot.classList.remove('active'));
            event.target.classList.add('active');
            checkDateAndThemeAndTime();
        }
    });

    document.getElementById('reserve-button').addEventListener('click', onReservationButtonClick);
    document.getElementById('wait-button').addEventListener('click', onWaitButtonClick);
});

function renderTheme(themes) {
    const themeSlots = document.getElementById('theme-slots');
    themeSlots.innerHTML = '';
    themes.forEach(theme => {
        themeSlots.appendChild(createSlot('theme', theme.name, theme.id));
    });
}

function createSlot(type, text, id, booked) {
    const div = document.createElement('div');
    div.className = type + '-slot cursor-pointer bg-light border rounded p-3 mb-2';
    div.textContent = text;
    div.setAttribute('data-' + type + '-id', id);
    if (type === 'time') {
        div.setAttribute('data-time-booked', booked);
    }
    return div;
}

function checkDate() {
    const selectedDate = document.getElementById("datepicker").value;
    if (selectedDate) {
        document.getElementById("theme-section").classList.remove("disabled");
        document.getElementById('time-slots').innerHTML = '';
        requestRead(THEME_API_ENDPOINT).then(renderTheme);
    }
}

function checkDateAndTheme() {
    const date = document.getElementById("datepicker").value;
    const theme = document.querySelector('.theme-slot.active');
    if (date && theme) {
        const themeId = theme.getAttribute('data-theme-id');
        fetchAvailableTimes(date, themeId);
    }
}

function fetchAvailableTimes(date, themeId) {
    fetch(`/times/available?date=${date}&themeId=${themeId}`, {
        method: 'GET',
        headers: {'Content-Type': 'application/json'}
    })
        .then(res => res.ok ? res.json() : Promise.reject('Read failed'))
        .then(renderAvailableTimes)
        .catch(console.error);
}

function renderAvailableTimes(times) {
    document.getElementById("time-section").classList.remove("disabled");

    const timeSlots = document.getElementById('time-slots');
    timeSlots.innerHTML = '';

    if (times.length === 0) {
        timeSlots.innerHTML = '<div class="no-times">선택할 수 있는 시간이 없습니다.</div>';
        return;
    }

    times.forEach(time => {
        timeSlots.appendChild(createSlot('time', time.startAt, time.timeId, time.alreadyBooked));
    });
}

function checkDateAndThemeAndTime() {
    const selectedDate = document.getElementById("datepicker").value;
    const selectedThemeElement = document.querySelector('.theme-slot.active');
    const selectedTimeElement = document.querySelector('.time-slot.active');
    const reserveButton = document.getElementById("reserve-button");
    const waitButton = document.getElementById("wait-button");

    if (selectedDate && selectedThemeElement && selectedTimeElement) {
        if (selectedTimeElement.getAttribute('data-time-booked') === 'true') {
            // 선택된 시간이 이미 예약된 경우
            reserveButton.classList.add("disabled");
            waitButton.classList.remove("disabled"); // 예약 대기 버튼 활성화
        } else {
            // 선택된 시간이 예약 가능한 경우
            reserveButton.classList.remove("disabled");
            waitButton.classList.add("disabled"); // 예약 대기 버튼 비활성화
        }
    } else {
        // 날짜, 테마, 시간 중 하나라도 선택되지 않은 경우
        reserveButton.classList.add("disabled");
        waitButton.classList.add("disabled");
    }
}

function onReservationButtonClick() {
    const date = document.getElementById("datepicker").value;
    const themeId = document.querySelector('.theme-slot.active')?.getAttribute('data-theme-id');
    const timeId = document.querySelector('.time-slot.active')?.getAttribute('data-time-id');

    if (date && themeId && timeId) {
        const reservationData = {date, themeId, timeId};

        fetch('/reservations', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            credentials: 'include', // 중요: 쿠키 기반 인증을 위해 필요
            body: JSON.stringify(reservationData)
        })
            .then(res => res.ok ? res.json() : Promise.reject('Reservation failed'))
            .then(() => {
                alert("Reservation successful!");
                location.reload();
            })
            .catch(err => {
                alert("예약 중 오류가 발생했습니다.");
                console.error(err);
            });
    } else {
        alert("날짜, 테마, 시간을 모두 선택해주세요.");
    }
}

function onWaitButtonClick() {
    const selectedDate = document.getElementById("datepicker").value;
    const selectedThemeId = document.querySelector('.theme-slot.active')?.getAttribute('data-theme-id');
    const selectedTimeId = document.querySelector('.time-slot.active')?.getAttribute('data-time-id');

    if (selectedDate && selectedThemeId && selectedTimeId) {
        const reservationData = {
            date: selectedDate,
            theme: selectedThemeId,
            time: selectedTimeId
        };

        /*
        TODO: [3단계] 예약 대기 생성 요청 API 호출
         */
        fetch('/waiting', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(reservationData)
        })
            .then(response => {
                if (!response.ok) throw new Error('Reservation waiting failed');
                return response.json();
            })
            .then(data => {
                alert('Reservation waiting successful!');
                window.location.href = "/";
            })
            .catch(error => {
                alert("An error occurred while making the reservation waiting.");
                console.error(error);
            });
    } else {
        alert("Please select a date, theme, and time before making a reservation waiting.");
    }
}

function requestRead(endpoint) {
    return fetch(endpoint)
        .then(res => res.ok ? res.json() : Promise.reject('Read failed'));
}
