const THEME_API_ENDPOINT = '/themes';

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
  fetch(`/times/available?date=${date}&themeId=${themeId}`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
  }).then(response => {
    if (response.status === 200) return response.json();
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
    const div = createSlot('time', time.startAt, time.timeId, time.alreadyBooked);
    timeSlots.appendChild(div);
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
          alert("Reservation successful!");
          location.reload();
        })
        .catch(error => {
          alert("An error occurred while making the reservation.");
          console.error(error);
        });
  } else {
    alert("Please select a date, theme, and time before making a reservation.");
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
