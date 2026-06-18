const state = {
    themes: [],
    popularThemes: [],
    times: [],
    availability: [],
    selectedTimeId: null,
    myReservations: [],
    myFilter: "active",
    paymentOrders: [],
    paymentFilter: "all",
    activeName: "",
    adminReservations: []
};

const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

document.addEventListener("DOMContentLoaded", () => {
    setDefaultDates();

    const page = document.body.dataset.page;
    if (page === "home") {
        initHome();
    }
    if (page === "my") {
        initMyPage();
    }
    if (page === "payments") {
        initPaymentOrdersPage();
    }
    if (page === "admin") {
        initAdmin();
    }
    if (page === "payment-success") {
        initPaymentSuccess();
    }
    if (page === "payment-fail") {
        initPaymentFail();
    }
});

function setDefaultDates() {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const value = toDateInputValue(tomorrow);

    $$("input[type='date']").forEach((input) => {
        input.min = toDateInputValue(new Date());
        if (!input.value) {
            input.value = value;
        }
    });
}

async function initHome() {
    $("#reservationForm").addEventListener("submit", createReservation);
    $("#themeSelect").addEventListener("change", () => {
        state.selectedTimeId = null;
        loadAvailability();
        markSelectedTheme(Number($("#themeSelect").value));
    });
    $("#reservationDate").addEventListener("change", () => {
        state.selectedTimeId = null;
        loadAvailability();
    });

    await loadCatalog();
    renderThemeSelect($("#themeSelect"));
    renderPopularThemes();
    await loadAvailability();
}

async function initMyPage() {
    const params = new URLSearchParams(window.location.search);
    if (params.get("filter") === "canceled") {
        state.myFilter = "canceled";
    }

    $("#lookupForm").addEventListener("submit", (event) => {
        event.preventDefault();
        state.activeName = $("#lookupName").value.trim();
        localStorage.setItem("roomflow.name", state.activeName);
        loadMyReservations();
    });
    $("#editForm").addEventListener("submit", updateReservation);
    $$(".tab-button").forEach((button) => {
        button.addEventListener("click", () => {
            state.myFilter = button.dataset.filter;
            renderMyReservations(state.myReservations);
        });
    });
    document.addEventListener("click", handleMyActions);

    const savedName = params.get("name") || localStorage.getItem("roomflow.name") || "";
    $("#lookupName").value = savedName;
    state.activeName = savedName;
    await loadCatalog();
    if (savedName) {
        await loadMyReservations();
    } else {
        renderMyReservations([]);
    }
}

async function initPaymentOrdersPage() {
    const params = new URLSearchParams(window.location.search);
    const filter = params.get("filter");
    if (["ready", "confirmed", "failed"].includes(filter)) {
        state.paymentFilter = filter;
    }

    $("#paymentLookupForm").addEventListener("submit", (event) => {
        event.preventDefault();
        state.activeName = $("#paymentLookupName").value.trim();
        localStorage.setItem("roomflow.name", state.activeName);
        loadPaymentOrders();
    });
    $$("[data-payment-filter]").forEach((button) => {
        button.addEventListener("click", () => {
            state.paymentFilter = button.dataset.paymentFilter;
            renderPaymentOrders(state.paymentOrders);
        });
    });

    const savedName = params.get("name") || localStorage.getItem("roomflow.name") || "";
    $("#paymentLookupName").value = savedName;
    state.activeName = savedName;
    if (savedName) {
        await loadPaymentOrders();
    } else {
        renderPaymentOrders([]);
    }
}

async function initAdmin() {
    $("#refreshAdmin").addEventListener("click", loadAdminData);
    $("#adminReservationForm").addEventListener("submit", createAdminReservation);
    $("#themeForm").addEventListener("submit", createTheme);
    $("#timeForm").addEventListener("submit", createTime);
    document.addEventListener("click", handleAdminActions);

    await loadAdminData();
}

async function loadCatalog() {
    const [themesResponse, timesResponse, popularResponse] = await Promise.all([
        api("/themes"),
        api("/times"),
        api("/themes/popularity?days=7&size=10").catch(() => ({ themes: [] }))
    ]);

    state.themes = themesResponse.themes || [];
    state.times = sortTimes(timesResponse.times || []);
    state.popularThemes = popularResponse.themes && popularResponse.themes.length > 0
        ? popularResponse.themes
        : state.themes.slice(0, 8);
}

