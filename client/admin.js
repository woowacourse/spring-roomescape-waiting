const API_BASE = new URLSearchParams(window.location.search).get("apiBase") || "http://localhost:8080/api";
const statusEl = document.getElementById("status");
const authStatusEl = document.getElementById("auth-status");
const loginFormEl = document.getElementById("login-form");
const loginNameEl = document.getElementById("login-name");
const loginPasswordEl = document.getElementById("login-password");
const adminLoginSectionEl = document.getElementById("admin-login-section");
const adminContentEl = document.getElementById("admin-content");
const confirmModalEl = document.getElementById("confirm-modal");
const confirmTitleEl = document.getElementById("confirm-title");
const confirmMessageEl = document.getElementById("confirm-message");
const confirmOkEl = document.getElementById("confirm-ok");
const confirmCancelEl = document.getElementById("confirm-cancel");
const resultModalEl = document.getElementById("result-modal");
const resultTitleEl = document.getElementById("result-title");
const resultMessageEl = document.getElementById("result-message");
const resultOkEl = document.getElementById("result-ok");
const AUTH_TOKEN_KEY = "roomescapeAccessToken";

function getAccessToken() {
  return localStorage.getItem(AUTH_TOKEN_KEY);
}

function setAccessToken(token) {
  if (!token) return;
  localStorage.setItem(AUTH_TOKEN_KEY, token);
}

function setAdminAuthState(authenticated) {
  adminLoginSectionEl.classList.toggle("hidden", authenticated);
  adminContentEl.classList.toggle("hidden", !authenticated);
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
    li.className = "empty-message";
    li.textContent = "조회된 테마가 없습니다.";
    list.appendChild(li);
    return;
  }
  themes.forEach((theme) => {
    const li = document.createElement("li");
    li.innerHTML = `<strong>#${theme.id} ${theme.name}</strong> - ${theme.description}`;
    list.appendChild(li);
  });
}

function renderReservationList(reservations) {
  const list = document.getElementById("reservation-list");
  list.innerHTML = "";
  if (!reservations.length) {
    const li = document.createElement("li");
    li.className = "empty-message";
    li.textContent = "조회된 예약이 없습니다.";
    list.appendChild(li);
    return;
  }
  reservations.forEach((reservation) => {
    const li = document.createElement("li");
    li.innerHTML = `#${reservation.id} ${reservation.memberName} / ${reservation.date} / ${reservation.time.time} / themeId=${reservation.theme?.id}`;
    list.appendChild(li);
  });
}

function renderTimeList(times) {
  const list = document.getElementById("time-list");
  list.innerHTML = "";
  if (!times.length) {
    const li = document.createElement("li");
    li.className = "empty-message";
    li.textContent = "조회된 시간 슬롯이 없습니다.";
    list.appendChild(li);
    return;
  }
  times.forEach((time) => {
    const li = document.createElement("li");
    li.innerHTML = `#${time.id} ${time.startAt}`;
    list.appendChild(li);
  });
}

function renderSlotList(slots) {
  const list = document.getElementById("slot-list");
  list.innerHTML = "";
  if (!slots.length) {
    const li = document.createElement("li");
    li.className = "empty-message";
    li.textContent = "조회된 슬롯이 없습니다.";
    list.appendChild(li);
    return;
  }
  slots.forEach((slot) => {
    const li = document.createElement("li");
    li.innerHTML = `#${slot.id} / ${slot.date} / timeId=${slot.time_id} / themeId=${slot.theme_id}`;
    list.appendChild(li);
  });
}

async function loadReservations() {
  const reservations = await api("/manager/reservations");
  renderReservationList(reservations);
}

async function loadTimes() {
  const times = await api("/manager/times");
  renderTimeList(times);
}

async function loadSlots() {
  const slots = await api("/manager/slots");
  renderSlotList(slots);
}

document.getElementById("theme-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const name = document.getElementById("theme-name").value;
    const description = document.getElementById("theme-description").value;
    const thumbnailUrl = document.getElementById("theme-thumbnail").value;
    const confirmed = await confirmAction({
      title: "테마 추가 확인",
      message: `${name} 테마를 추가할까요?`,
      okLabel: "추가",
    });
    if (!confirmed) {
      setStatus("테마 추가 취소");
      return;
    }

    await api("/manager/themes", {
      method: "POST",
      body: JSON.stringify({
        name,
        description,
        thumbnailUrl,
      }),
    });
    setStatus("테마 추가 완료");
    e.target.reset();
    await showResultModal({
      title: "테마 추가 성공",
      message: `${name} 테마가 추가되었습니다.`,
    });
  } catch (error) {
    setStatus(error.message, true);
    await showResultModal({
      title: "테마 추가 실패",
      message: error.message,
      isError: true,
    });
  }
});

