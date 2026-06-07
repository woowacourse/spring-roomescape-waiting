"use strict";

// ─── State ───────────────────────────────
const state = {
    user: null,            // { id, email, name, role, storeId }
    stores: [],           // [{ id, name }]
    themes: [],            // [{ id, name, description, imgUrl }]
    allTimes: [],          // [{ id, startAt }] — full list of times
    userSelectedStoreId: null,
    userSelectedThemeId: null,
    userSelectedDate: null,
    userSelectedTimeId: null,
    userAvailableTimes: [], // [{ time: {id, startAt}, available }]
    editingReservationId: null,
};

// ─── DOM ─────────────────────────────────
const loginView = document.getElementById("loginView");
const userView = document.getElementById("userView");
const managerView = document.getElementById("managerView");

// Login
const loginEmailInput = document.getElementById("loginEmail");
const loginPasswordInput = document.getElementById("loginPassword");
const loginButton = document.getElementById("loginButton");

// User view
const userName = document.getElementById("userName");
const userLogoutButton = document.getElementById("userLogoutButton");
const popularRow = document.getElementById("popularRow");
const popularEmpty = document.getElementById("popularEmpty");
const userStoreSelect = document.getElementById("userStoreSelect");
const userThemeGrid = document.getElementById("userThemeGrid");
const userThemeEmpty = document.getElementById("userThemeEmpty");
const userReservationDate = document.getElementById("userReservationDate");
const userTimeHint = document.getElementById("userTimeHint");
const userTimeGrid = document.getElementById("userTimeGrid");
const userReserveButton = document.getElementById("userReserveButton");
const loadMyReservationsButton = document.getElementById("loadMyReservationsButton");
const myReservationsList = document.getElementById("myReservationsList");
const myReservationsEmpty = document.getElementById("myReservationsEmpty");
const myWaitingsList = document.getElementById("myWaitingsList");
const myWaitingsEmpty = document.getElementById("myWaitingsEmpty");
const reservationsCount = document.getElementById("reservationsCount");
const waitingsCount = document.getElementById("waitingsCount");
const tabReservations = document.getElementById("tabReservations");
const tabWaitings = document.getElementById("tabWaitings");
const reservationsPane = document.getElementById("reservationsPane");
const waitingsPane = document.getElementById("waitingsPane");

// Manager view
const managerName = document.getElementById("managerName");
const managerStoreName = document.getElementById("managerStoreName");
const managerLogoutButton = document.getElementById("managerLogoutButton");
const managerTableBody = document.getElementById("managerTableBody");
const managerEmpty = document.getElementById("managerEmpty");
const refreshManagerButton = document.getElementById("refreshManagerButton");

// Edit modal
const editModalOverlay = document.getElementById("editModalOverlay");
const editModalReservationId = document.getElementById("editModalReservationId");
const editModalDate = document.getElementById("editModalDate");
const editModalTimeSelect = document.getElementById("editModalTimeSelect");
const editModalCancelButton = document.getElementById("editModalCancelButton");
const editModalSubmitButton = document.getElementById("editModalSubmitButton");

// Wait success modal
const waitSuccessModalOverlay = document.getElementById("waitSuccessModalOverlay");
const waitOrderBadge = document.getElementById("waitOrderBadge");
const waitInfoOrder = document.getElementById("waitInfoOrder");
const waitInfoTheme = document.getElementById("waitInfoTheme");
const waitInfoDate = document.getElementById("waitInfoDate");
const waitInfoTime = document.getElementById("waitInfoTime");
const waitSuccessCloseButton = document.getElementById("waitSuccessCloseButton");

// Toast
const toastContainer = document.getElementById("toastContainer");

// ─── Toast ───────────────────────────────
function showToast(message, type) {
    const toast = document.createElement("div");
    toast.className = "toast " + (type === "success" ? "success" : "error");
    const icon = type === "success" ? "✓" : "✕";
    toast.innerHTML =
        '<span class="toast-icon">' + icon + '</span>' +
        '<span class="toast-msg">' + message + '</span>';
    toastContainer.appendChild(toast);

    setTimeout(function () {
        toast.classList.add("fade-out");
        setTimeout(function () { toast.remove(); }, 300);
    }, 3500);
}

