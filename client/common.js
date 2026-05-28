const API_BASE = new URLSearchParams(window.location.search).get("apiBase") || "http://localhost:8080/api";
const statusEl = document.getElementById("status");
const reservationSubmitEl = document.getElementById("reservation-submit");
const selectedThemeLabelEl = document.getElementById("selected-theme-label");
const selectedTimeLabelEl = document.getElementById("selected-time-label");
const resultModalEl = document.getElementById("result-modal");
const resultTitleEl = document.getElementById("result-title");
const resultMessageEl = document.getElementById("result-message");
const resultOkEl = document.getElementById("result-ok");
const myInfoOpenEl = document.getElementById("my-info-open");
const authStatusEl = document.getElementById("auth-status");
const loginOpenEl = document.getElementById("login-open");
const loginFormEl = document.getElementById("login-form");
const loginNameEl = document.getElementById("login-name");
const loginPasswordEl = document.getElementById("login-password");
const loginModalEl = document.getElementById("login-modal");
const loginCancelEl = document.getElementById("login-cancel");
const confirmModalEl = document.getElementById("confirm-modal");
const confirmTitleEl = document.getElementById("confirm-title");
const confirmMessageEl = document.getElementById("confirm-message");
const confirmOkEl = document.getElementById("confirm-ok");
const confirmCancelEl = document.getElementById("confirm-cancel");
const myReservationOwnerEl = document.getElementById("my-reservation-owner");
const myReservationCardsEl = document.getElementById("my-reservation-cards");
const myReservationSectionEl = document.getElementById("my-reservation-section");
const editReservationModalEl = document.getElementById("edit-reservation-modal");
const editReservationFormEl = document.getElementById("edit-reservation-form");
const editReservationIdEl = document.getElementById("edit-reservation-id");
const editReservationDateEl = document.getElementById("edit-reservation-date");
const editReservationThemeEl = document.getElementById("edit-reservation-theme");
const editAvailableTimesEl = document.getElementById("edit-available-times");
const editReservationCancelEl = document.getElementById("edit-reservation-cancel");
const AUTH_TOKEN_KEY = "roomescapeAccessToken";
const DEMO_DATE = "2026-05-05";
let selectedThemeId = null;
let selectedTimeId = null;
let selectedTimeLabel = null;
let selectedThemeName = null;
let editingReservation = null;
let selectedEditTimeId = null;
let currentLoginName = null;
let isAuthenticated = false;

function getAccessToken() {
  return localStorage.getItem(AUTH_TOKEN_KEY);
}

function setAccessToken(token) {
  if (!token) return;
  localStorage.setItem(AUTH_TOKEN_KEY, token);
}

function clearAccessToken() {
  localStorage.removeItem(AUTH_TOKEN_KEY);
}

function setCommonAuthState(authenticated) {
  isAuthenticated = authenticated;
  loginOpenEl.textContent = authenticated ? "로그아웃" : "로그인";
  const selectors = [
    "#theme-by-date-form input",
    "#theme-by-date-form button",
    "#reservation-form button",
    "#reservation-form input",
  ];
  selectors.forEach((selector) => {
    document.querySelectorAll(selector).forEach((el) => {
      el.disabled = !authenticated;
    });
  });
  reservationSubmitEl.disabled = !authenticated || !selectedTimeId;

  if (!authenticated) {
    selectedThemeId = null;
    selectedThemeName = null;
    selectedTimeId = null;
    selectedTimeLabel = null;
    selectedThemeLabelEl.textContent = "테마: 미선택";
    selectedTimeLabelEl.textContent = "시간: 미선택";
    myReservationOwnerEl.textContent = "로그인 후 조회됩니다.";
    myReservationCardsEl.innerHTML = "";
    myReservationSectionEl.classList.add("hidden");
    reservationSubmitEl.disabled = true;
    renderAvailableTimes([]);
  }
}

function setStatus(message, isError = false) {
  if (!statusEl) return;
  statusEl.textContent = message;
  statusEl.style.color = isError ? "#dc2626" : "#065f46";
}

async function api(path, options = {}) {
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
  const token = getAccessToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  const response = await fetch(`${API_BASE}${path}`, {
    headers,
    ...options,
  });
  if (response.status === 204) return null;

  const raw = await response.text();
  let body = null;
  if (raw) {
    try {
      body = JSON.parse(raw);
    } catch (error) {
      body = null;
    }
  }

  if (!response.ok || body?.success === false) {
    const message = body?.error?.message || `요청 실패: ${response.status}`;
    throw new Error(message);
  }
  return body?.data ?? null;
}

