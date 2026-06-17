const ERROR_MESSAGES = {
  // Common
  "COMMON_001": "입력 데이터 형식이 올바르지 않습니다. 다시 확인해주세요.",
  "COMMON_002": "지원하지 않는 요청 방식입니다.",
  "COMMON_003": "요청을 처리할 수 없습니다.",
  "COMMON_004": "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
  "COMMON_005": "입력값이 올바르지 않습니다.",
  "COMMON_006": "입력값이 유효하지 않습니다. 내용을 다시 확인해주세요.",
  "COMMON_007": "데이터 처리 중 오류가 발생하여 요청을 완료할 수 없습니다.",

  // Reservation
  "RES_001": "예약 ID가 누락되어 요청을 처리할 수 없습니다.",
  "RES_002": "예약 회원 정보가 필요합니다.",
  "RES_003": "예약 날짜를 선택해주세요.",
  "RES_004": "예약 시간을 선택해주세요.",
  "RES_005": "테마를 선택해주세요.",
  "RES_006": "해당 예약 정보를 찾을 수 없습니다.",
  "RES_007": "선택하신 일정은 이미 예약이 완료되었습니다. 다른 시간을 선택해주세요.",
  "RES_008": "이미 취소된 예약은 변경하거나 다시 취소할 수 없습니다.",
  "RES_009": "본인의 예약만 취소하거나 변경할 수 있습니다.",
  "RES_010": "이미 지난 예약은 변경하거나 취소할 수 없습니다.",
  "RES_011": "과거 날짜나 시간으로는 예약할 수 없습니다.",
  "RES_012": "과거 날짜나 시간으로 일정을 변경할 수 없습니다.",
  "RES_013": "대기 중인 예약은 일정을 변경할 수 없습니다.",
};

async function handleResponseError(response, defaultMessage) {
  try {
    const errorData = await response.json();
    const errorCode = errorData.errorCode;
    const message = ERROR_MESSAGES[errorCode] || errorData.message
        || defaultMessage;
    alert(message);
  } catch (e) {
    alert(defaultMessage);
  }
}

let reschedulingReservation = null;
let selectedDate = null;
let selectedTime = null;

async function authFetch(url, options = {}) {
  const token = localStorage.getItem("token");
  if (!token) {
    location.href = "/";
    return;
  }

  const headers = {
    ...options.headers,
    "Authorization": `Bearer ${token}`
  };

  return fetch(url, {...options, headers});
}

document.addEventListener("DOMContentLoaded", async () => {
  if (!localStorage.getItem("token")) {
    location.href = "/";
    return;
  }
  await loadMyReservations();
});

async function loadMyReservations() {
  const response = await authFetch("/member/my-reservations");

  if (!response.ok) {
    await handleResponseError(response, "예약 내역을 불러오지 못했습니다.");
    return;
  }

  const reservations = await response.json();
  renderReservations(reservations);
}

function renderReservations(reservations) {
  const reservationResultList = document.getElementById(
      "reservation-result-list");
  reservationResultList.innerHTML = "";

  if (reservations.length === 0) {
    reservationResultList.innerHTML = `
            <div class="lookup-empty-message">
                예약 내역이 없습니다.
            </div>
        `;
    return;
  }

  reservations.forEach(reservation => {
    const article = document.createElement("article");
    article.className = "reservation-result-card";

    const isCanceled = reservation.status === "CANCELED";
    const isWaiting = reservation.status === "WAITING";
    const canReschedule = !isCanceled && !isWaiting;
    const waitingTurnText = isWaiting && reservation.waitingTurn
        ? `<p>대기 순번: ${reservation.waitingTurn}번째</p>`
        : "";

    article.innerHTML = `
            <div class="reservation-thumbnail-box">
                <img
                    class="reservation-thumbnail"
                    src="${reservation.themeThumbnailUrl}"
                    alt="${reservation.themeName}"
                >
            </div>

            <div class="reservation-result-info">
                <h3>${reservation.themeName}</h3>
                <p>날짜: ${reservation.date}</p>
                <p>시간: ${formatTime(reservation.time)}</p>
                <p>상태: ${formatStatus(reservation.status)}</p>
                ${waitingTurnText}

                <div class="button-group">
                    <button
                        class="reschedule-button"
                        type="button"
                        ${canReschedule ? "" : "style='display: none;'"}
                    >
                        예약 변경
                    </button>
                    <button
                        class="cancel-button"
                        type="button"
                        ${isCanceled ? "disabled" : ""}
                    >
                        ${isCanceled ? "취소 완료" : "예약 취소"}
                    </button>
                </div>
            </div>
        `;

    const cancelButton = article.querySelector(".cancel-button");
    const rescheduleButton = article.querySelector(".reschedule-button");

    if (!isCanceled) {
      cancelButton.addEventListener("click", async () => {
        await cancelReservation(reservation);
      });

      if (canReschedule) {
        rescheduleButton.addEventListener("click", () => {
          openRescheduleModal(reservation);
        });
      }
    }

    reservationResultList.appendChild(article);
  });
}

