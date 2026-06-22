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
const reservationSummaryEl = document.getElementById("reservation-summary");
const AUTH_TOKEN_KEY = "roomescapeAccessToken";
const AUTH_NAME_KEY = "roomescapeLoginName";
let selectedThemeId = null;
let selectedTimeId = null;
let selectedTimeLabel = null;
let selectedThemeName = null;
let currentLoginName = localStorage.getItem(AUTH_NAME_KEY);
let isAuthenticated = false;
let paymentClientKey = null;

function getAccessToken() {
  return localStorage.getItem(AUTH_TOKEN_KEY);
}

function setAccessToken(token) {
  if (!token) return;
  localStorage.setItem(AUTH_TOKEN_KEY, token);
}

function setLoginName(name) {
  currentLoginName = name || null;
  if (currentLoginName) {
    localStorage.setItem(AUTH_NAME_KEY, currentLoginName);
  }
}

function clearAccessToken() {
  localStorage.removeItem(AUTH_TOKEN_KEY);
  localStorage.removeItem(AUTH_NAME_KEY);
}

function currentUserLabel() {
  return currentLoginName || "현재 사용자";
}

function paymentRedirectUrl(result) {
  const url = new URL(window.location.href);
  url.search = "";
  url.searchParams.set("paymentResult", result);
  return url.toString();
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
    reservationSummaryEl.innerHTML = "";
    reservationSummaryEl.classList.add("hidden");
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
    const reservable = item.status === "RESERVABLE";
    const statusText = reservable ? "즉시 예약 가능" : "대기 신청 가능";
    button.type = "button";
    button.className = `chip${reservable ? "" : " waitable"}`;
    button.setAttribute("aria-label", `${timeLabel} ${statusText}`);
    button.innerHTML = `<strong>${timeLabel}</strong><span>${reservable ? "예약" : "대기"}</span>`;
    if (!reservable) {
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
    const date = document.getElementById("theme-date").value;
    const confirmed = await confirmAction({
      title: "대기 신청 확인",
      message: `예약자 ${currentUserLabel()}\n날짜 ${date}\n테마 ${selectedThemeName}\n시간 ${timeLabel}\n\n이미 예약 또는 대기가 있는 시간입니다.\n대기 신청 후 내 예약 정보에서 현재 순번을 확인할 수 있습니다.`,
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
    await refreshAvailableTimesForCurrentSelection();
    setStatus(`대기 신청 완료: ${timeLabel} / ${waiting.waitingOrder}번째`);
    await showResultModal({
      title: "대기 신청 성공",
      message: `${currentUserLabel()}님의 대기가 신청되었습니다.\n현재 대기 순번: ${waiting.waitingOrder}번째`,
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

function summarizeReservations(reservations) {
  const reservedCount = reservations.filter((reservation) => reservation.status === "CONFIRMED").length;
  const waitingCount = reservations.filter((reservation) => reservation.status === "WAITING").length;
  const pendingCount = reservations.filter((reservation) => reservation.status === "PENDING").length;
  const checkRequiredCount = reservations.filter((reservation) => reservation.status === "PAYMENT_CHECK_REQUIRED").length;

  reservationSummaryEl.classList.remove("hidden");
  reservationSummaryEl.innerHTML = `
    <span><strong>${reservedCount}</strong> 예약</span>
    <span><strong>${waitingCount}</strong> 대기</span>
    <span><strong>${pendingCount}</strong> 결제 대기</span>
    <span><strong>${checkRequiredCount}</strong> 확인 필요</span>
    <span>대기 순번은 취소/승격 후 자동 재계산됩니다.</span>
  `;
}

function formatReservationMeta(reservation) {
  return `${reservation.date} ${reservation.time.time} · ${reservation.theme.name}`;
}

function findReservationById(reservations, id, status = null) {
  return reservations.find((reservation) => {
    if (String(reservation.id) !== String(id)) return false;
    return status ? reservation.status === status : true;
  });
}

function buildReservationChangeMessage(beforeReservations, afterReservations, canceledReservationId) {
  const beforeReserved = findReservationById(beforeReservations, canceledReservationId, "CONFIRMED");
  const reorderedWaitings = afterReservations
    .filter((reservation) => reservation.status === "WAITING")
    .sort((a, b) => (a.waitingOrder ?? 0) - (b.waitingOrder ?? 0));

  const lines = [`예약 ID ${canceledReservationId}가 취소되었습니다.`];
  if (beforeReserved) {
    lines.push(`취소 슬롯: ${formatReservationMeta(beforeReserved)}`);
  }
  lines.push("같은 슬롯에 대기가 있었다면 1번 대기가 자동으로 예약 전환되었습니다.");
  if (reorderedWaitings.length) {
    lines.push(`남은 대기 순번: ${reorderedWaitings.map((waiting) => `#${waiting.id} ${waiting.waitingOrder}번째`).join(", ")}`);
  } else {
    lines.push("내 목록에 남은 대기는 없습니다.");
  }
  return lines.join("\n");
}

function buildWaitingDeleteMessage(afterReservations, waitingId) {
  const waitings = afterReservations
    .filter((reservation) => reservation.status === "WAITING")
    .sort((a, b) => (a.waitingOrder ?? 0) - (b.waitingOrder ?? 0));

  const lines = [`대기 ID ${waitingId}가 취소되었습니다.`];
  if (waitings.length) {
    lines.push(`남은 대기 순번: ${waitings.map((waiting) => `#${waiting.id} ${waiting.waitingOrder}번째`).join(", ")}`);
  } else {
    lines.push("남은 대기가 없습니다.");
  }
  return lines.join("\n");
}

function paymentStatusLabel(status) {
  if (status === "CONFIRMED") return "예약 확정";
  if (status === "PENDING") return "결제 대기";
  if (status === "PAYMENT_CHECK_REQUIRED") return "확인 필요";
  if (status === "PAYMENT_FAILED") return "결제 실패";
  if (status === "WAITING") return "대기";
  return status;
}

function paymentMetaHtml(reservation) {
  if (reservation.status === "WAITING") return "";
  const amount = reservation.amount ? `${reservation.amount.toLocaleString()}원` : "-";
  const paymentKey = reservation.paymentKey || "-";
  const orderId = reservation.orderId || "-";
  return `
        <p><span>결제 상태</span><strong>${paymentStatusLabel(reservation.status)}</strong></p>
        <p><span>주문번호</span><strong>${orderId}</strong></p>
        <p><span>결제 금액</span><strong>${amount}</strong></p>
        <p><span>결제키</span><strong>${paymentKey}</strong></p>
  `;
}

function renderMyReservationCards(reservations) {
  myReservationCardsEl.innerHTML = "";
  if (!reservations.length) {
    reservationSummaryEl.classList.add("hidden");
    const empty = document.createElement("p");
    empty.className = "empty-message";
    empty.textContent = "조회된 내 예약이 없습니다.";
    myReservationCardsEl.appendChild(empty);
    return;
  }

  reservations.forEach((reservation) => {
    const card = document.createElement("article");
    const isWaiting = reservation.status === "WAITING";
    const isPending = reservation.status === "PENDING";
    const isCheckRequired = reservation.status === "PAYMENT_CHECK_REQUIRED";
    const isFailed = reservation.status === "PAYMENT_FAILED";
    const menuId = `${reservation.status}-${reservation.id}`;
    const statusLabel = paymentStatusLabel(reservation.status);
    const menuItems = isWaiting
      ? `<button type="button" class="menu-item delete" data-waiting-delete-id="${reservation.id}">대기 취소</button>`
      : (isCheckRequired || isFailed ? "" : `<button type="button" class="menu-item delete" data-delete-id="${reservation.id}">예약 취소</button>`);
    const rankLabel = isWaiting ? `대기 ${reservation.waitingOrder}번째` : statusLabel;

    card.className = `reservation-card${isWaiting ? " waiting-card" : ""}`;
    card.innerHTML = `
      <div class="reservation-card-head">
        <div>
          <div class="reservation-title-row">
            <strong>${reservation.memberName}</strong>
            <span class="status-badge ${isWaiting ? "waiting" : "reserved"}">${statusLabel}</span>
          </div>
          <p>#${reservation.id} · ${rankLabel}</p>
        </div>
        <div class="menu-wrapper">
          <button type="button" class="menu-button" data-menu-id="${menuId}" aria-label="${statusLabel} 메뉴">☰</button>
          <div class="menu-panel hidden" id="menu-${menuId}">
            ${menuItems || `<span class="menu-item">처리할 수 있는 작업이 없습니다.</span>`}
          </div>
        </div>
      </div>
      <div class="reservation-card-body">
        <p><span>날짜</span><strong>${reservation.date}</strong></p>
        <p><span>테마</span><strong>${reservation.theme.name}</strong></p>
        <p><span>시간</span><strong>${reservation.time.time}</strong></p>
        <p><span>상태</span><strong>${isWaiting ? `${reservation.waitingOrder}번째 대기` : statusLabel}</strong></p>
        ${paymentMetaHtml(reservation)}
      </div>
    `;
    myReservationCardsEl.appendChild(card);
  });
}

async function loadMyReservations() {
  if (!isAuthenticated) return;
  const reservations = await api("/user/reservations/me");
  myReservationOwnerEl.textContent = `${currentUserLabel()}님의 예약 목록`;
  summarizeReservations(reservations);
  renderMyReservationCards(reservations);
  return reservations;
}

async function loadPaymentClientKey() {
  if (paymentClientKey) return paymentClientKey;
  const config = await api("/user/payments/config");
  paymentClientKey = config.clientKey;
  return paymentClientKey;
}

async function requestTossPayment(order) {
  const clientKey = await loadPaymentClientKey();
  if (!window.TossPayments) {
    throw new Error("Toss Payments SDK를 불러오지 못했습니다.");
  }

  const tossPayments = window.TossPayments(clientKey);
  const payment = tossPayments.payment({ customerKey: "ANONYMOUS" });
  await payment.requestPayment({
    method: "CARD",
    amount: {
      value: order.amount,
      currency: "KRW",
    },
    orderId: order.orderId,
    orderName: order.orderName || `${selectedThemeName} ${selectedTimeLabel}`,
    customerName: currentUserLabel(),
    successUrl: paymentRedirectUrl("success"),
    failUrl: paymentRedirectUrl("fail"),
  });
}

async function handlePaymentRedirect() {
  const params = new URLSearchParams(window.location.search);
  const result = params.get("paymentResult");
  if (!result) return;

  try {
    if (result === "success") {
      const response = await api("/user/payments/confirm", {
        method: "POST",
        body: JSON.stringify({
          paymentKey: params.get("paymentKey"),
          orderId: params.get("orderId"),
          amount: Number(params.get("amount")),
        }),
      });
      myReservationSectionEl.classList.remove("hidden");
      await loadMyReservations();
      await showResultModal({
        title: "예약 확정",
        message: `${currentUserLabel()}님의 결제가 승인되어 예약이 확정되었습니다.\n주문번호: ${response.orderId}`,
      });
      setStatus("결제 승인 및 예약 확정 완료");
      return;
    }

    await api("/user/payments/fail", {
      method: "POST",
      body: JSON.stringify({
        code: params.get("code"),
        message: params.get("message"),
        orderId: params.get("orderId"),
      }),
    });
    await showResultModal({
      title: "결제 실패",
      message: params.get("message") || "결제가 취소되었거나 실패했습니다.",
      isError: true,
    });
    setStatus("결제 실패 또는 취소", true);
  } catch (error) {
    await showErrorModal("결제 처리 실패", error);
  } finally {
    const cleanUrl = new URL(window.location.href);
    cleanUrl.search = "";
    window.history.replaceState({}, document.title, cleanUrl.toString());
  }
}

function closeMenuPanels() {
  document.querySelectorAll(".menu-panel").forEach((panel) => panel.classList.add("hidden"));
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
  let reservationOrder = null;
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
    const confirmed = await confirmAction({
      title: "예약 확인",
      message: `예약자 ${currentUserLabel()}\n날짜 ${date}\n테마 ${selectedThemeName}\n시간 ${selectedTimeLabel}\n\n예약하시겠습니까?`,
      okLabel: "예약",
    });
    if (!confirmed) {
      setStatus("예약이 취소되었습니다.");
      return;
    }

    reservationOrder = await api("/user/reservations", {
      method: "POST",
      body: JSON.stringify({
        date,
        timeId: selectedTimeId,
        themeId: selectedThemeId,
        storeId: 1,
      }),
    });
    setStatus("결제창으로 이동합니다.");
    await requestTossPayment(reservationOrder);
  } catch (error) {
    if (reservationOrder?.orderId) {
      try {
        await api("/user/payments/fail", {
          method: "POST",
          body: JSON.stringify({
            code: "PAYMENT_WINDOW_FAILED",
            message: error.message,
            orderId: reservationOrder.orderId,
          }),
        });
      } catch (cleanupError) {
        console.warn("결제 대기 예약 정리 실패", cleanupError);
      }
    }
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
      const beforeReservations = await loadMyReservations();
      await api(`/user/waitings/${id}`, { method: "DELETE" });
      const afterReservations = await loadMyReservations();
      await refreshAvailableTimesForCurrentSelection();
      setStatus(`대기 #${id} 취소 완료`);
      await showResultModal({
        title: "대기 취소 성공",
        message: buildWaitingDeleteMessage(afterReservations ?? beforeReservations ?? [], id),
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
    const beforeReservations = await loadMyReservations();
    await api(`/user/reservations/${id}`, { method: "DELETE" });
    const afterReservations = await loadMyReservations();
    await refreshAvailableTimesForCurrentSelection();
    setStatus(`예약 #${id} 취소 완료`);
    await showResultModal({
      title: "예약 취소 및 대기 자동 전환 완료",
      message: buildReservationChangeMessage(beforeReservations ?? [], afterReservations ?? [], id),
    });
  } catch (error) {
    await showErrorModal("예약 취소 실패", error);
  }
});

document.addEventListener("click", (e) => {
  if (!e.target.closest(".menu-wrapper")) {
    closeMenuPanels();
  }
});

function getDefaultDate() {
  const date = new Date();
  date.setDate(date.getDate() + 1);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function setDefaultDate() {
  document.getElementById("theme-date").value = getDefaultDate();
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
    setLoginName(name);
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

setDefaultDate();
setCommonAuthState(Boolean(getAccessToken()));
if (getAccessToken()) {
  authStatusEl.textContent = `${currentUserLabel()} 로그인 상태입니다.`;
}
handlePaymentRedirect();