function showResultModal({ title, message, isError = false }) {
  return new Promise((resolve) => {
    const card = resultModalEl.querySelector(".modal-card");
    card.classList.remove("success", "error");
    card.classList.add(isError ? "error" : "success");
    resultTitleEl.textContent = title;
    resultMessageEl.textContent = message;
    resultModalEl.classList.remove("hidden");
    resultOkEl.onclick = () => {
      resultModalEl.classList.add("hidden");
      resultOkEl.onclick = null;
      resolve();
    };
  });
}

function confirmAction({ title, message, okLabel = "확인", okDanger = false }) {
  return new Promise((resolve) => {
    confirmTitleEl.textContent = title;
    confirmMessageEl.textContent = message;
    confirmOkEl.textContent = okLabel;
    confirmOkEl.classList.toggle("danger-button", okDanger);
    confirmModalEl.classList.remove("hidden");

    const cleanup = () => {
      confirmModalEl.classList.add("hidden");
      confirmOkEl.onclick = null;
      confirmCancelEl.onclick = null;
    };

    confirmOkEl.onclick = () => {
      cleanup();
      resolve(true);
    };
    confirmCancelEl.onclick = () => {
      cleanup();
      resolve(false);
    };
  });
}

async function showErrorModal(title, error) {
  const message = error?.message || "알 수 없는 오류가 발생했습니다.";
  setStatus(message, true);
  await showResultModal({
    title,
    message,
    isError: true,
  });
}

function renderThemeList(themes) {
  const list = document.getElementById("theme-list");
  list.innerHTML = "";
  if (!themes.length) {
    const li = document.createElement("li");
    li.textContent = "선택한 날짜에 예약 가능한 테마가 없습니다.";
    li.className = "empty-message";
    list.appendChild(li);
    return;
  }

  themes.forEach((theme) => {
    const li = document.createElement("li");
    li.classList.add("theme-item");
    li.dataset.id = String(theme.id);
    li.dataset.name = theme.name;
    li.tabIndex = 0;
    li.setAttribute("role", "button");
    li.innerHTML = `
      <img class="theme-thumbnail" src="${theme.thumbnailUrl}" alt="${theme.name} 썸네일">
      <button class="theme-select" data-id="${theme.id}" type="button">${theme.name}</button>
      - ${theme.description}
      <span class="muted">(ID: ${theme.id})</span>
    `;
    list.appendChild(li);
  });
}

function renderAvailableTimes(items) {
  const wrapper = document.getElementById("available-times");
  wrapper.innerHTML = "";
  selectedTimeId = null;
  selectedTimeLabel = null;
  selectedTimeLabelEl.textContent = "시간: 미선택";
  reservationSubmitEl.disabled = true;
  if (!items.length) {
    const empty = document.createElement("span");
    empty.className = "empty-message";
    empty.textContent = "조회된 예약 가능 시간이 없습니다.";
    wrapper.appendChild(empty);
    return;
  }

  items.forEach((item) => {
    const button = document.createElement("button");
    const timeId = item.timeInformation.id;
    const timeLabel = item.timeInformation.time;
    button.type = "button";
    button.className = `chip${item.isAvailable ? "" : " waitable"}`;
    button.textContent = `${timeLabel} (${item.isAvailable ? "예약 가능" : "대기 가능"})`;
    if (!item.isAvailable) {
      button.addEventListener("click", async () => {
        await requestWaiting(timeId, timeLabel);
      });
      wrapper.appendChild(button);
      return;
    }
    button.addEventListener("click", () => {
      document.querySelectorAll("#available-times .chip").forEach((chip) => chip.classList.remove("selected"));
      button.classList.add("selected");
      selectedTimeId = timeId;
      selectedTimeLabel = timeLabel;
      selectedTimeLabelEl.textContent = `시간: ${selectedTimeLabel}`;
      reservationSubmitEl.disabled = false;
      setStatus(`시간 ${selectedTimeLabel} 선택됨`);
    });
    wrapper.appendChild(button);
  });
}

async function refreshAvailableTimesForCurrentSelection() {
  const date = document.getElementById("theme-date").value;
  if (!date || !selectedThemeId) return;
  const items = await api(`/user/times/availability?date=${date}&themeId=${selectedThemeId}`);
  renderAvailableTimes(items);
}

