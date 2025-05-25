const THEME_API_ENDPOINT = '/themes';
const TIME_API_ENDPOINT = '/times';

document.addEventListener('DOMContentLoaded', () => {
    requestRead(THEME_API_ENDPOINT)
        .then(renderTheme)
        .catch(error => console.error('Error fetching times:', error));

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
        const name = theme.name;
        const themeId = theme.id;

        themeSlots.appendChild(createSlot('theme', name, themeId));
    });
}

function createSlot(type, text, id, booked) {
    const div = document.createElement('div');
    div.className = type + '-slot cursor-pointer bg-light border rounded p-3 mb-2';
    div.textContent = text;

    // 디버깅: ID 값 확인
    console.log(`Creating ${type} slot with id: ${id}, type: ${typeof id}`);

    // id가 null이나 undefined인 경우 빈 문자열로 설정하여 'undefined' 문자열 방지
    id = id || '';

    div.setAttribute('data-' + type + '-id', id);
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
        const availableTimeRequest = {
            date: selectedDate,
            themeId: selectedThemeId
        }

        fetchAvailableTimes(selectedDate, selectedThemeId);
    }
}


function fetchAvailableTimes(selectedDate, selectedThemeId) {
    const url = `${TIME_API_ENDPOINT}?date=${selectedDate}&themeId=${selectedThemeId}`;

    fetch(url, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    })
        .then(response => {
            if (response.status === 200) return response.json();
            throw new Error('Read failed');
        })
        .then(renderAvailableTimes)
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

    console.log("Available times:", times); // 디버깅: 서버에서 받은 시간 데이터 로깅

    times.forEach(time => {
        // API 응답 구조 전체 확인
        console.log("Time object structure:", JSON.stringify(time));

        // time 객체에서 필요한 속성을 추출
        // API가 다양한 형태로 응답할 수 있으므로 여러 속성을 확인
        const startAt = time.startAt;

        // timeId 필드를 여러 가능한 위치에서 찾기
        let timeId;
        if (time.timeId !== undefined) {
            timeId = time.timeId;
        } else if (time.id !== undefined) {
            timeId = time.id;
        } else if (time.time && time.time.id !== undefined) {
            timeId = time.time.id;
        } else {
            console.error("Cannot find timeId in the time object:", time);
            timeId = null; // 혹은 다른 fallback 값
        }

        const alreadyBooked = time.isBooked;

        console.log(`Time slot: startAt=${startAt}, id=${timeId}, booked=${alreadyBooked}`); // 디버깅 로깅

        if (timeId) {
            const div = createSlot('time', startAt, timeId, alreadyBooked);
            timeSlots.appendChild(div);
        } else {
            console.error("Skipping time slot due to missing timeId");
        }
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
    const selectedDate = document.getElementById("datepicker").value;
    const selectedThemeId = document.querySelector('.theme-slot.active')?.getAttribute('data-theme-id');
    const selectedTimeId = document.querySelector('.time-slot.active')?.getAttribute('data-time-id');

    console.log("Selected values:", {date: selectedDate, themeId: selectedThemeId, timeId: selectedTimeId}); // 디버깅
    console.log("Selected time element:", document.querySelector('.time-slot.active')); // 추가 디버깅

    // time-slot 요소들 전체 확인
    const allTimeSlots = document.querySelectorAll('.time-slot');
    console.log("All time slots:", allTimeSlots);
    allTimeSlots.forEach((slot, index) => {
        console.log(`Slot ${index}:`, slot, `data-time-id: ${slot.getAttribute('data-time-id')}`);
    });

    // 값 검증
    if (!selectedDate) {
        alert("날짜를 선택해주세요.");
        return;
    }

    if (!selectedThemeId) {
        alert("테마를 선택해주세요.");
        return;
    }

    if (!selectedTimeId || selectedTimeId === "undefined" || selectedTimeId === "null") {
        alert("시간을 선택해주세요.");
        return;
    }

    // 숫자 문자열을 숫자로 변환 (API 요구사항에 맞게)
    const timeIdNumber = parseInt(selectedTimeId, 10);
    const themeIdNumber = parseInt(selectedThemeId, 10);

    // 모든 값이 유효한 경우
    const reservationData = {
        date: selectedDate,
        themeId: isNaN(themeIdNumber) ? selectedThemeId : themeIdNumber,
        timeId: isNaN(timeIdNumber) ? selectedTimeId : timeIdNumber
    };

    console.log("Sending reservation data:", reservationData); // 디버깅

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
            alert("Reservation successful!");
            location.reload();
        })
        .catch(error => {
            alert("An error occurred while making the reservation.");
            console.error(error);
        });
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
        fetch('', {
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
        .then(response => {
            if (response.status === 200) return response.json();
            throw new Error('Read failed');
        });
}