document.getElementById("theme-refresh").addEventListener("click", async () => {
  try {
    const themes = await api("/manager/themes");
    renderThemeList(themes);
    setStatus("테마 목록 조회 완료");
  } catch (error) {
    await showErrorModal("테마 목록 조회 실패", error);
  }
});

document.getElementById("theme-by-date-form-admin").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const date = document.getElementById("theme-by-date").value;
    const themes = await api(`/themes?date=${date}`);
    renderThemeList(themes);
    setStatus(`날짜(${date}) 기준 테마 조회 완료`);
  } catch (error) {
    await showErrorModal("날짜별 테마 조회 실패", error);
  }
});

document.getElementById("theme-delete-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const id = document.getElementById("theme-delete-id").value;
    const confirmed = await confirmAction({
      title: "테마 삭제 확인",
      message: `테마 ID ${id}를 삭제할까요?`,
      okLabel: "삭제",
      okDanger: true,
    });
    if (!confirmed) {
      setStatus("테마 삭제 취소");
      return;
    }

    await api(`/manager/themes/${id}`, { method: "DELETE" });
    setStatus(`테마 #${id} 삭제 완료`);
    e.target.reset();
    await showResultModal({
      title: "테마 삭제 성공",
      message: `테마 ID ${id}가 삭제되었습니다.`,
    });
  } catch (error) {
    setStatus(error.message, true);
    await showResultModal({
      title: "테마 삭제 실패",
      message: error.message,
      isError: true,
    });
  }
});

document.getElementById("reservation-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const date = document.getElementById("reservation-date").value;
    const timeId = Number(document.getElementById("reservation-time-id").value);
    const themeId = Number(document.getElementById("reservation-theme-id").value);
    const confirmed = await confirmAction({
      title: "예약 추가 확인",
      message: `${date} / timeId ${timeId} / themeId ${themeId} 예약을 추가할까요?`,
      okLabel: "추가",
    });
    if (!confirmed) {
      setStatus("예약 추가 취소");
      return;
    }

    await api("/user/reservations", {
      method: "POST",
      body: JSON.stringify({
        date,
        timeId,
        themeId,
      }),
    });
    await loadReservations();
    setStatus("예약 추가 완료");
    await showResultModal({
      title: "예약 추가 성공",
      message: "예약이 추가되었습니다.",
    });
  } catch (error) {
    setStatus(error.message, true);
    await showResultModal({
      title: "예약 추가 실패",
      message: error.message,
      isError: true,
    });
  }
});

document.getElementById("reservation-delete-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const id = document.getElementById("reservation-delete-id").value;
    const confirmed = await confirmAction({
      title: "예약 삭제 확인",
      message: `예약 ID ${id} 예약을 삭제할까요?`,
      okLabel: "삭제",
      okDanger: true,
    });
    if (!confirmed) {
      setStatus("예약 삭제 취소");
      return;
    }

    await api(`/manager/reservations/${id}`, { method: "DELETE" });
    await loadReservations();
    setStatus(`예약 #${id} 삭제 완료`);
    e.target.reset();
    await showResultModal({
      title: "예약 삭제 성공",
      message: `예약 ID ${id}가 삭제되었습니다.`,
    });
  } catch (error) {
    setStatus(error.message, true);
    await showResultModal({
      title: "예약 삭제 실패",
      message: error.message,
      isError: true,
    });
  }
});

document.getElementById("reservation-update-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const id = Number(document.getElementById("reservation-update-id").value);
    const dateInput = document.getElementById("reservation-update-date").value;
    const timeIdInput = document.getElementById("reservation-update-time-id").value;
    const payload = {};
    if (dateInput) payload.date = dateInput;
    if (timeIdInput) payload.timeId = Number(timeIdInput);

    const confirmed = await confirmAction({
      title: "예약 수정 확인",
      message: `예약 ID ${id}를 수정할까요?`,
      okLabel: "수정",
    });
    if (!confirmed) {
      setStatus("예약 수정 취소");
      return;
    }

    await api(`/manager/reservations/${id}`, {
      method: "PATCH",
      body: JSON.stringify(payload),
    });
    await loadReservations();
    setStatus(`예약 #${id} 수정 완료`);
    e.target.reset();
    await showResultModal({
      title: "예약 수정 성공",
      message: `예약 ID ${id}가 수정되었습니다.`,
    });
  } catch (error) {
    setStatus(error.message, true);
    await showResultModal({
      title: "예약 수정 실패",
      message: error.message,
      isError: true,
    });
  }
});

document.getElementById("reservation-refresh").addEventListener("click", async () => {
  try {
    await loadReservations();
    setStatus("예약 목록 조회 완료");
  } catch (error) {
    await showErrorModal("예약 목록 조회 실패", error);
  }
});

