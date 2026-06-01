const state = {
    themes: [],
    times: [],
    selectedThemeId: null,
    myName: "",
    reservations: [],
    waitings: [],
};

const elements = {};

document.addEventListener("DOMContentLoaded", () => {
    cacheElements();
    bindEvents();
    initialize();
});

function cacheElements() {
    elements.connectionStatus = document.querySelector("#connectionStatus");
    elements.reloadButton = document.querySelector("#reloadButton");
    elements.bookingName = document.querySelector("#bookingName");
    elements.bookingDate = document.querySelector("#bookingDate");
    elements.bookingForm = document.querySelector("#bookingForm");
    elements.themeList = document.querySelector("#themeList");
    elements.selectedThemeName = document.querySelector("#selectedThemeName");
    elements.selectedDateLabel = document.querySelector("#selectedDateLabel");
    elements.timeList = document.querySelector("#timeList");
    elements.mySearchForm = document.querySelector("#mySearchForm");
    elements.myName = document.querySelector("#myName");
    elements.reservationCount = document.querySelector("#reservationCount");
    elements.waitingCount = document.querySelector("#waitingCount");
    elements.myReservationList = document.querySelector("#myReservationList");
    elements.toast = document.querySelector("#toast");
}

function bindEvents() {
    elements.bookingForm.addEventListener("submit", (event) => {
        event.preventDefault();
    });

    elements.reloadButton.addEventListener("click", () => refreshSchedule());
    elements.bookingDate.addEventListener("change", () => refreshSchedule());

    elements.bookingName.addEventListener("input", () => {
        elements.myName.value = elements.bookingName.value;
        clearLoadedMyItemsIfNameChanged();
        renderTimes();
    });

    elements.myName.addEventListener("input", () => {
        elements.bookingName.value = elements.myName.value;
        clearLoadedMyItemsIfNameChanged();
        renderTimes();
    });

    elements.mySearchForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const name = getActiveName();
        if (!name) {
            focusNameInput();
            return;
        }
        await loadMyItems(name);
    });

    elements.themeList.addEventListener("click", (event) => {
        const card = event.target.closest("[data-theme-id]");
        if (!card) {
            return;
        }
        state.selectedThemeId = Number(card.dataset.themeId);
        renderThemes();
        refreshSchedule();
    });

    elements.timeList.addEventListener("click", async (event) => {
        const button = event.target.closest("[data-action]");
        if (!button) {
            return;
        }
        const timeId = Number(button.dataset.timeId);
        const action = button.dataset.action;
        if (action === "reserve") {
            await reserve(timeId);
        }
        if (action === "wait") {
            await applyWaiting(timeId);
        }
    });

    elements.myReservationList.addEventListener("click", async (event) => {
        const button = event.target.closest("[data-cancel-kind]");
        if (!button) {
            return;
        }
        await cancelItem(button.dataset.cancelKind, Number(button.dataset.itemId));
    });
}

async function initialize() {
    const today = toLocalDateString(new Date());
    elements.bookingDate.min = today;
    elements.bookingDate.value = today;
    elements.selectedDateLabel.textContent = formatDate(today);
    renderMyList();
    await loadThemes();
}

async function loadThemes() {
    setStatus("API 연결 중", "");
    elements.themeList.innerHTML = loadingMarkup("테마를 불러오는 중입니다.");
    try {
        const themes = await requestJson("/themes");
        state.themes = themes;
        state.selectedThemeId = themes[0]?.id ?? null;
        setStatus("API 연결됨", "is-online");
        renderThemes();
        await refreshSchedule();
    } catch (error) {
        setStatus("API 연결 실패", "is-error");
        elements.themeList.innerHTML = errorMarkup(error.message);
        elements.timeList.innerHTML = emptyMarkup("시간을 표시할 수 없습니다.");
    }
}