async function loadAvailability() {
    const date = $("#reservationDate").value;
    const themeId = Number($("#themeSelect").value);
    const grid = $("#timeGrid");

    if (!date || !themeId) {
        grid.innerHTML = emptyState("날짜와 테마를 선택해 주세요.");
        updateSelectedSlot();
        return;
    }

    grid.innerHTML = emptyState("시간을 불러오는 중입니다.");
    try {
        const response = await api(`/times/availability?date=${encodeURIComponent(date)}&themeId=${themeId}`);
        state.availability = sortTimes(response.availableTimes || []);
        renderTimeGrid();
    } catch (error) {
        state.availability = [];
        grid.innerHTML = emptyState(error.message);
        showToast(error.message, "error");
    }
    updateSelectedSlot();
}

function renderThemeSelect(select, selectedId) {
    if (!select) {
        return;
    }

    select.innerHTML = state.themes.map((theme) => {
        const selected = Number(selectedId) === Number(theme.id) ? "selected" : "";
        return `<option value="${theme.id}" ${selected}>${escapeHtml(theme.name)}</option>`;
    }).join("");
}

function renderTimeSelect(select, selectedId) {
    if (!select) {
        return;
    }

    select.innerHTML = state.times.map((time) => {
        const selected = Number(selectedId) === Number(time.id) ? "selected" : "";
        return `<option value="${time.id}" ${selected}>${displayTime(time.startAt)}</option>`;
    }).join("");
}

function renderPopularThemes() {
    const root = $("#popularThemes");
    if (!root) {
        return;
    }

    if (state.popularThemes.length === 0) {
        root.innerHTML = emptyState("등록된 테마가 없습니다.");
        return;
    }

    root.innerHTML = state.popularThemes.map((theme, index) => themeCard(theme, index)).join("");
    $$(".theme-card", root).forEach((card) => {
        card.addEventListener("click", () => {
            $("#themeSelect").value = card.dataset.themeId;
            state.selectedTimeId = null;
            markSelectedTheme(Number(card.dataset.themeId));
            loadAvailability();
            document.querySelector("#reservation-panel").scrollIntoView({ block: "start" });
        });
    });
    markSelectedTheme(Number($("#themeSelect").value));
}

function themeCard(theme, index) {
    const tone = 204 + (index * 29) % 112;
    const initial = String(theme.name || "?").trim().slice(0, 1).toUpperCase();

    return `
        <article class="theme-card" data-theme-id="${theme.id}">
            <div class="theme-art" style="--tone: ${tone}">
                <img src="${escapeAttribute(theme.thumbnail)}" alt="${escapeAttribute(theme.name)}" loading="lazy" onerror="this.closest('.theme-card').classList.add('is-image-missing')">
                <span class="theme-initial">${escapeHtml(initial)}</span>
            </div>
            <div class="theme-body">
                <h3>${escapeHtml(theme.name)}</h3>
                <p>${escapeHtml(theme.description)}</p>
            </div>
        </article>
    `;
}

function markSelectedTheme(themeId) {
    $$(".theme-card").forEach((card) => {
        card.classList.toggle("is-selected", Number(card.dataset.themeId) === Number(themeId));
    });
}

function renderTimeGrid() {
    const grid = $("#timeGrid");
    if (state.availability.length === 0) {
        grid.innerHTML = emptyState("등록된 시간이 없습니다.");
        return;
    }

    grid.innerHTML = state.availability.map((time) => {
        const selected = Number(state.selectedTimeId) === Number(time.id) ? "is-selected" : "";
        const statusClass = time.isAvailable ? "available" : "waiting";
        const statusText = time.isAvailable ? "예약 가능" : "대기 가능";

        return `
            <button class="time-slot ${selected}" type="button" data-time-id="${time.id}">
                <strong>${displayTime(time.startAt)}</strong>
                <span class="status-chip ${statusClass}">${statusText}</span>
            </button>
        `;
    }).join("");

    $$(".time-slot", grid).forEach((button) => {
        button.addEventListener("click", () => {
            state.selectedTimeId = Number(button.dataset.timeId);
            renderTimeGrid();
            updateSelectedSlot();
        });
    });
}