document.getElementById("time-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const startAt = document.getElementById("time-start-at").value;
    const confirmed = await confirmAction({
      title: "시간 슬롯 추가 확인",
      message: `${startAt} 시간 슬롯을 추가할까요?`,
      okLabel: "추가",
    });
    if (!confirmed) {
      setStatus("시간 슬롯 추가 취소");
      return;
    }

    await api("/manager/times", {
      method: "POST",
      body: JSON.stringify({
        startAt,
      }),
    });
    await loadTimes();
    setStatus("시간 슬롯 추가 완료");
    e.target.reset();
    await showResultModal({
      title: "시간 슬롯 추가 성공",
      message: `${startAt} 시간 슬롯이 추가되었습니다.`,
    });
  } catch (error) {
    setStatus(error.message, true);
    await showResultModal({
      title: "시간 슬롯 추가 실패",
      message: error.message,
      isError: true,
    });
  }
});

document.getElementById("time-refresh").addEventListener("click", async () => {
  try {
    await loadTimes();
    setStatus("시간 슬롯 조회 완료");
  } catch (error) {
    await showErrorModal("시간 슬롯 조회 실패", error);
  }
});

document.getElementById("time-delete-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const id = document.getElementById("time-delete-id").value;
    const confirmed = await confirmAction({
      title: "시간 슬롯 삭제 확인",
      message: `시간 슬롯 ID ${id}를 삭제할까요?`,
      okLabel: "삭제",
      okDanger: true,
    });
    if (!confirmed) {
      setStatus("시간 슬롯 삭제 취소");
      return;
    }

    await api(`/manager/times/${id}`, { method: "DELETE" });
    await loadTimes();
    setStatus(`시간 슬롯 #${id} 삭제 완료`);
    e.target.reset();
    await showResultModal({
      title: "시간 슬롯 삭제 성공",
      message: `시간 슬롯 ID ${id}가 삭제되었습니다.`,
    });
  } catch (error) {
    setStatus(error.message, true);
    await showResultModal({
      title: "시간 슬롯 삭제 실패",
      message: error.message,
      isError: true,
    });
  }
});

document.getElementById("slot-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const date = document.getElementById("slot-date").value;
    const timeId = Number(document.getElementById("slot-time-id").value);
    const themeId = Number(document.getElementById("slot-theme-id").value);
    const confirmed = await confirmAction({
      title: "슬롯 추가 확인",
      message: `${date} / timeId ${timeId} / themeId ${themeId} 슬롯을 추가할까요?`,
      okLabel: "추가",
    });
    if (!confirmed) {
      setStatus("슬롯 추가 취소");
      return;
    }

    await api("/manager/slots", {
      method: "POST",
      body: JSON.stringify({
        date,
        timeId,
        themeId,
      }),
    });
    await loadSlots();
    setStatus("슬롯 추가 완료");
    await showResultModal({
      title: "슬롯 추가 성공",
      message: `${date} 슬롯이 추가되었습니다.`,
    });
  } catch (error) {
    setStatus(error.message, true);
    await showResultModal({
      title: "슬롯 추가 실패",
      message: error.message,
      isError: true,
    });
  }
});

document.getElementById("slot-delete-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const id = document.getElementById("slot-delete-id").value;
    const confirmed = await confirmAction({
      title: "슬롯 삭제 확인",
      message: `슬롯 ID ${id}를 삭제할까요?`,
      okLabel: "삭제",
      okDanger: true,
    });
    if (!confirmed) {
      setStatus("슬롯 삭제 취소");
      return;
    }

    await api(`/manager/slots/${id}`, { method: "DELETE" });
    await loadSlots();
    setStatus(`슬롯 #${id} 삭제 완료`);
    e.target.reset();
    await showResultModal({
      title: "슬롯 삭제 성공",
      message: `슬롯 ID ${id}가 삭제되었습니다.`,
    });
  } catch (error) {
    setStatus(error.message, true);
    await showResultModal({
      title: "슬롯 삭제 실패",
      message: error.message,
      isError: true,
    });
  }
});

document.getElementById("slot-refresh").addEventListener("click", async () => {
  try {
    await loadSlots();
    setStatus("슬롯 목록 조회 완료");
  } catch (error) {
    await showErrorModal("슬롯 목록 조회 실패", error);
  }
});

document.getElementById("slot-find-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    const id = document.getElementById("slot-find-id").value;
    const slot = await api(`/manager/slots/${id}`);
    renderSlotList([slot]);
    setStatus(`슬롯 #${id} 단건 조회 완료`);
  } catch (error) {
    await showErrorModal("슬롯 단건 조회 실패", error);
  }
});

function setTodayDefault() {
  const today = new Date().toISOString().slice(0, 10);
  document.getElementById("reservation-date").value = today;
  document.getElementById("slot-date").value = today;
  document.getElementById("theme-by-date").value = today;
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
    setAdminAuthState(true);
    authStatusEl.textContent = `${name} 로그인됨`;
    setStatus("로그인 성공");
  } catch (error) {
    await showErrorModal("로그인 실패", error);
  }
});

setTodayDefault();
setAdminAuthState(false);
authStatusEl.textContent = "관리자 계정으로 로그인하세요.";