// ─── Error messages ──────────────────────
const userFriendlyErrorMessages = Object.freeze({
    AUTH401_001: "이메일 또는 비밀번호가 일치하지 않습니다.",
    AUTH401_002: "로그인이 필요합니다.",
    AUTH401_003: "인증 정보가 유효하지 않습니다. 다시 로그인해 주세요.",
    AUTH401_004: "세션이 만료되었습니다. 다시 로그인해 주세요.",
    AUTH403_001: "이 작업을 수행할 권한이 없습니다.",
    AUTH403_002: "다른 매장의 자원에는 접근할 수 없습니다.",
    RESERVATION404_001: "요청한 예약을 찾을 수 없습니다.",
    RESERVATION409_001: "이미 예약된 시간입니다. 다른 시간을 선택해 주세요.",
    RESERVATION400_001: "지나간 날짜와 시간은 예약할 수 없습니다.",
    RESERVATION400_002: "이미 지난 예약은 취소할 수 없습니다.",
    RESERVATION400_003: "본인 예약만 변경/취소할 수 있습니다.",
    RESERVATION_TIME404_001: "선택한 예약 시간을 찾을 수 없습니다.",
    RESERVATION_TIME409_001: "이미 예약이 존재하는 시간입니다.",
    RESERVATION_WAIT404_001: "요청한 예약 대기를 찾을 수 없습니다.",
    RESERVATION_WAIT409_001: "이미 해당 슬롯에 대기 중입니다.",
    RESERVATION_WAIT422_001: "이미 지난 슬롯에는 대기를 신청할 수 없습니다.",
    THEME404_001: "선택한 테마를 찾을 수 없습니다.",
    THEME409_001: "이미 예약이 존재하는 테마는 삭제할 수 없습니다.",
    COMMON400_001: "입력값이 올바르지 않습니다.",
    COMMON400_002: "요청 경로가 올바르지 않습니다.",
    COMMON400_003: "필요한 조회 조건이 빠졌습니다.",
    COMMON400_004: "입력 형식이 올바르지 않습니다.",
    COMMON400_005: "조회 조건 형식이 올바르지 않습니다.",
    COMMON400_006: "조회 조건 값이 올바르지 않습니다.",
    COMMON500_001: "문제가 발생했습니다. 잠시 후 다시 시도해 주세요.",
});

function friendlyMessage(errorCode, fallback) {
    return userFriendlyErrorMessages[errorCode] || fallback || "요청을 처리하지 못했습니다.";
}

// ─── HTTP helpers ────────────────────────
async function readErrorBody(response, fallbackMessage) {
    try {
        const body = await response.json();
        return {
            errorCode: body.errorCode,
            message: friendlyMessage(body.errorCode, body.message || fallbackMessage),
        };
    } catch {
        return {errorCode: null, message: fallbackMessage || "요청을 처리하지 못했습니다."};
    }
}

async function ensureOk(response, fallbackMessage) {
    if (response.ok) return response;
    const body = await readErrorBody(response, fallbackMessage);
    const err = new Error(body.message);
    err.errorCode = body.errorCode;
    err.status = response.status;

    // 401 자동 동기화 — 로그인 화면으로
    if (response.status === 401 && state.user) {
        state.user = null;
        renderView();
    }
    throw err;
}

async function getJson(url) {
    const response = await fetch(url, {credentials: "include"});
    await ensureOk(response);
    return response.json();
}

async function postJson(url, body) {
    const response = await fetch(url, {
        method: "POST",
        credentials: "include",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(body),
    });
    await ensureOk(response);
    return response.status === 204 ? null : response.json();
}

async function patchJson(url, body) {
    const response = await fetch(url, {
        method: "PATCH",
        credentials: "include",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(body),
    });
    await ensureOk(response);
    return response.status === 204 ? null : response.json();
}

async function deleteResource(url) {
    const response = await fetch(url, {
        method: "DELETE",
        credentials: "include",
    });
    await ensureOk(response);
}

// ─── View routing ────────────────────────
function renderView() {
    loginView.classList.add("hidden");
    userView.classList.add("hidden");
    managerView.classList.add("hidden");

    if (!state.user) {
        loginView.classList.remove("hidden");
        return;
    }
    if (state.user.role === "MANAGER") {
        managerName.textContent = state.user.name + "님";
        const store = state.stores.find(m => m.id === state.user.storeId);
        managerStoreName.textContent = store ? store.name : ("매장 #" + state.user.storeId);
        managerView.classList.remove("hidden");
        loadManagerReservations();
    } else {
        userName.textContent = state.user.name + "님";
        userView.classList.remove("hidden");
        initUserView();
    }
}