function updateSelectedSlot() {
    const label = $("#selectedSlot");
    const submit = $("#submitReservation");
    if (!label || !submit) {
        return;
    }

    const selected = state.availability.find((time) => Number(time.id) === Number(state.selectedTimeId));
    if (!selected) {
        label.textContent = "시간을 선택해 주세요.";
        submit.disabled = true;
        return;
    }

    const theme = state.themes.find((item) => Number(item.id) === Number($("#themeSelect").value));
    const status = selected.isAvailable ? "예약 확정 예상" : "대기 신청 예상";
    label.textContent = `${$("#reservationDate").value} · ${displayTime(selected.startAt)} · ${theme ? theme.name : ""} · ${status}`;
    submit.disabled = false;
}

function createReservation(event) {
    event.preventDefault();

    const name = $("#guestName").value.trim();
    const date = $("#reservationDate").value;
    const timeId = state.selectedTimeId;
    const themeId = Number($("#themeSelect").value);

    if (!timeId) {
        showToast("시간을 선택해 주세요.", "error");
        return;
    }

    $("#selectedTimeId").value = timeId;
    if (!name || !date || !themeId) {
        showToast("예약 정보를 입력해 주세요.", "error");
        return;
    }

    localStorage.setItem("roomflow.name", name);
    showToast("결제 주문서를 준비합니다.");
    event.target.submit();
}

async function initPaymentSuccess() {
    const params = new URLSearchParams(window.location.search);
    const paymentKey = params.get("paymentKey");
    const orderId = params.get("orderId");
    const amount = Number(params.get("amount"));

    if (!paymentKey || !orderId || !Number.isInteger(amount) || amount <= 0) {
        renderPaymentFailure("결제 승인 정보가 올바르지 않습니다.", "INVALID_PAYMENT_CALLBACK");
        return;
    }

    try {
        const reservation = await api("/payments/confirm", {
            method: "POST",
            body: JSON.stringify({ paymentKey, orderId, amount })
        });
        localStorage.setItem("roomflow.name", reservation.name);
        renderPaymentSuccess(reservation);
    } catch (error) {
        renderPaymentFailure(error.message, "PAYMENT_CONFIRM_FAILED");
    }
}

async function initPaymentFail() {
    const params = new URLSearchParams(window.location.search);
    const code = params.get("code") || "PAYMENT_FAILED";
    const message = params.get("message") || "결제가 완료되지 않았습니다.";
    const orderId = params.get("orderId");

    try {
        await api("/payments/fail", {
            method: "POST",
            body: JSON.stringify({ code, message, orderId })
        });
    } catch (error) {
        showToast(error.message, "error");
    }

    renderPaymentFailure(message, code);
}

function renderPaymentSuccess(reservation) {
    const root = $("#paymentResult");
    if (!root) {
        return;
    }

    root.classList.remove("is-failed");
    root.innerHTML = `
        <p class="eyebrow">PAYMENT</p>
        <h1>예약 완료</h1>
        <p>${escapeHtml(resultMessage(reservation))}</p>
        <div class="payment-summary">
            <span>${escapeHtml(reservation.name)}</span>
            <span>${escapeHtml(reservation.date)}</span>
            <span>${displayTime(reservation.time.startAt)}</span>
            <span>${escapeHtml(reservation.theme.name)}</span>
            ${statusChip(reservation)}
        </div>
        <div class="hero-actions">
            <a class="button primary" href="/my.html?name=${encodeURIComponent(reservation.name)}">내 예약 보기</a>
            <a class="button ghost" href="/payments.html?name=${encodeURIComponent(reservation.name)}">결제 내역</a>
            <a class="button ghost" href="/">다른 예약하기</a>
        </div>
    `;
}

function renderPaymentFailure(message, code) {
    const root = $("#paymentResult");
    if (!root) {
        return;
    }

    root.classList.add("is-failed");
    root.innerHTML = `
        <p class="eyebrow">PAYMENT</p>
        <h1>결제 실패</h1>
        <p>${escapeHtml(message)}</p>
        <div class="payment-summary">
            <span>코드 ${escapeHtml(code)}</span>
        </div>
        <div class="hero-actions">
            <a class="button primary" href="/">다시 예약하기</a>
            <a class="button ghost" href="/payments.html">결제 내역</a>
        </div>
    `;
}