async function requestWaiting(timeId, timeLabel) {
  try {
    if (!selectedThemeId) {
      throw new Error("테마를 먼저 선택하세요.");
    }
    if (!currentLoginName) {
      throw new Error("로그인 후 대기 신청할 수 있습니다.");
    }

    const date = document.getElementById("theme-date").value;
    const confirmed = await confirmAction({
      title: "대기 신청 확인",
      message: `예약자 ${currentLoginName}\n날짜 ${date}\n테마 ${selectedThemeName}\n시간 ${timeLabel}\n\n이미 예약된 시간에 대기를 신청하시겠습니까?`,
      okLabel: "대기 신청",
    });
    if (!confirmed) {
      setStatus("대기 신청이 취소되었습니다.");
      return;
    }

    const waiting = await api("/user/waitings", {
      method: "POST",
      body: JSON.stringify({
        date,
        timeId,
        themeId: selectedThemeId,
      }),
    });

    myReservationSectionEl.classList.remove("hidden");
    await loadMyReservations();
    setStatus(`대기 신청 완료: ${timeLabel} / ${waiting.waitingOrder}번째`);
    await showResultModal({
      title: "대기 신청 성공",
      message: `${currentLoginName}님의 대기가 신청되었습니다.\n대기 순번: ${waiting.waitingOrder}`,
    });
  } catch (error) {
    await showErrorModal("대기 신청 실패", error);
  }
}

function renderPopular(themes) {
  const list = document.getElementById("popular-list");
  list.innerHTML = "";
  if (!themes.length) {
    const li = document.createElement("li");
    li.className = "empty-message";
    li.textContent = "조회된 인기 테마가 없습니다.";
    list.appendChild(li);
    return;
  }
  themes.forEach((theme) => {
    const li = document.createElement("li");
    li.textContent = `${theme.name} - ${theme.description}`;
    list.appendChild(li);
  });
}

function renderMyReservationCards(reservations) {
  myReservationCardsEl.innerHTML = "";
  if (!reservations.length) {
    const empty = document.createElement("p");
    empty.className = "empty-message";
    empty.textContent = "조회된 내 예약이 없습니다.";
    myReservationCardsEl.appendChild(empty);
    return;
  }

  reservations.forEach((reservation) => {
    const card = document.createElement("article");
    const isWaiting = reservation.status === "WAITING";
    const menuId = `${reservation.status}-${reservation.id}`;
    const statusLabel = isWaiting ? "대기" : "예약";
    const menuItems = isWaiting
      ? `<button type="button" class="menu-item delete" data-waiting-delete-id="${reservation.id}">대기 취소</button>`
      : `
            <button
              type="button"
              class="menu-item edit"
              data-edit-id="${reservation.id}"
              data-edit-date="${reservation.date}"
              data-edit-time-id="${reservation.time.id}"
              data-edit-time-label="${reservation.time.time}"
              data-edit-theme-id="${reservation.theme.id}"
              data-edit-theme-name="${reservation.theme.name}"
            >수정</button>
            <button type="button" class="menu-item delete" data-delete-id="${reservation.id}">예약 취소</button>
          `;

    card.className = `reservation-card${isWaiting ? " waiting-card" : ""}`;
    card.innerHTML = `
      <div class="reservation-card-head">
        <div>
          <div class="reservation-title-row">
            <strong>${reservation.memberName}</strong>
            <span class="status-badge ${isWaiting ? "waiting" : "reserved"}">${statusLabel}</span>
          </div>
          <p>#${reservation.id}${isWaiting ? ` · 대기 ${reservation.waitingOrder}번째` : ""}</p>
        </div>
        <div class="menu-wrapper">
          <button type="button" class="menu-button" data-menu-id="${menuId}" aria-label="${statusLabel} 메뉴">☰</button>
          <div class="menu-panel hidden" id="menu-${menuId}">
            ${menuItems}
          </div>
        </div>
      </div>
      <div class="reservation-card-body">
        <p><span>날짜</span><strong>${reservation.date}</strong></p>
        <p><span>테마</span><strong>${reservation.theme.name}</strong></p>
        <p><span>시간</span><strong>${reservation.time.time}</strong></p>
        ${isWaiting ? `<p><span>대기 순번</span><strong>${reservation.waitingOrder}번째</strong></p>` : ""}
      </div>
    `;
    myReservationCardsEl.appendChild(card);
  });
}

async function loadMyReservations() {
  if (!isAuthenticated) return;
  const reservations = await api("/user/reservations/me");
  myReservationOwnerEl.textContent = `${currentLoginName ?? "현재 사용자"}님의 예약 목록`;
  renderMyReservationCards(reservations);
}