async function refreshSchedule() {
    if (!state.selectedThemeId) {
        renderThemes();
        renderTimes();
        return;
    }

    const date = elements.bookingDate.value;
    elements.selectedDateLabel.textContent = formatDate(date);
    elements.timeList.innerHTML = loadingMarkup("시간을 불러오는 중입니다.");

    try {
        const params = new URLSearchParams({
            date,
            themeId: String(state.selectedThemeId),
        });
        const times = await requestJson(`/times?${params.toString()}`);
        state.times = [...times].sort((a, b) => a.startAt.localeCompare(b.startAt));
        renderThemes();
        renderTimes();
    } catch (error) {
        elements.timeList.innerHTML = errorMarkup(error.message);
    }
}

function renderThemes() {
    if (state.themes.length === 0) {
        elements.selectedThemeName.textContent = "테마 없음";
        elements.themeList.innerHTML = emptyMarkup("등록된 테마가 없습니다.");
        return;
    }

    const selectedTheme = getSelectedTheme();
    elements.selectedThemeName.textContent = selectedTheme?.name ?? "테마 선택";
    elements.themeList.innerHTML = state.themes.map((theme) => {
        const selectedClass = theme.id === state.selectedThemeId ? " is-selected" : "";
        return `
            <button class="theme-card${selectedClass}" type="button" data-theme-id="${theme.id}" aria-pressed="${theme.id === state.selectedThemeId}">
                <img src="${escapeAttribute(theme.thumbnail)}" alt="${escapeAttribute(theme.name)}" loading="lazy" onerror="this.style.visibility='hidden'">
                <div class="theme-body">
                    <strong>${escapeHtml(theme.name)}</strong>
                    <span>${escapeHtml(theme.description)}</span>
                </div>
            </button>
        `;
    }).join("");
}

function renderTimes() {
    if (!state.selectedThemeId) {
        elements.timeList.innerHTML = emptyMarkup("테마를 선택해 주세요.");
        return;
    }

    if (state.times.length === 0) {
        elements.timeList.innerHTML = emptyMarkup("등록된 시간이 없습니다.");
        return;
    }

    const date = elements.bookingDate.value;
    const name = getActiveName();
    elements.timeList.innerHTML = state.times.map((time) => timeSlotMarkup(time, date, name)).join("");
}

function timeSlotMarkup(time, date, name) {
    const past = isPastSlot(date, time.startAt);
    const myReservation = findMyReservation(date, time.id);
    const myWaiting = findMyWaiting(date, time.id);
    const status = getSlotStatus(time, past, myReservation, myWaiting);
    const disabled = past || !name || Boolean(myReservation) || Boolean(myWaiting);
    const action = time.alreadyBooked ? "wait" : "reserve";
    const actionLabel = getActionLabel(time, past, name, myReservation, myWaiting);
    const actionClass = time.alreadyBooked ? "wait-button" : "";

    return `
        <article class="time-slot${past ? " is-past" : ""}">
            <div class="time-main">
                <strong>${escapeHtml(formatTime(time.startAt))}</strong>
                <span class="badge ${status.className}">${escapeHtml(status.label)}</span>
            </div>
            <div class="slot-meta">${escapeHtml(status.meta)}</div>
            <div class="time-actions">
                <button class="${actionClass}" type="button" data-action="${action}" data-time-id="${time.id}" ${disabled ? "disabled" : ""}>
                    ${escapeHtml(actionLabel)}
                </button>
            </div>
        </article>
    `;
}

function getSlotStatus(time, past, myReservation, myWaiting) {
    if (past) {
        return {label: "마감", className: "closed", meta: "지난 시간"};
    }
    if (myReservation) {
        return {label: "예약 확정", className: "available", meta: "내 예약"};
    }
    if (myWaiting) {
        return {label: `대기 ${myWaiting.rank}번`, className: "waiting", meta: "내 대기"};
    }
    if (time.alreadyBooked) {
        return {label: "예약 마감", className: "booked", meta: "대기 가능"};
    }
    return {label: "예약 가능", className: "available", meta: "즉시 예약"};
}

function getActionLabel(time, past, name, myReservation, myWaiting) {
    if (!name) {
        return "이름 필요";
    }
    if (past) {
        return "마감";
    }
    if (myReservation) {
        return "예약 완료";
    }
    if (myWaiting) {
        return `대기 ${myWaiting.rank}번`;
    }
    return time.alreadyBooked ? "대기 신청" : "예약하기";
}