async function loadMyReservations() {
    const name = state.activeName || $("#lookupName").value.trim();
    if (!name) {
        renderMyReservations([]);
        return;
    }

    try {
        const response = await api(`/reservations?name=${encodeURIComponent(name)}`);
        state.myReservations = sortReservations(response.reservations || []);
        renderMyReservations(state.myReservations);
    } catch (error) {
        state.myReservations = [];
        renderMyReservations([]);
        showToast(error.message, "error");
    }
}

function renderMyReservations(reservations) {
    const activeReservations = reservations.filter(isActiveReservation);
    const canceledReservations = reservations.filter(isCanceledReservation);
    const visibleReservations = state.myFilter === "canceled" ? canceledReservations : activeReservations;

    $("#myTotalCount").textContent = reservations.length;
    $("#myReservedCount").textContent = activeReservations.filter((item) => item.status === "RESERVED").length;
    $("#myWaitingCount").textContent = activeReservations.filter((item) => item.status === "WAITING").length;
    $("#myCanceledCount").textContent = canceledReservations.length;

    $$(".tab-button").forEach((button) => {
        button.classList.toggle("is-active", button.dataset.filter === state.myFilter);
    });

    const root = $("#myReservations");
    if (visibleReservations.length === 0) {
        root.innerHTML = emptyState(state.myFilter === "canceled"
            ? "취소된 예약이 없습니다."
            : "진행 중인 예약이 없습니다.");
        return;
    }

    root.innerHTML = visibleReservations.map((reservation) => reservationCard(reservation)).join("");
}

async function loadPaymentOrders() {
    const name = state.activeName || $("#paymentLookupName").value.trim();
    if (!name) {
        renderPaymentOrders([]);
        return;
    }

    try {
        const response = await api(`/payments/orders?name=${encodeURIComponent(name)}`);
        state.paymentOrders = response.orders || [];
        renderPaymentOrders(state.paymentOrders);
    } catch (error) {
        state.paymentOrders = [];
        renderPaymentOrders([]);
        showToast(error.message, "error");
    }
}

function renderPaymentOrders(orders) {
    const visibleOrders = orders.filter((order) => {
        if (state.paymentFilter === "ready") {
            return order.status === "READY";
        }
        if (state.paymentFilter === "confirmed") {
            return order.status === "CONFIRMED";
        }
        if (state.paymentFilter === "failed") {
            return order.status === "FAILED";
        }
        return true;
    });

    $("#paymentTotalCount").textContent = orders.length;
    $("#paymentReadyCount").textContent = orders.filter((order) => order.status === "READY").length;
    $("#paymentConfirmedCount").textContent = orders.filter((order) => order.status === "CONFIRMED").length;
    $("#paymentFailedCount").textContent = orders.filter((order) => order.status === "FAILED").length;

    $$("[data-payment-filter]").forEach((button) => {
        button.classList.toggle("is-active", button.dataset.paymentFilter === state.paymentFilter);
    });

    const root = $("#paymentOrders");
    if (visibleOrders.length === 0) {
        root.innerHTML = emptyState(paymentEmptyMessage());
        return;
    }

    root.innerHTML = visibleOrders.map(paymentOrderCard).join("");
}

function paymentEmptyMessage() {
    const messages = {
        ready: "결제 대기 내역이 없습니다.",
        confirmed: "결제 완료 내역이 없습니다.",
        failed: "결제 실패 내역이 없습니다."
    };

    return messages[state.paymentFilter] || "결제 내역이 없습니다.";
}

function paymentOrderCard(order) {
    const failure = order.status === "FAILED" && (order.failureMessage || order.failureCode)
        ? `<div class="payment-failure">${escapeHtml(order.failureMessage || order.failureCode)}</div>`
        : "";
    const reservation = order.reservationId == null
        ? ""
        : `<span>예약 ID ${order.reservationId}</span>`;

    return `
        <article class="reservation-card payment-order-card">
            <div class="reservation-main">
                <div class="reservation-title">
                    <h3>${escapeHtml(order.theme.name)}</h3>
                    ${paymentStatusChip(order.status)}
                </div>
                <div class="reservation-meta">
                    <span>${escapeHtml(order.name)}</span>
                    <span>${escapeHtml(order.date)}</span>
                    <span>${displayTime(order.time.startAt)}</span>
                    <span>${formatCurrency(order.amount)}</span>
                    ${reservation}
                </div>
                <div class="payment-order-code">${escapeHtml(order.orderId)}</div>
                ${failure}
            </div>
        </article>
    `;
}