// ─── Auth flow ───────────────────────────
async function tryLoadCurrentUser() {
    try {
        const me = await getJson("/api/v1/auth/me");
        state.user = me;
    } catch {
        state.user = null;
    }
}

async function loginAndRoute() {
    const email = loginEmailInput.value.trim();
    const password = loginPasswordInput.value;

    if (!email || !password) {
        alert("이메일과 비밀번호를 입력해 주세요.");
        return;
    }

    try {
        const formBody = new URLSearchParams({email, password}).toString();
        const response = await fetch("/api/v1/auth/login", {
            method: "POST",
            credentials: "include",
            headers: {"Content-Type": "application/x-www-form-urlencoded"},
            body: formBody,
        });
        await ensureOk(response, "로그인에 실패했습니다.");

        const me = await getJson("/api/v1/auth/me");
        state.user = me;
        loginPasswordInput.value = "";
        renderView();
    } catch (e) {
        alert(e.message);
    }
}

async function logout() {
    try {
        await fetch("/api/v1/auth/logout", {method: "POST", credentials: "include"});
    } catch { /* ignore */
    }
    state.user = null;
    state.userSelectedStoreId = null;
    state.userSelectedThemeId = null;
    state.userSelectedDate = null;
    state.userSelectedTimeId = null;
    renderView();
}

// ─── Stores + Themes load (shared) ──────
async function loadStoresAndThemes() {
    const [stores, themes] = await Promise.all([
        getJson("/api/v1/stores"),
        getJson("/api/v1/themes"),
    ]);
    state.stores = stores;
    state.themes = themes;
}

// ─── User view ───────────────────────────
async function initUserView() {
    renderStoreSelect();
    renderThemeGrid();
    renderPopularThemes();
    userReservationDate.value = "";
    userTimeGrid.innerHTML = "";
    userTimeHint.textContent = "매장, 테마, 날짜를 모두 선택하면 가능한 시간이 표시됩니다.";
    updateReserveButton();
}

function renderStoreSelect() {
    userStoreSelect.innerHTML = '<option value="">매장을 선택하세요</option>' +
        state.stores.map(m => `<option value="${m.id}">${m.name}</option>`).join("");
    if (state.userSelectedStoreId) {
        userStoreSelect.value = String(state.userSelectedStoreId);
    }
}

function renderThemeGrid() {
    if (!state.themes.length) {
        userThemeGrid.classList.add("hidden");
        userThemeEmpty.classList.remove("hidden");
        return;
    }
    userThemeGrid.classList.remove("hidden");
    userThemeEmpty.classList.add("hidden");
    userThemeGrid.innerHTML = state.themes.map(t => `
        <button type="button" class="theme-card ${state.userSelectedThemeId === t.id ? "selected" : ""}" data-theme-id="${t.id}">
            <span class="theme-card-name">${t.name}</span>
            <span class="theme-card-desc">${t.description}</span>
        </button>
    `).join("");

    userThemeGrid.querySelectorAll("[data-theme-id]").forEach(el => {
        el.addEventListener("click", () => {
            state.userSelectedThemeId = Number(el.dataset.themeId);
            renderThemeGrid();
            refreshAvailableTimes();
        });
    });
}

async function renderPopularThemes() {
    const today = new Date();
    const to = today.toISOString().split("T")[0];
    const fromDate = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
    const from = fromDate.toISOString().split("T")[0];

    try {
        const popular = await getJson(`/api/v1/themes/popular?from=${from}&to=${to}`);
        if (!popular.length) {
            popularRow.classList.add("hidden");
            popularEmpty.classList.remove("hidden");
            return;
        }
        popularRow.classList.remove("hidden");
        popularEmpty.classList.add("hidden");
        popularRow.innerHTML = popular.map(p => `
            <button type="button" class="popular-card" data-theme-id="${p.id}">
                <span class="popular-rank">${p.rank}위</span>
                <div class="popular-name">${p.name}</div>
                <div class="popular-count">${p.reservationCount}건 예약</div>
            </button>
        `).join("");
        popularRow.querySelectorAll("[data-theme-id]").forEach(el => {
            el.addEventListener("click", () => {
                state.userSelectedThemeId = Number(el.dataset.themeId);
                renderThemeGrid();
                refreshAvailableTimes();
            });
        });
    } catch {
        popularRow.innerHTML = "";
        popularEmpty.classList.remove("hidden");
        popularEmpty.textContent = "인기 테마를 불러오지 못했습니다.";
    }
}