async function reserve(timeId) {
    const payload = buildRequestPayload(timeId);
    if (!payload) {
        return;
    }

    try {
        await requestJson("/reservations", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(payload),
        });
        showToast("예약이 완료되었습니다.");
        await afterMutation(payload.name);
    } catch (error) {
        showToast(error.message);
        await refreshSchedule();
    }
}

async function applyWaiting(timeId) {
    const payload = buildRequestPayload(timeId);
    if (!payload) {
        return;
    }

    await ensureMyItems(payload.name);

    if (findMyReservation(payload.date, timeId)) {
        showToast("이미 예약한 시간입니다.");
        renderTimes();
        return;
    }
    if (findMyWaiting(payload.date, timeId)) {
        showToast("이미 대기 중인 시간입니다.");
        renderTimes();
        return;
    }

    try {
        const waiting = await requestJson("/waiting", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(payload),
        });
        showToast(`대기 ${waiting.rank}번으로 신청되었습니다.`);
        await afterMutation(payload.name);
    } catch (error) {
        showToast(error.message);
        await refreshSchedule();
    }
}

async function cancelItem(kind, id) {
    const name = getActiveName();
    if (!name) {
        focusNameInput();
        return;
    }

    const path = kind === "waiting" ? `/waiting/me/${id}` : `/reservations/me/${id}`;
    const params = new URLSearchParams({name});

    try {
        await requestJson(`${path}?${params.toString()}`, {method: "DELETE"});
        showToast(kind === "waiting" ? "대기가 취소되었습니다." : "예약이 취소되었습니다.");
        await afterMutation(name);
    } catch (error) {
        showToast(error.message);
        await loadMyItems(name);
        await refreshSchedule();
    }
}

async function afterMutation(name) {
    await loadMyItems(name, {silent: true});
    await refreshSchedule();
}

async function loadMyItems(name, options = {}) {
    const normalizedName = name.trim();
    if (!normalizedName) {
        return;
    }

    if (!options.silent) {
        elements.myReservationList.innerHTML = loadingMarkup("내 예약을 불러오는 중입니다.");
    }

    try {
        const params = new URLSearchParams({name: normalizedName});
        const data = await requestJson(`/reservations/me?${params.toString()}`);
        state.myName = normalizedName;
        state.reservations = data.reservations ?? [];
        state.waitings = data.waitings ?? [];
        renderMyList();
        renderTimes();
    } catch (error) {
        state.myName = normalizedName;
        state.reservations = [];
        state.waitings = [];
        renderCounters();
        elements.myReservationList.innerHTML = errorMarkup(error.message);
    }
}

async function ensureMyItems(name) {
    if (state.myName === name.trim()) {
        return;
    }
    await loadMyItems(name, {silent: true});
}

function renderMyList() {
    renderCounters();

    const items = [
        ...state.reservations.map((item) => ({...item, kind: "reservation"})),
        ...state.waitings.map((item) => ({...item, kind: "waiting"})),
    ].sort(compareItems);

    if (items.length === 0) {
        elements.myReservationList.innerHTML = emptyMarkup("조회된 예약이 없습니다.");
        return;
    }

    elements.myReservationList.innerHTML = items.map((item) => {
        const isWaiting = item.kind === "waiting";
        const badge = isWaiting
            ? `<span class="badge waiting">대기 ${item.rank}번</span>`
            : `<span class="badge available">예약 확정</span>`;
        const cancelLabel = isWaiting ? "대기 취소" : "예약 취소";
        return `
            <article class="reservation-item">
                <img src="${escapeAttribute(item.theme.thumbnail)}" alt="${escapeAttribute(item.theme.name)}" loading="lazy" onerror="this.style.visibility='hidden'">
                <div class="item-body">
                    <div class="item-title">
                        <strong>${escapeHtml(item.theme.name)}</strong>
                        ${badge}
                    </div>
                    <div class="item-meta">${escapeHtml(formatDate(item.date))} ${escapeHtml(formatTime(item.time.startAt))}</div>
                    <div class="item-actions">
                        <button class="cancel-button" type="button" data-cancel-kind="${item.kind}" data-item-id="${item.id}">
                            ${cancelLabel}
                        </button>
                    </div>
                </div>
            </article>
        `;
    }).join("");
}