function closeMenuPanels() {
  document.querySelectorAll(".menu-panel").forEach((panel) => panel.classList.add("hidden"));
}

async function requestEditWaiting(timeId, timeLabel) {
  try {
    if (!editingReservation) {
      throw new Error("수정 중인 예약 정보를 찾을 수 없습니다.");
    }
    if (!currentLoginName) {
      throw new Error("로그인 후 대기 신청할 수 있습니다.");
    }

    const date = editReservationDateEl.value;
    const confirmed = await confirmAction({
      title: "대기 신청 확인",
      message: `예약자 ${currentLoginName}\n날짜 ${date}\n테마 ${editingReservation.themeName}\n시간 ${timeLabel}\n\n기존 예약은 유지하고 이 시간에 대기를 신청하시겠습니까?`,
      okLabel: "대기 신청",
    });
    if (!confirmed) {
      setStatus("대기 신청이 취소되었습니다.");
      return;
    }

    const waiting = await api("/user/waitings", {
      method: "POST",
      body: JSON.stringify({
        date,
        timeId,
        themeId: editingReservation.themeId,
      }),
    });

    editReservationModalEl.classList.add("hidden");
    myReservationSectionEl.classList.remove("hidden");
    await loadMyReservations();
    await refreshAvailableTimesForCurrentSelection();
    setStatus(`대기 신청 완료: ${timeLabel} / ${waiting.waitingOrder}번째`);
    await showResultModal({
      title: "대기 신청 성공",
      message: `${currentLoginName}님의 대기가 신청되었습니다.\n대기 순번: ${waiting.waitingOrder}`,
    });
  } catch (error) {
    await showErrorModal("대기 신청 실패", error);
  }
}

function renderEditAvailableTimes(items, currentTime, selectedDate) {
  editAvailableTimesEl.innerHTML = "";
  const isOriginalDate = selectedDate === editingReservation.date;
  selectedEditTimeId = isOriginalDate ? currentTime.id : null;

  if (!items.length) {
    const empty = document.createElement("span");
    empty.className = "empty-message";
    empty.textContent = "선택 가능한 시간이 없습니다.";
    editAvailableTimesEl.appendChild(empty);
    return;
  }

  items.forEach((item) => {
    const button = document.createElement("button");
    const timeId = item.timeInformation.id;
    const timeLabel = item.timeInformation.time;
    const isCurrent = isOriginalDate && timeId === currentTime.id;
    const available = item.isAvailable || isCurrent;
    button.type = "button";
    button.className = `chip${available ? "" : " waitable"}${isCurrent ? " selected" : ""}`;
    button.textContent = `${timeLabel} (${isCurrent ? "현재 예약" : item.isAvailable ? "예약 가능" : "대기 가능"})`;
    if (!available) {
      button.addEventListener("click", async () => {
        await requestEditWaiting(timeId, timeLabel);
      });
      editAvailableTimesEl.appendChild(button);
      return;
    }
    button.addEventListener("click", () => {
      editAvailableTimesEl.querySelectorAll(".chip").forEach((chip) => chip.classList.remove("selected"));
      button.classList.add("selected");
      selectedEditTimeId = timeId;
    });
    editAvailableTimesEl.appendChild(button);
  });
}

async function loadEditTimesByDate(date) {
  if (!editingReservation) return;
  const items = await api(`/user/times/availability?date=${date}&themeId=${editingReservation.themeId}`);
  renderEditAvailableTimes(items, editingReservation.time, date);
}

document.getElementById("theme-by-date-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const date = document.getElementById("theme-date").value;
    const themes = await api(`/themes?date=${date}`);
    renderThemeList(themes);
    selectedThemeId = null;
    selectedThemeName = null;
    selectedThemeLabelEl.textContent = "테마: 미선택";
    renderAvailableTimes([]);
    setStatus("날짜별 테마 조회 완료");
  } catch (error) {
    await showErrorModal("날짜별 테마 조회 실패", error);
  }
});

async function selectThemeFromListItem(target) {
  const item = target.closest(".theme-item");
  if (!item) return;
  try {
    const date = document.getElementById("theme-date").value;
    const themeId = item.dataset.id;
    const themeName = item.dataset.name;
    document.querySelectorAll(".theme-item").forEach((li) => li.classList.remove("selected"));
    item.classList.add("selected");
    selectedThemeId = Number(themeId);
    selectedThemeName = themeName;
    selectedThemeLabelEl.textContent = `테마: ${selectedThemeName}`;
    const items = await api(`/user/times/availability?date=${date}&themeId=${themeId}`);
    renderAvailableTimes(items);
    setStatus(`테마 ID ${themeId} 예약 가능 시간 조회 완료`);
  } catch (error) {
    await showErrorModal("예약 가능 시간 조회 실패", error);
  }
}