function reservationCard(reservation) {
    const actions = isActiveReservation(reservation)
        ? `
            <div class="reservation-actions">
                <button class="button ghost small" type="button" data-action="edit-reservation" data-id="${reservation.id}">변경</button>
                <button class="button danger small" type="button" data-action="cancel-reservation" data-id="${reservation.id}">취소</button>
            </div>
        `
        : "";

    return `
        <article class="reservation-card ${isCanceledReservation(reservation) ? "is-canceled" : ""}">
            <div class="reservation-main">
                <div class="reservation-title">
                    <h3>${escapeHtml(reservation.theme.name)}</h3>
                    ${statusChip(reservation)}
                </div>
                <div class="reservation-meta">
                    <span>${escapeHtml(reservation.name)}</span>
                    <span>${escapeHtml(reservation.date)}</span>
                    <span>${displayTime(reservation.time.startAt)}</span>
                </div>
            </div>
            ${actions}
        </article>
    `;
}

function handleMyActions(event) {
    const target = event.target.closest("[data-action]");
    if (!target) {
        return;
    }

    if (target.dataset.action === "edit-reservation") {
        openEditDialog(Number(target.dataset.id));
    }
    if (target.dataset.action === "cancel-reservation") {
        cancelReservation(Number(target.dataset.id));
    }
    if (target.dataset.action === "close-edit") {
        $("#editDialog").close();
    }
}

function openEditDialog(id) {
    const reservation = state.myReservations.find((item) => Number(item.id) === Number(id));
    if (!reservation) {
        return;
    }

    $("#editReservationId").value = reservation.id;
    $("#editDate").value = reservation.date;
    renderTimeSelect($("#editTime"), reservation.time.id);
    $("#editDialog").showModal();
}

async function updateReservation(event) {
    event.preventDefault();

    const id = Number($("#editReservationId").value);
    const name = state.activeName || $("#lookupName").value.trim();
    const date = $("#editDate").value;
    const timeId = Number($("#editTime").value);

    try {
        const reservation = await api(`/reservations/${id}`, {
            method: "PATCH",
            body: JSON.stringify({ name, date, timeId })
        });
        $("#editDialog").close();
        showToast(resultMessage(reservation));
        await loadMyReservations();
    } catch (error) {
        showToast(error.message, "error");
    }
}

async function cancelReservation(id) {
    const name = state.activeName || $("#lookupName").value.trim();
    if (!name) {
        showToast("이름을 입력해 주세요.", "error");
        return;
    }

    try {
        await api(`/reservations/${id}?name=${encodeURIComponent(name)}`, { method: "DELETE" });
        showToast("예약 요청을 취소했습니다.");
        await loadMyReservations();
    } catch (error) {
        showToast(error.message, "error");
    }
}

async function loadAdminData() {
    try {
        await loadCatalog();
        const response = await api("/admin/reservations");
        state.adminReservations = sortReservations(response.reservations || []);
        renderAdminForms();
        renderAdminReservations();
        renderAdminThemes();
        renderAdminTimes();
        renderAdminMetrics();
    } catch (error) {
        showToast(error.message, "error");
    }
}

function renderAdminForms() {
    renderThemeSelect($("#adminReservationTheme"));
    renderTimeSelect($("#adminReservationTime"));
}

function renderAdminMetrics() {
    $("#adminReservationCount").textContent = state.adminReservations.length;
    $("#adminWaitingCount").textContent = state.adminReservations.filter((item) => item.status === "WAITING").length;
    $("#adminThemeCount").textContent = state.themes.length;
    $("#adminTimeCount").textContent = state.times.length;
}