async function refreshAvailableTimes() {
    userTimeGrid.innerHTML = "";
    userTimeHint.textContent = "";

    const date = userReservationDate.value;
    const themeId = state.userSelectedThemeId;
    if (!date || !themeId) {
        userTimeHint.textContent = "매장, 테마, 날짜를 모두 선택하면 가능한 시간이 표시됩니다.";
        updateReserveButton();
        return;
    }

    try {
        const data = await getJson(`/api/v1/reservation-times/availability?date=${date}&themeId=${themeId}`);
        state.userAvailableTimes = data;
        if (!data.length) {
            userTimeHint.textContent = "표시할 시간이 없습니다.";
            updateReserveButton();
            return;
        }
        userTimeGrid.innerHTML = data.map(item => {
            const isSelected = state.userSelectedTimeId === item.id;
            const reservationIdAttr = item.reservationId != null ? `data-reservation-id="${item.reservationId}"` : "";
            const slotClass = ["time-slot", isSelected ? "selected" : "", item.available ? "" : "wait-eligible"].filter(Boolean).join(" ");
            const titleAttr = item.available ? "" : `title="이미 예약된 슬롯 — 대기 신청"`;
            return `<button type="button" class="${slotClass}" data-time-id="${item.id}" ${reservationIdAttr} ${titleAttr}>${item.time.slice(0, 5)}</button>`;
        }).join("");

        userTimeGrid.querySelectorAll("[data-time-id]").forEach(el => {
            el.addEventListener("click", async () => {
                const reservationId = el.dataset.reservationId;
                if (reservationId) {
                    await submitWait(Number(reservationId), el.textContent.trim());
                    return;
                }
                state.userSelectedTimeId = Number(el.dataset.timeId);
                userTimeGrid.querySelectorAll(".time-slot").forEach(s => s.classList.remove("selected"));
                el.classList.add("selected");
                updateReserveButton();
            });
        });
    } catch (e) {
        userTimeHint.textContent = e.message;
    }
    updateReserveButton();
}

function updateReserveButton() {
    const ok = state.userSelectedStoreId && state.userSelectedThemeId
        && userReservationDate.value && state.userSelectedTimeId;
    userReserveButton.disabled = !ok;
}

async function reserve() {
    const body = {
        date: userReservationDate.value,
        timeId: state.userSelectedTimeId,
        themeId: state.userSelectedThemeId,
        storeId: state.userSelectedStoreId,
    };
    try {
        await postJson("/api/v1/reservations", body);
        showToast("예약이 완료되었습니다.", "success");
        state.userSelectedTimeId = null;
        await refreshAvailableTimes();
        await loadMyReservations();
    } catch (e) {
        showToast(e.message, "error");
    }
}