document.getElementById("theme-list").addEventListener("click", async (e) => {
  await selectThemeFromListItem(e.target);
});

document.getElementById("theme-list").addEventListener("keydown", async (e) => {
  if (e.key !== "Enter" && e.key !== " ") return;
  e.preventDefault();
  await selectThemeFromListItem(e.target);
});

document.getElementById("reservation-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    if (!selectedThemeId || !selectedTimeId) {
      setStatus("테마와 시간을 먼저 선택하세요.", true);
      await showResultModal({
        title: "예약 실패",
        message: "테마와 시간을 먼저 선택해주세요.",
        isError: true,
      });
      return;
    }
    const date = document.getElementById("theme-date").value;
    if (!currentLoginName) {
      throw new Error("로그인 후 예약할 수 있습니다.");
    }
    const confirmed = await confirmAction({
      title: "예약 확인",
      message: `예약자 ${currentLoginName}\n날짜 ${date}\n테마 ${selectedThemeName}\n시간 ${selectedTimeLabel}\n\n예약하시겠습니까?`,
      okLabel: "예약",
    });
    if (!confirmed) {
      setStatus("예약이 취소되었습니다.");
      return;
    }

    await api("/user/reservations", {
      method: "POST",
      body: JSON.stringify({
        date,
        timeId: selectedTimeId,
        themeId: selectedThemeId,
        storeId: 1,
      }),
    });
    setStatus(`예약 완료: ${currentLoginName} / ${date} / ${selectedTimeLabel}`);
    await showResultModal({
      title: "예약 성공",
      message: `${currentLoginName}님의 예약이 완료되었습니다.`,
    });
    await loadMyReservations();
    const items = await api(`/user/times/availability?date=${date}&themeId=${selectedThemeId}`);
    renderAvailableTimes(items);
  } catch (error) {
    setStatus(`예약 실패: ${error.message}`, true);
    await showResultModal({
      title: "예약 실패",
      message: error.message,
      isError: true,
    });
  }
});

document.getElementById("popular-refresh").addEventListener("click", async () => {
  try {
    const themes = await api("/themes/popular");
    renderPopular(themes);
    setStatus("인기 테마 조회 완료");
  } catch (error) {
    await showErrorModal("인기 테마 조회 실패", error);
  }
});

myReservationCardsEl.addEventListener("click", async (e) => {
  const menuButton = e.target.closest(".menu-button");
  if (menuButton) {
    const menuId = menuButton.dataset.menuId;
    const panel = document.getElementById(`menu-${menuId}`);
    const isOpen = !panel.classList.contains("hidden");
    closeMenuPanels();
    if (!isOpen) panel.classList.remove("hidden");
    return;
  }

  const editButton = e.target.closest(".menu-item.edit");
  if (editButton) {
    closeMenuPanels();
    editingReservation = {
      id: editButton.dataset.editId,
      date: editButton.dataset.editDate,
      time: {
        id: Number(editButton.dataset.editTimeId),
        label: editButton.dataset.editTimeLabel,
      },
      themeId: Number(editButton.dataset.editThemeId),
      themeName: editButton.dataset.editThemeName,
    };
    editReservationIdEl.value = editButton.dataset.editId;
    editReservationDateEl.value = editButton.dataset.editDate;
    editReservationThemeEl.textContent = `테마: ${editingReservation.themeName}`;
    await loadEditTimesByDate(editReservationDateEl.value);
    editReservationModalEl.classList.remove("hidden");
    return;
  }

  const waitingDeleteButton = e.target.closest(".menu-item.delete[data-waiting-delete-id]");
  if (waitingDeleteButton) {
    closeMenuPanels();
    try {
      const id = waitingDeleteButton.dataset.waitingDeleteId;
      const confirmed = await confirmAction({
        title: "대기 취소 확인",
        message: `대기 ID ${id}를 취소하시겠습니까?`,
        okLabel: "대기 취소",
        okDanger: true,
      });
      if (!confirmed) {
        return;
      }
      await api(`/user/waitings/${id}`, { method: "DELETE" });
      await loadMyReservations();
      await refreshAvailableTimesForCurrentSelection();
      setStatus(`대기 #${id} 취소 완료`);
      await showResultModal({
        title: "대기 취소 성공",
        message: `대기 ID ${id}가 취소되었습니다.`,
      });
    } catch (error) {
      await showErrorModal("대기 취소 실패", error);
    }
    return;
  }

  const deleteButton = e.target.closest(".menu-item.delete[data-delete-id]");
  if (!deleteButton) return;

  closeMenuPanels();
  try {
    const id = deleteButton.dataset.deleteId;
    const confirmed = await confirmAction({
      title: "예약 취소 확인",
      message: `예약 ID ${id}를 취소하시겠습니까?`,
      okLabel: "예약 취소",
      okDanger: true,
    });
    if (!confirmed) {
      return;
    }
    await api(`/user/reservations/${id}`, { method: "DELETE" });
    await loadMyReservations();
    await refreshAvailableTimesForCurrentSelection();
    setStatus(`예약 #${id} 취소 완료`);
    await showResultModal({
      title: "예약 취소 성공",
      message: `예약 ID ${id}가 취소되었습니다.`,
    });
  } catch (error) {
    await showErrorModal("예약 취소 실패", error);
  }
});