function renderAdminReservations() {
    const root = $("#adminReservations");
    if (state.adminReservations.length === 0) {
        root.innerHTML = emptyState("활성 예약 요청이 없습니다.");
        return;
    }

    root.innerHTML = `
        <table>
            <thead>
            <tr>
                <th>ID</th>
                <th>상태</th>
                <th>이름</th>
                <th>날짜</th>
                <th>시간</th>
                <th>테마</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            ${state.adminReservations.map((reservation) => `
                <tr>
                    <td>${reservation.id}</td>
                    <td>${statusChip(reservation)}</td>
                    <td>${escapeHtml(reservation.name)}</td>
                    <td>${escapeHtml(reservation.date)}</td>
                    <td>${displayTime(reservation.time.startAt)}</td>
                    <td>${escapeHtml(reservation.theme.name)}</td>
                    <td><button class="button danger small" type="button" data-action="delete-admin-reservation" data-id="${reservation.id}">삭제</button></td>
                </tr>
            `).join("")}
            </tbody>
        </table>
    `;
}

function renderAdminThemes() {
    const root = $("#adminThemes");
    if (state.themes.length === 0) {
        root.innerHTML = emptyState("등록된 테마가 없습니다.");
        return;
    }

    root.innerHTML = state.themes.map((theme) => `
        <div class="compact-item">
            <div>
                <strong>${escapeHtml(theme.name)}</strong>
                <span>${escapeHtml(theme.description)}</span>
            </div>
            <button class="button danger small" type="button" data-action="deactivate-theme" data-id="${theme.id}">비활성화</button>
        </div>
    `).join("");
}

function renderAdminTimes() {
    const root = $("#adminTimes");
    if (state.times.length === 0) {
        root.innerHTML = emptyState("등록된 시간이 없습니다.");
        return;
    }

    root.innerHTML = state.times.map((time) => `
        <div class="compact-item">
            <div>
                <strong>${displayTime(time.startAt)}</strong>
                <span>ID ${time.id}</span>
            </div>
            <button class="button danger small" type="button" data-action="deactivate-time" data-id="${time.id}">비활성화</button>
        </div>
    `).join("");
}