async function loadMyReservations() {
    try {
        const data = await getJson("/api/v1/reservations");
        const reservations = data.reservations || [];
        const waitings = data.waitings || [];

        reservationsCount.textContent = reservations.length;
        waitingsCount.textContent = waitings.length;

        if (!reservations.length) {
            myReservationsList.innerHTML = "";
            myReservationsEmpty.classList.remove("hidden");
            myReservationsEmpty.textContent = "현재 예약된 항목이 없습니다.";
        } else {
            myReservationsEmpty.classList.add("hidden");
            myReservationsList.innerHTML = reservations.map(r => `
                <div class="reservation-row">
                    <div class="info">
                        <div class="info-line">
                            <span class="info-main">${r.date}</span>
                            <span class="info-main">${r.time.startAt.slice(0, 5)}</span>
                        </div>
                        <div class="info-line">
                            <span class="info-sub">${r.theme.name} · ${r.store.name} · 예약 ID ${r.id}</span>
                        </div>
                    </div>
                    <button type="button" class="btn btn-sm btn-danger" data-cancel-id="${r.id}">예약 취소</button>
                </div>
            `).join("");

            myReservationsList.querySelectorAll("[data-cancel-id]").forEach(el => {
                el.addEventListener("click", async () => {
                    if (!confirm("이 예약을 취소하시겠습니까?")) return;
                    try {
                        await deleteResource(`/api/v1/reservations/${el.dataset.cancelId}`);
                        showToast("예약을 취소했습니다.", "success");
                        await loadMyReservations();
                    } catch (e) {
                        showToast(e.message, "error");
                    }
                });
            });
        }

        if (!waitings.length) {
            myWaitingsList.innerHTML = "";
            myWaitingsEmpty.classList.remove("hidden");
            myWaitingsEmpty.textContent = "신청한 대기가 없습니다.";
        } else {
            myWaitingsEmpty.classList.add("hidden");
            myWaitingsList.innerHTML = waitings.map(w => {
                const r = w.reservation;
                return `
                <div class="reservation-row">
                    <div class="info">
                        <div class="info-line">
                            <span class="info-main">${r.date}</span>
                            <span class="info-main">${r.time.startAt.slice(0, 5)}</span>
                            <span class="info-main">대기 ${w.order}순위</span>
                        </div>
                        <div class="info-line">
                            <span class="info-sub">${r.theme.name} · ${r.store.name} · 예약 ID ${r.id}</span>
                        </div>
                        <div class="info-line">
                            <span class="info-sub">신청 ${w.createdAt.replace("T", " ")}</span>
                        </div>
                    </div>
                    <button type="button" class="btn btn-sm btn-danger" data-cancel-wait="${r.id}">대기 취소</button>
                </div>
            `;
            }).join("");

            myWaitingsList.querySelectorAll("[data-cancel-wait]").forEach(el => {
                el.addEventListener("click", async () => {
                    if (!confirm("이 대기를 취소하시겠습니까?")) return;
                    try {
                        await deleteResource(`/api/v1/reservations/${el.dataset.cancelWait}/waits/mine`);
                        showToast("대기를 취소했습니다.", "success");
                        await loadMyReservations();
                    } catch (e) {
                        showToast(e.message, "error");
                    }
                });
            });
        }
    } catch (e) {
        showToast(e.message, "error");
    }
}

async function submitWait(reservationId, timeText) {
    if (!reservationId) {
        showToast("예약 ID를 찾을 수 없습니다.", "error");
        return;
    }
    if (!confirm("이미 예약된 슬롯입니다. 대기를 신청하시겠습니까?")) return;
    try {
        const waitResponse = await postJson(`/api/v1/reservations/${reservationId}/waits`, {});
        const theme = state.themes.find(t => t.id === state.userSelectedThemeId);
        const themeName = theme ? theme.name : ("테마 #" + state.userSelectedThemeId);
        openWaitSuccessModal(waitResponse.order, themeName, userReservationDate.value, timeText);
        await loadMyReservations();
        await refreshAvailableTimes();
    } catch (e) {
        showToast(e.message, "error");
    }
}

// ─── Wait success modal ──────────────────
function openWaitSuccessModal(order, themeName, date, time) {
    waitOrderBadge.textContent = order;
    waitInfoOrder.textContent = order + "번째";
    waitInfoTheme.textContent = themeName;
    waitInfoDate.textContent = date;
    waitInfoTime.textContent = time;
    waitSuccessModalOverlay.classList.remove("hidden");
}

function closeWaitSuccessModal() {
    waitSuccessModalOverlay.classList.add("hidden");
}