editReservationCancelEl.addEventListener("click", () => {
  editReservationModalEl.classList.add("hidden");
});

editReservationDateEl.addEventListener("change", async () => {
  try {
    await loadEditTimesByDate(editReservationDateEl.value);
  } catch (error) {
    await showErrorModal("수정 가능 시간 조회 실패", error);
  }
});

editReservationFormEl.addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const id = editReservationIdEl.value;
    if (!selectedEditTimeId) throw new Error("수정할 시간을 선택해주세요.");
    await api(`/user/reservations/${id}`, {
      method: "PATCH",
      body: JSON.stringify({
        date: editReservationDateEl.value,
        timeId: Number(selectedEditTimeId),
      }),
    });
    editReservationModalEl.classList.add("hidden");
    await loadMyReservations();
    setStatus(`예약 #${id} 수정 완료`);
    await showResultModal({
      title: "예약 수정 성공",
      message: `예약 ID ${id}가 수정되었습니다.`,
    });
  } catch (error) {
    await showErrorModal("예약 수정 실패", error);
  }
});

document.addEventListener("click", (e) => {
  if (!e.target.closest(".menu-wrapper")) {
    closeMenuPanels();
  }
});

function setTodayDefault() {
  document.getElementById("theme-date").value = DEMO_DATE;
}

loginFormEl.addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const name = loginNameEl.value.trim();
    const password = loginPasswordEl.value;
    const loginResponse = await api("/login", {
      method: "POST",
      body: JSON.stringify({ name, password }),
    });
    setAccessToken(loginResponse.accessToken.replace("Bearer ", ""));
    currentLoginName = name;
    setCommonAuthState(true);
    authStatusEl.textContent = `${name} 로그인됨`;
    loginModalEl.classList.add("hidden");
    setStatus("로그인 성공");
  } catch (error) {
    await showErrorModal("로그인 실패", error);
  }
});

myInfoOpenEl.addEventListener("click", async () => {
  try {
    if (!isAuthenticated) {
      throw new Error("로그인 후 내정보를 조회할 수 있습니다.");
    }
    const willOpen = myReservationSectionEl.classList.contains("hidden");
    myReservationSectionEl.classList.toggle("hidden");
    if (!willOpen) {
      setStatus("내 예약 정보 닫힘");
      return;
    }
    await loadMyReservations();
    setStatus("내 예약 조회 완료");
  } catch (error) {
    await showErrorModal("내 예약 조회 실패", error);
  }
});

loginOpenEl.addEventListener("click", async () => {
  if (!isAuthenticated) {
    loginModalEl.classList.remove("hidden");
    loginNameEl.focus();
    return;
  }

  try {
    await api("/logout", { method: "DELETE" });
    clearAccessToken();
    currentLoginName = null;
    authStatusEl.textContent = "로그인이 필요합니다.";
    setCommonAuthState(false);
    setStatus("로그아웃 성공");
  } catch (error) {
    await showErrorModal("로그아웃 실패", error);
  }
});

loginCancelEl.addEventListener("click", () => {
  loginModalEl.classList.add("hidden");
});

setTodayDefault();
setCommonAuthState(Boolean(getAccessToken()));
if (getAccessToken()) {
  authStatusEl.textContent = "로그인 상태입니다.";
}