function openRescheduleModal(reservation) {
  if (reservation.status === "WAITING") {
    alert("대기 중인 예약은 일정을 변경할 수 없습니다.");
    return;
  }

  reschedulingReservation = reservation;
  selectedDate = null;
  selectedTime = null;

  document.getElementById(
      "reschedule-time-list").innerHTML = "<p style='color: #a9a9a9;'>날짜를 먼저 선택해주세요.</p>";
  document.getElementById("reschedule-modal").style.display = "block";

  loadRescheduleDates();
}

function closeRescheduleModal() {
  document.getElementById("reschedule-modal").style.display = "none";
  reschedulingReservation = null;
  selectedDate = null;
  selectedTime = null;
}

async function loadRescheduleDates() {
  const response = await authFetch("/member/dates");

  if (!response.ok) {
    await handleResponseError(response, "날짜 목록을 불러오지 못했습니다.");
    return;
  }

  const dates = await response.json();
  const dateList = document.getElementById("reschedule-date-list");
  dateList.innerHTML = "";

  dates.forEach(date => {
    const localDate = new Date(date.date);
    const month = localDate.toLocaleString("en-US",
        {month: "short"}).toUpperCase();
    const day = localDate.getDate();

    const button = document.createElement("button");
    button.className = "date-card";
    button.type = "button";
    button.innerHTML = `
            <span class="month">${month}</span>
            <span class="day">${day}</span>
        `;

    button.addEventListener("click", () => {
      document.querySelectorAll("#reschedule-date-list .date-card")
      .forEach(item => item.classList.remove("selected"));

      button.classList.add("selected");
      selectedDate = date;
      loadRescheduleTimes();
    });

    dateList.appendChild(button);
  });
}

async function loadRescheduleTimes() {
  const themeId = reschedulingReservation.themeId;
  const response = await authFetch(
      `/member/times?dateId=${selectedDate.id}&themeId=${themeId}`);

  if (!response.ok) {
    await handleResponseError(response, "예약 가능 시간을 불러오지 못했습니다.");
    return;
  }

  const times = await response.json();
  const timeList = document.getElementById("reschedule-time-list");
  timeList.innerHTML = "";
  selectedTime = null;

  if (times.length === 0) {
    timeList.innerHTML = `<p style="color: #b5b5b5;">해당 날짜에 예약 가능한 시간이 없습니다.</p>`;
    return;
  }

  times.forEach(time => {
    const button = document.createElement("button");
    button.className = "time-button";
    button.type = "button";
    button.textContent = formatTime(time.startAt);

    button.addEventListener("click", () => {
      document.querySelectorAll("#reschedule-time-list .time-button")
      .forEach(item => item.classList.remove("selected"));

      button.classList.add("selected");
      selectedTime = time;
    });

    timeList.appendChild(button);
  });
}

async function submitReschedule() {
  if (!selectedDate || !selectedTime) {
    alert("날짜와 시간을 모두 선택해주세요.");
    return;
  }

  const response = await authFetch(
      `/member/reservations/${reschedulingReservation.id}/schedule`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          dateId: selectedDate.id,
          timeId: selectedTime.id
        })
      });

  if (!response.ok) {
    await handleResponseError(response, "예약 변경에 실패했습니다.");
    return;
  }

  alert("예약이 변경되었습니다.");
  closeRescheduleModal();
  await loadMyReservations();
}

async function cancelReservation(reservation) {
  const confirmMessage = reservation.status === "WAITING"
      ? "예약 대기를 취소하시겠습니까?"
      : "예약을 취소하시겠습니까?";
  const confirmed = confirm(confirmMessage);
  if (!confirmed) {
    return;
  }

  const response = await authFetch(
      `/member/reservations/${reservation.id}/cancel`, {
        method: "PATCH"
      });

  if (!response.ok) {
    await handleResponseError(response, "예약 취소에 실패했습니다.");
    return;
  }

  alert("예약이 취소되었습니다.");
  await loadMyReservations();
}

function logout() {
  localStorage.removeItem("token");
  location.href = "/";
}

function formatTime(value) {
  if (!value) {
    return "";
  }
  const parts = value.split(":");
  return `${parts[0]}:${parts[1]}`;
}

function formatStatus(status) {
  if (status === "RESERVED") {
    return "예약 완료";
  }
  if (status === "WAITING") {
    return "예약 대기";
  }
  if (status === "CANCELED") {
    return "예약 취소";
  }
  return status;
}
