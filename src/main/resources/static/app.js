const $ = (selector) => document.querySelector(selector);

const state = {
  themes: [],
  times: [],
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
    const text = await response.text();
    throw new Error(text || "요청 처리에 실패했습니다.");
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

  if (state.times.length === 0) {
    root.textContent = "등록된 시간이 없습니다.";
    return;
  }

  const availableTimeIds = new Set(state.availableTimes.map((time) => time.id));

  state.times.forEach((time) => {
    const isAvailable = availableTimeIds.has(time.id);
    const button = document.createElement("button");
    button.className = isAvailable ? "chip" : "chip chip-waiting";
    button.type = "button";
    button.dataset.timeId = time.id;
    button.dataset.action = isAvailable ? "reserve" : "wait";
    button.textContent = `${time.startAt} ${isAvailable ? "예약" : "대기"}`;
    root.appendChild(button);
  });
}

function statusLabel(reservation) {
  if (reservation.status === "waiting") {
    return `대기 ${reservation.waitingOrder}번째`;
  }

  return "예약 완료";
}

function renderReservations(reservations) {
  const root = $("#reservations");
  if (!reservations.length) {
    root.textContent = "아직 예약이 없습니다.";
    return;
  }

  root.innerHTML = "";

  reservations.forEach((reservation) => {
    const isWaiting = reservation.status === "waiting";
    const row = document.createElement("div");
    row.className = `reservation-row ${isWaiting ? "reservation-waiting" : ""}`;
    row.innerHTML = `
      <span class="reservation-text">
        <span class="status-badge ${isWaiting ? "status-waiting" : "status-reserved"}">${statusLabel(reservation)}</span>
        ${reservation.id}. [${reservation.theme?.name ?? "테마 없음"}] ${reservation.date} ${reservation.time.startAt} - ${reservation.name}
      </span>
      <div class="reservation-actions">
        ${isWaiting ? "" : `<button class="ghost reservation-update" data-id="${reservation.id}" data-theme-id="${reservation.theme?.id ?? ""}" type="button">변경</button>`}
        <button class="danger reservation-delete" data-id="${reservation.id}" data-type="${isWaiting ? "waiting" : "reservation"}" type="button">삭제</button>
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

async function loadTimes() {
  state.times = await api("/times");
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

  const [times, availableTimes] = await Promise.all([
    api("/times"),
    api(`/times/available-times?date=${date}&themeId=${themeId}`)
  ]);
  state.times = times;
  state.availableTimes = availableTimes;
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
    const isWaiting = button.dataset.action === "wait";
    const created = await api(isWaiting ? "/reservation-waitings" : "/reservations", {
      method: "POST",
      body: JSON.stringify({
        name,
        date,
        timeId: Number(button.dataset.timeId),
        themeId: Number(themeId)
      })
    });

    await loadAvailableTimes();
    await loadPopularThemes();
    $("#lookupName").value = name;
    await loadReservations();
    $("#reservationSuccess").textContent =
      `${isWaiting ? "예약 대기 신청" : "예약"} 성공: #${created.id} / [${created.theme?.name ?? "선택 테마"}] ${created.date} ${created.time.startAt} / ${created.name}`;
    setMessage(isWaiting ? "예약 대기 신청이 완료되었습니다." : "예약이 정상적으로 완료되었습니다.");
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
  const button = event.target.closest("button[data-id]");
  if (!button) return;

  const reservationId = button.dataset.id;
  if (button.classList.contains("reservation-update")) {
    const themeId = button.dataset.themeId;
    window.location.href = `/reservation-update.html?id=${encodeURIComponent(reservationId)}&themeId=${encodeURIComponent(themeId)}`;
    return;
  }

  const type = button.dataset.type ?? "reservation";
  window.location.href = `/reservation-cancel.html?id=${encodeURIComponent(reservationId)}&type=${encodeURIComponent(type)}`;
});

async function init() {
  try {
    await loadThemes();
    await loadTimes();
    await loadPopularThemes();
    setMessage("초기 데이터 로딩 완료");
  } catch (error) {
    setMessage(error.message);
  }
}

init();