function renderCounters() {
    elements.reservationCount.textContent = String(state.reservations.length);
    elements.waitingCount.textContent = String(state.waitings.length);
}

function compareItems(a, b) {
    const dateCompare = a.date.localeCompare(b.date);
    if (dateCompare !== 0) {
        return dateCompare;
    }
    const timeCompare = a.time.startAt.localeCompare(b.time.startAt);
    if (timeCompare !== 0) {
        return timeCompare;
    }
    return a.kind.localeCompare(b.kind);
}

function buildRequestPayload(timeId) {
    const name = getActiveName();
    const date = elements.bookingDate.value;
    const themeId = state.selectedThemeId;

    if (!name) {
        focusNameInput();
        return null;
    }
    if (!date || !themeId || !timeId) {
        showToast("예약 정보를 확인해 주세요.");
        return null;
    }

    return {name, date, timeId, themeId};
}

function getActiveName() {
    return (elements.bookingName.value || elements.myName.value).trim();
}

function focusNameInput() {
    elements.bookingName.focus();
    showToast("예약자 이름을 입력해 주세요.");
}

function clearLoadedMyItemsIfNameChanged() {
    const currentName = getActiveName();
    if (state.myName && state.myName !== currentName) {
        state.myName = "";
        state.reservations = [];
        state.waitings = [];
        renderMyList();
    }
}

function findMyReservation(date, timeId) {
    if (state.myName !== getActiveName()) {
        return null;
    }
    return state.reservations.find((reservation) => isSameSlot(reservation, date, timeId)) ?? null;
}

function findMyWaiting(date, timeId) {
    if (state.myName !== getActiveName()) {
        return null;
    }
    return state.waitings.find((waiting) => isSameSlot(waiting, date, timeId)) ?? null;
}

function isSameSlot(item, date, timeId) {
    return item.date === date
        && Number(item.time.id) === Number(timeId)
        && Number(item.theme.id) === Number(state.selectedThemeId);
}

function getSelectedTheme() {
    return state.themes.find((theme) => theme.id === state.selectedThemeId) ?? null;
}

function isPastSlot(date, startAt) {
    if (!date || !startAt) {
        return false;
    }
    const [hours, minutes] = startAt.split(":").map(Number);
    const slot = new Date(`${date}T${String(hours).padStart(2, "0")}:${String(minutes).padStart(2, "0")}:00`);
    return slot.getTime() < Date.now();
}

async function requestJson(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            Accept: "application/json",
            ...(options.headers ?? {}),
        },
    });

    if (response.status === 204) {
        return null;
    }

    const contentType = response.headers.get("content-type") ?? "";
    const body = contentType.includes("application/json")
        ? await response.json()
        : await response.text();

    if (!response.ok) {
        const message = typeof body === "string"
            ? body || "요청을 처리하지 못했습니다."
            : body.message || "요청을 처리하지 못했습니다.";
        throw new Error(message);
    }

    return body;
}

function setStatus(text, className) {
    elements.connectionStatus.className = `status-pill ${className}`.trim();
    elements.connectionStatus.textContent = text;
}

let toastTimer = null;

function showToast(message) {
    elements.toast.textContent = message;
    elements.toast.classList.add("is-visible");
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => {
        elements.toast.classList.remove("is-visible");
    }, 2800);
}

function loadingMarkup(message) {
    return `<div class="loading-state">${escapeHtml(message)}</div>`;
}

function emptyMarkup(message) {
    return `<div class="empty-state">${escapeHtml(message)}</div>`;
}

function errorMarkup(message) {
    return `<div class="error-state">${escapeHtml(message)}</div>`;
}

function formatDate(value) {
    if (!value) {
        return "";
    }
    const [year, month, day] = value.split("-");
    return `${year}.${month}.${day}`;
}

function formatTime(value) {
    if (!value) {
        return "";
    }
    return value.slice(0, 5);
}

function toLocalDateString(date) {
    const timezoneOffset = date.getTimezoneOffset() * 60000;
    return new Date(date.getTime() - timezoneOffset).toISOString().slice(0, 10);
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
    return escapeHtml(value);
}