async function createAdminReservation(event) {
    event.preventDefault();

    const payload = {
        name: $("#adminGuestName").value.trim(),
        date: $("#adminReservationDate").value,
        timeId: Number($("#adminReservationTime").value),
        themeId: Number($("#adminReservationTheme").value)
    };

    try {
        const reservation = await api("/reservations", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        $("#adminReservationForm").reset();
        setDefaultDates();
        showToast(resultMessage(reservation));
        await loadAdminData();
    } catch (error) {
        showToast(error.message, "error");
    }
}

async function createTheme(event) {
    event.preventDefault();

    const payload = {
        name: $("#themeName").value.trim(),
        description: $("#themeDescription").value.trim(),
        thumbnail: $("#themeThumbnail").value.trim()
    };

    try {
        await api("/admin/themes", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        $("#themeForm").reset();
        showToast("테마를 추가했습니다.");
        await loadAdminData();
    } catch (error) {
        showToast(error.message, "error");
    }
}

async function createTime(event) {
    event.preventDefault();

    try {
        await api("/admin/times", {
            method: "POST",
            body: JSON.stringify({ startAt: $("#timeStartAt").value })
        });
        $("#timeForm").reset();
        showToast("시간을 추가했습니다.");
        await loadAdminData();
    } catch (error) {
        showToast(error.message, "error");
    }
}

function handleAdminActions(event) {
    const target = event.target.closest("[data-action]");
    if (!target) {
        return;
    }

    const id = Number(target.dataset.id);
    const actions = {
        "delete-admin-reservation": () => deleteAdminReservation(id),
        "deactivate-theme": () => deactivateTheme(id),
        "deactivate-time": () => deactivateTime(id)
    };

    if (actions[target.dataset.action]) {
        actions[target.dataset.action]();
    }
}

async function deleteAdminReservation(id) {
    try {
        await api(`/admin/reservations/${id}`, { method: "DELETE" });
        showToast("예약 요청을 삭제했습니다.");
        await loadAdminData();
    } catch (error) {
        showToast(error.message, "error");
    }
}

async function deactivateTheme(id) {
    try {
        await api(`/admin/themes/${id}`, {
            method: "PATCH",
            body: JSON.stringify({ status: "INACTIVE" })
        });
        showToast("테마를 비활성화했습니다.");
        await loadAdminData();
    } catch (error) {
        showToast(error.message, "error");
    }
}

async function deactivateTime(id) {
    try {
        await api(`/admin/times/${id}`, {
            method: "PATCH",
            body: JSON.stringify({ status: "INACTIVE" })
        });
        showToast("시간을 비활성화했습니다.");
        await loadAdminData();
    } catch (error) {
        showToast(error.message, "error");
    }
}

async function api(path, options = {}) {
    const headers = options.body
        ? { "Content-Type": "application/json", ...options.headers }
        : { ...options.headers };
    const response = await fetch(path, { ...options, headers });

    if (response.status === 204) {
        return null;
    }

    const text = await response.text();
    const data = text ? JSON.parse(text) : null;

    if (!response.ok) {
        throw new Error(problemMessage(data, response));
    }

    return data;
}

function problemMessage(data, response) {
    if (data && Array.isArray(data.errors) && data.errors.length > 0) {
        return data.errors.map((error) => error.reason).join(" ");
    }
    if (data && data.detail) {
        return data.detail;
    }
    return `요청을 처리하지 못했습니다. (${response.status})`;
}

function statusChip(reservation) {
    if (reservation.status === "RESERVED") {
        return `<span class="status-chip reserved">예약 확정</span>`;
    }
    if (reservation.status === "WAITING") {
        const rank = reservation.waitingRank == null ? "" : ` ${reservation.waitingRank}번`;
        return `<span class="status-chip waiting">대기${rank}</span>`;
    }
    if (reservation.status === "CANCELED") {
        return `<span class="status-chip canceled">취소됨</span>`;
    }
    return `<span class="status-chip muted">상태 확인 필요</span>`;
}

function paymentStatusChip(status) {
    if (status === "READY") {
        return `<span class="status-chip waiting">결제 대기</span>`;
    }
    if (status === "CONFIRMED") {
        return `<span class="status-chip reserved">결제 완료</span>`;
    }
    if (status === "FAILED") {
        return `<span class="status-chip canceled">결제 실패</span>`;
    }
    return `<span class="status-chip muted">상태 확인 필요</span>`;
}

function isActiveReservation(reservation) {
    return reservation.status === "RESERVED" || reservation.status === "WAITING";
}

function isCanceledReservation(reservation) {
    return reservation.status === "CANCELED";
}

function resultMessage(reservation) {
    if (reservation.status === "RESERVED") {
        return `${reservation.theme.name} 예약이 확정되었습니다.`;
    }
    return `${reservation.theme.name} 대기 ${reservation.waitingRank}번으로 신청되었습니다.`;
}

function sortTimes(times) {
    return [...times].sort((a, b) => displayTime(a.startAt).localeCompare(displayTime(b.startAt)));
}

function sortReservations(reservations) {
    return [...reservations].sort((a, b) => {
        const dateCompare = a.date.localeCompare(b.date);
        if (dateCompare !== 0) {
            return dateCompare;
        }
        return displayTime(a.time.startAt).localeCompare(displayTime(b.time.startAt));
    });
}

function displayTime(value) {
    if (!value) {
        return "";
    }
    if (Array.isArray(value)) {
        return `${pad(value[0])}:${pad(value[1])}`;
    }
    return String(value).slice(0, 5);
}

function formatCurrency(value) {
    return `${Number(value).toLocaleString("ko-KR")}원`;
}

function toDateInputValue(date) {
    const year = date.getFullYear();
    const month = pad(date.getMonth() + 1);
    const day = pad(date.getDate());
    return `${year}-${month}-${day}`;
}

function pad(value) {
    return String(value).padStart(2, "0");
}

function emptyState(message) {
    return `<div class="empty-state">${escapeHtml(message)}</div>`;
}

function showToast(message, type = "info") {
    const toast = $("#toast");
    if (!toast) {
        return;
    }

    toast.textContent = message;
    toast.classList.toggle("is-error", type === "error");
    toast.classList.add("is-visible");
    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(() => {
        toast.classList.remove("is-visible");
    }, 3200);
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function escapeAttribute(value) {
    return escapeHtml(value).replaceAll("`", "&#096;");
}
