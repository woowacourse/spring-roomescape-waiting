const $ = (selector) => document.querySelector(selector);

const state = {
  themes: [],
  availableTimes: []
};

async function api(path, options = {}) {
  const { headers = {}, ...restOptions } = options;
  const mergedHeaders = {
    "Content-Type": "application/json",
    ...headers
  };

  const response = await fetch(path, {
    headers: mergedHeaders,
    ...restOptions
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => null);
    const msg = errorData?.message || "요청 처리에 실패했습니다.";
    window.alert(msg);
    throw new Error(msg);
  }

  if (response.status === 204) return null;
  return response.json();
}

function setMessage(message) {
  $("#message").textContent = message;
}

function renderThemeOptions() {
  const select = $("#createThemeId");
  select.innerHTML = '<option value="">테마 선택</option>';

  state.themes.forEach((theme) => {
    const option = document.createElement("option");
    option.value = theme.id;
    option.textContent = theme.name;
    select.appendChild(option);
  });
}

function renderAvailableTimes() {
  const root = $("#availableTimes");
  root.innerHTML = "";

  if (state.availableTimes.length === 0) {
    root.textContent = "예약 가능한 시간이 없습니다.";
    return;
  }

  state.availableTimes.forEach((time) => {
    const button = document.createElement("button");
    button.className = "chip";
    button.type = "button";
    button.dataset.timeId = time.id;
    if (time.alreadyBooked) {
      button.dataset.isWaiting = "true";
      button.textContent = `${time.startAt} 대기 신청`;
    } else {
      button.dataset.isWaiting = "false";
      button.textContent = `${time.startAt} 예약`;
    }
    root.appendChild(button);
  });
}

function renderReservations(reservations) {
  const root = $("#reservations");
  if (!reservations.length) {
    root.textContent = "아직 예약이 없습니다.";
    return;
  }

  root.innerHTML = "";

  reservations.forEach((reservation) => {
    const row = document.createElement("div");
    row.className = "reservation-row";
    const isWaiting = reservation.status === "waiting";
    const waitingText = isWaiting && reservation.waitingOrder ? `(대기 ${reservation.waitingOrder}번째)` : (isWaiting ? '(대기)' : '');
    const needsPayment = !isWaiting && reservation.orderId;
    row.innerHTML = `
      <span class="reservation-text">${reservation.id}. [${reservation.theme?.name ?? "테마 없음"}] ${reservation.date} ${reservation.time.startAt} - ${reservation.name} ${waitingText}${needsPayment ? ' (결제 대기)' : ''}</span>
      <div class="reservation-actions">
        ${needsPayment ? `<button class="ghost reservation-pay" data-order-id="${reservation.orderId}" type="button">결제하기</button>` : ''}
        ${!isWaiting ? `<button class="ghost reservation-update" data-id="${reservation.id}" data-theme-id="${reservation.theme?.id ?? ""}" type="button">변경</button>` : ''}
        <button class="danger reservation-delete" data-id="${reservation.id}" data-status="${reservation.status || 'reserved'}" type="button">삭제</button>
      </div>
    `;
    root.appendChild(row);
  });
}

function renderPopularThemes(popularThemes) {
  const list = $("#popularThemes");
  list.innerHTML = "";

  popularThemes.forEach((theme) => {
    const li = document.createElement("li");
    li.textContent = `${theme.name} - ${theme.description}`;
    list.appendChild(li);
  });
}

async function loadThemes() {
  state.themes = await api("/themes");
  renderThemeOptions();
}

async function loadReservations() {
  const name = $("#lookupName").value.trim();
  if (!name) {
    renderReservations([]);
    return;
  }

  const reservations = await api(`/reservations?name=${encodeURIComponent(name)}`);
  renderReservations(reservations);
}

async function loadPopularThemes() {
  const popular = await api("/themes?popular=true&period=7&limit=10");
  renderPopularThemes(popular);
}

async function loadAvailableTimes() {
  const date = $("#createDate").value;
  const themeId = $("#createThemeId").value;

  if (!date || !themeId) {
    setMessage("날짜와 테마를 먼저 선택해 주세요.");
    return;
  }

  state.availableTimes = await api(`/times/available-times?date=${date}&themeId=${themeId}`);
  renderAvailableTimes();
}

$("#loadTimes").addEventListener("click", async () => {
  try {
    await loadAvailableTimes();
    setMessage("예약 가능한 시간을 조회했습니다.");
  } catch (error) {
    setMessage(error.message);
  }
});

$("#loadReservations").addEventListener("click", async () => {
  const name = $("#lookupName").value.trim();
  if (!name) {
    setMessage("예약자 이름을 입력해 주세요.");
    renderReservations([]);
    return;
  }

  try {
    await loadReservations();
    setMessage("예약 내역을 조회했습니다.");
  } catch (error) {
    setMessage(error.message);
  }
});

$("#availableTimes").addEventListener("click", async (event) => {
  const button = event.target.closest("button[data-time-id]");
  if (!button) return;

  const name = $("#createName").value.trim();
  const date = $("#createDate").value;
  const themeId = $("#createThemeId").value;

  if (!name || !date || !themeId) {
    setMessage("예약자 이름, 날짜, 테마를 모두 입력해 주세요.");
    return;
  }

  try {
    const isWaiting = button.dataset.isWaiting === "true";
    const endpoint = isWaiting ? "/reservations-waitings" : "/reservations";

    const created = await api(endpoint, {
      method: "POST",
      body: JSON.stringify({
        name,
        date,
        timeId: Number(button.dataset.timeId),
        themeId: Number(themeId)
      })
    });

    if (!isWaiting && created.orderId) {
      window.location.href = `/payments/checkout?orderId=${encodeURIComponent(created.orderId)}`;
      return;
    }

    await loadAvailableTimes();
    await loadPopularThemes();
    $("#lookupName").value = name;
    await loadReservations();
    $("#reservationSuccess").textContent =
      `대기 성공: #${created.id} / [${created.theme?.name ?? "선택 테마"}] ${created.date} ${created.time.startAt} / ${created.name}`;
    setMessage("대기 신청이 정상적으로 완료되었습니다.");
  } catch (error) {
    setMessage(error.message);
  }
});

$("#loadPopular").addEventListener("click", async () => {
  try {
    await loadPopularThemes();
    setMessage("인기 테마를 갱신했습니다.");
  } catch (error) {
    setMessage(error.message);
  }
});

$("#reservations").addEventListener("click", async (event) => {
  const payButton = event.target.closest("button.reservation-pay");
  if (payButton) {
    window.location.href = `/payments/checkout?orderId=${encodeURIComponent(payButton.dataset.orderId)}`;
    return;
  }

  const button = event.target.closest("button[data-id]");
  if (!button) return;

  const reservationId = button.dataset.id;
  if (button.classList.contains("reservation-update")) {
    const themeId = button.dataset.themeId;
    window.location.href = `/reservation-update.html?id=${encodeURIComponent(reservationId)}&themeId=${encodeURIComponent(themeId)}`;
    return;
  }

  const status = button.dataset.status || 'reserved';
  window.location.href = `/reservation-cancel.html?id=${encodeURIComponent(reservationId)}&status=${encodeURIComponent(status)}`;
});

async function init() {
  try {
    await loadThemes();
    await loadPopularThemes();
    setMessage("초기 데이터 로딩 완료");
  } catch (error) {
    setMessage(error.message);
  }
}

init();