// ─── Manager view ────────────────────────
async function loadManagerReservations() {
    try {
        const reservations = await getJson("/api/v1/admin/store/reservations");
        if (!reservations.length) {
            managerTableBody.innerHTML = "";
            managerEmpty.classList.remove("hidden");
            return;
        }
        managerEmpty.classList.add("hidden");
        managerTableBody.innerHTML = reservations.map(r => `
            <tr>
                <td>#${r.id}</td>
                <td>${r.date}</td>
                <td>${r.time.startAt.slice(0, 5)}</td>
                <td>${r.theme.name}</td>
                <td>
                    <div class="cell-main">${r.member.name}</div>
                    <div class="cell-sub">${r.member.email}</div>
                </td>
                <td class="actions">
                    <button type="button" class="btn btn-sm btn-secondary" data-edit-id="${r.id}" data-edit-date="${r.date}" data-edit-time-id="${r.time.id}">변경</button>
                    <button type="button" class="btn btn-sm btn-danger" data-delete-id="${r.id}">삭제</button>
                </td>
            </tr>
        `).join("");

        managerTableBody.querySelectorAll("[data-edit-id]").forEach(el => {
            el.addEventListener("click", () => openEditModal(
                Number(el.dataset.editId),
                el.dataset.editDate,
                Number(el.dataset.editTimeId),
            ));
        });
        managerTableBody.querySelectorAll("[data-delete-id]").forEach(el => {
            el.addEventListener("click", async () => {
                if (!confirm(`예약 #${el.dataset.deleteId}을(를) 삭제하시겠습니까?`)) return;
                try {
                    await deleteResource(`/api/v1/admin/store/reservations/${el.dataset.deleteId}`);
                    showToast("예약을 삭제했습니다.", "success");
                    await loadManagerReservations();
                } catch (e) {
                    showToast(e.message, "error");
                }
            });
        });
    } catch (e) {
        showToast(e.message, "error");
    }
}

function openEditModal(reservationId, date, timeId) {
    state.editingReservationId = reservationId;
    editModalReservationId.textContent = "#" + reservationId;
    editModalDate.value = date;
    editModalTimeSelect.innerHTML = '<option value="">시간을 선택하세요</option>' +
        state.allTimes.map(t => {
            const selected = t.id === timeId ? "selected" : "";
            return `<option value="${t.id}" ${selected}>${t.startAt.slice(0, 5)}</option>`;
        }).join("");
    editModalOverlay.classList.remove("hidden");
}

function closeEditModal() {
    editModalOverlay.classList.add("hidden");
    state.editingReservationId = null;
}

async function submitEdit() {
    const date = editModalDate.value;
    const timeId = editModalTimeSelect.value;
    if (!date || !timeId) {
        alert("날짜와 시간을 모두 선택해 주세요.");
        return;
    }
    try {
        await patchJson(`/api/v1/admin/store/reservations/${state.editingReservationId}`, {
            date,
            timeId: Number(timeId),
        });
        showToast("예약을 변경했습니다.", "success");
        closeEditModal();
        await loadManagerReservations();
    } catch (e) {
        showToast(e.message, "error");
    }
}

// ─── Shared: load times list (for modal) ──
async function loadAllTimes() {
    try {
        state.allTimes = await getJson("/api/v1/reservation-times");
    } catch { /* manager modal will show empty */
    }
}

// ─── Tab switching ────────────────────────
function switchTab(tab) {
    const isReservations = tab === "reservations";
    tabReservations.classList.toggle("active", isReservations);
    tabWaitings.classList.toggle("active", !isReservations);
    reservationsPane.classList.toggle("hidden", !isReservations);
    waitingsPane.classList.toggle("hidden", isReservations);
}

// ─── Init ────────────────────────────────
async function init() {
    await tryLoadCurrentUser();
    await Promise.all([loadStoresAndThemes(), loadAllTimes()]);
    renderView();
}

// ─── Event bindings ──────────────────────
loginButton.addEventListener("click", loginAndRoute);
loginPasswordInput.addEventListener("keydown", e => {
    if (e.key === "Enter") loginAndRoute();
});

userLogoutButton.addEventListener("click", logout);
managerLogoutButton.addEventListener("click", logout);

userStoreSelect.addEventListener("change", () => {
    state.userSelectedStoreId = userStoreSelect.value ? Number(userStoreSelect.value) : null;
    updateReserveButton();
});
userReservationDate.addEventListener("change", () => {
    refreshAvailableTimes();
});
userReserveButton.addEventListener("click", reserve);
loadMyReservationsButton.addEventListener("click", loadMyReservations);
tabReservations.addEventListener("click", () => switchTab("reservations"));
tabWaitings.addEventListener("click", () => switchTab("waitings"));

refreshManagerButton.addEventListener("click", loadManagerReservations);
editModalCancelButton.addEventListener("click", closeEditModal);
editModalSubmitButton.addEventListener("click", submitEdit);
editModalOverlay.addEventListener("click", e => {
    if (e.target === editModalOverlay) closeEditModal();
});

waitSuccessCloseButton.addEventListener("click", closeWaitSuccessModal);
waitSuccessModalOverlay.addEventListener("click", e => {
    if (e.target === waitSuccessModalOverlay) closeWaitSuccessModal();
});

init();
