import {api} from "./api.js";
import {appEl} from "./dom.js";
import {ADMIN_TABS, canSubmitReservation, selectedTheme, selectedTime, state} from "./state.js";
import {render, syncThemeFilter} from "./render.js";

window.addEventListener("hashchange", handleRoute);
document.addEventListener("click", handleClick);
document.addEventListener("submit", handleSubmit);
document.addEventListener("change", handleChange);
document.addEventListener("input", handleInput);
document.addEventListener("error", handleImageError, true);

boot();

async function boot() {
    applyRoute();
    const nameFromUrl = new URLSearchParams(location.search).get("name");
    if (nameFromUrl) {
        state.guestName = nameFromUrl;
    }
    state.loading.boot = true;
    render();

    try {
        await loadAllData({silent: true});
        ensureSelectedTheme();
        if (state.selectedThemeId) {
            await loadAvailableTimes({silent: true});
        }
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        state.loading.boot = false;
        render({animate: true});
    }
}

function handleRoute() {
    applyRoute();
    render({animate: true});
}

function applyRoute() {
    const normalized = location.hash.replace(/^#\/?/, "");
    const [route = "reserve", tab = "themes"] = normalized.split("/");

    if (route === "admin") {
        state.route = "admin";
        state.adminTab = ADMIN_TABS.has(tab) ? tab : "themes";
        return;
    }

    if (route === "reservations") {
        state.route = "reservations";
        return;
    }

    state.route = "reserve";
}

async function handleClick(event) {
    const routeButton = event.target.closest("[data-route]");
    if (routeButton) {
        const route = routeButton.dataset.route;
        if (route !== "reservations" && state.editingReservationId) {
            cancelReservationEditing();
        }
        if (route === "admin") {
            location.hash = `#/admin/${state.adminTab}`;
            return;
        }
        location.hash = route === "reservations" ? "#/reservations" : "#/reserve";
        return;
    }

    const adminTabButton = event.target.closest("[data-admin-tab]");
    if (adminTabButton) {
        location.hash = `#/admin/${adminTabButton.dataset.adminTab}`;
        return;
    }

    const themeButton = event.target.closest("[data-theme-card], .popular-item");
    if (themeButton && !themeButton.disabled) {
        const themeId = Number(themeButton.dataset.themeId);
        const isSameTheme = Number(state.selectedThemeId) === themeId;

        if (isSameTheme) {
            closeBookingPanel();
            render();
            return;
        }

        state.selectedThemeId = themeId;
        state.selectedTimeId = null;
        await loadAvailableTimes();
        return;
    }

    const actionTarget = event.target.closest("[data-action]");
    if (actionTarget && !actionTarget.disabled) {
        const action = actionTarget.dataset.action;

        if (action === "close-booking") {
            closeBookingPanel();
            render();
            return;
        }

        if (action === "refresh-all") {
            await refreshEverything();
            return;
        }

        if (action === "delete-theme") {
            openConfirm("테마를 삭제할까요?", "연결된 예약도 함께 삭제될 수 있습니다.", () => deleteTheme(Number(actionTarget.dataset.themeId)));
            return;
        }

        if (action === "delete-time") {
            openConfirm("시간을 삭제할까요?", "연결된 예약도 함께 삭제될 수 있습니다.", () => deleteTime(Number(actionTarget.dataset.timeId)));
            return;
        }

        if (action === "delete-reservation") {
            openConfirm("예약을 삭제할까요?", "삭제한 예약은 되돌릴 수 없습니다.", () => deleteReservation(Number(actionTarget.dataset.reservationId)));
            return;
        }

        if (action === "delete-waiting") {
            openConfirm("대기를 취소할까요?", "취소한 대기는 되돌릴 수 없습니다.", () => deleteWaiting(Number(actionTarget.dataset.waitingId)));
            return;
        }

        if (action === "edit-reservation") {
            await startReservationEdit(Number(actionTarget.dataset.reservationId));
            return;
        }

        if (action === "cancel-editing") {
            cancelReservationEditing();
            return;
        }

        if (action === "cancel-confirm") {
            if (event.target !== actionTarget && actionTarget.classList.contains("modal-backdrop")) {
                return;
            }

            closeConfirm();
            return;
        }

        if (action === "confirm-ok") {
            const onConfirm = state.confirm?.onConfirm;
            closeConfirm();
            if (onConfirm) {
                await onConfirm();
            }
            return;
        }

        if (action === "dismiss-toast") {
            state.toast = null;
            render();
        }

        return;
    }

    const timeButton = event.target.closest("[data-time-slot-id]");
    if (timeButton && !timeButton.disabled) {
        state.selectedTimeId = Number(timeButton.dataset.timeSlotId);
        render();
        return;
    }

    if (shouldCloseBookingFromBlankClick(event)) {
        closeBookingPanel();
        render();
    }
}

async function handleSubmit(event) {
    event.preventDefault();

    if (event.target.id === "reservation-lookup-form") {
        await submitReservationLookup(event.target);
        return;
    }

    if (event.target.id === "reservation-form") {
        await submitReservation(event.target);
        return;
    }

    if (event.target.id === "theme-form") {
        await submitTheme(event.target);
        return;
    }

    if (event.target.id === "time-form") {
        await submitTime(event.target);
    }
}

async function handleChange(event) {
    if (event.target.id !== "reservation-date") {
        return;
    }

    state.selectedDate = event.target.value;
    state.selectedTimeId = null;
    render();
    await loadAvailableTimes();
}

function handleInput(event) {
    if (event.target.id === "guest-name") {
        state.guestName = event.target.value;
        syncReservationButton();
        return;
    }

    if (event.target.id === "reservation-name") {
        state.guestName = event.target.value;
        return;
    }

    if (event.target.id === "theme-search") {
        state.themeQuery = event.target.value;
        syncThemeFilter();
    }
}

function handleImageError(event) {
    if (!event.target.matches("img[data-cover]")) {
        return;
    }

    event.target.closest(".image-frame, .row-thumb")?.classList.add("is-missing");
    event.target.remove();
}

async function refreshEverything() {
    state.loading.boot = true;
    render();

    try {
        await loadAllData({silent: true});
        ensureSelectedTheme();
        if (state.selectedThemeId) {
            await loadAvailableTimes({silent: true});
        }
        showToast("데이터를 새로 불러왔습니다.", "success");
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        state.loading.boot = false;
        render();
    }
}

async function loadAllData() {
    const today = new Date();
    const endAt = new Date(today);
    endAt.setDate(today.getDate() - 1);
    const startAt = new Date(today);
    startAt.setDate(today.getDate() - 7);
    const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;

    const [themes, popularThemes, adminTimes] = await Promise.all([
        api("/themes"),
        api(`/themes/popular?startAt=${fmt(startAt)}&endAt=${fmt(endAt)}&limit=10`),
        api("/admin/times")
    ]);

    state.themes = themes;
    state.popularThemes = popularThemes;
    state.adminTimes = adminTimes;
    await loadReservations({silent: true});
}

async function loadAvailableTimes(options = {}) {
    if (!state.selectedThemeId || !state.selectedDate) {
        state.availableTimes = [];
        state.selectedTimeId = null;
        render();
        return;
    }

    state.loading.times = true;
    if (!options.silent) {
        render();
    }

    try {
        state.availableTimes = await api(`/times?themeId=${state.selectedThemeId}&date=${state.selectedDate}`);

        if (!selectedTime()?.available) {
            state.selectedTimeId = null;
        }
    } catch (error) {
        state.availableTimes = [];
        state.selectedTimeId = null;
        showToast(error.message, "error");
    } finally {
        state.loading.times = false;
        render();
    }
}

async function submitReservation(form) {
    const name = state.guestName.trim();
    const isEditing = Boolean(state.editingReservationId);
    let submitted = false;

    state.guestName = name;

    if (!canSubmitReservation()) {
        showToast("테마, 날짜, 시간, 예약자를 모두 입력해 주세요.", "error");
        syncReservationButton();
        return;
    }

    state.submitting = true;
    render();

    try {
        if (state.editingReservationId) {
            await api(`/reservations/${state.editingReservationId}`, {
                method: "PATCH",
                body: {
                    name,
                    date: state.selectedDate,
                    timeId: state.selectedTimeId
                }
            });
        } else {
            await api("/reservations", {
                method: "POST",
                body: {
                    name,
                    date: state.selectedDate,
                    themeId: state.selectedThemeId,
                    timeId: state.selectedTimeId
                }
            });
        }

        const wasWaiting = !selectedTime()?.available;
        state.selectedThemeId = null;
        state.selectedTimeId = null;
        state.availableTimes = [];
        state.editingReservationId = null;
        await loadAllData({silent: true});
        showToast(isEditing ? "예약을 변경했습니다." : wasWaiting ? "대기 신청이 완료되었습니다." : "예약이 완료되었습니다.", "success");
        submitted = true;
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        state.submitting = false;
        render();
        if (submitted) {
            scrollToPageTop();
        }
    }
}

async function submitTheme(form) {
    const formData = new FormData(form);
    const name = String(formData.get("name") || "").trim();
    const description = String(formData.get("description") || "").trim();
    const thumbnailImgUrl = String(formData.get("thumbnailImgUrl") || "").trim();
    const priceRaw = String(formData.get("price") || "").trim();
    const price = priceRaw ? Number(priceRaw) : null;

    if (!name || !description || !thumbnailImgUrl || price === null || isNaN(price)) {
        showToast("테마명, 설명, 이미지 URL, 가격을 모두 입력해 주세요.", "error");
        return;
    }

    state.submitting = true;
    render();

    try {
        const createdTheme = await api("/admin/themes", {
            method: "POST",
            body: {name, description, thumbnailImgUrl, price}
        });

        state.selectedThemeId = createdTheme.id;
        await loadAllData({silent: true});
        await loadAvailableTimes({silent: true});
        showToast("테마를 추가했습니다.", "success");
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        state.submitting = false;
        render();
    }
}

async function submitTime(form) {
    const formData = new FormData(form);
    const startAt = String(formData.get("startAt") || "").trim();

    if (!startAt) {
        showToast("시작 시간을 입력해 주세요.", "error");
        return;
    }

    state.submitting = true;
    render();

    try {
        await api("/admin/times", {
            method: "POST",
            body: {startAt}
        });

        await loadAllData({silent: true});
        if (state.selectedThemeId) {
            await loadAvailableTimes({silent: true});
        }
        showToast("예약 시간을 추가했습니다.", "success");
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        state.submitting = false;
        render();
    }
}

async function deleteTheme(themeId) {
    await mutateWithReload(() => api(`/admin/themes/${themeId}`, {method: "DELETE"}), "테마를 삭제했습니다.");
}

async function deleteTime(timeId) {
    await mutateWithReload(() => api(`/admin/times/${timeId}`, {method: "DELETE"}), "시간을 삭제했습니다.");
}

async function deleteWaiting(waitingId) {
    const name = state.guestName.trim();

    if (!name) {
        showToast("예약자를 입력한 뒤 취소해 주세요.", "error");
        return;
    }

    const params = new URLSearchParams({name});
    await mutateWithReload(() => api(`/waitings/${waitingId}?${params.toString()}`, {method: "DELETE"}), "대기를 취소했습니다.");
}

async function deleteReservation(reservationId) {
    const name = state.guestName.trim();

    if (!name) {
        showToast("예약자를 입력한 뒤 삭제해 주세요.", "error");
        return;
    }

    const params = new URLSearchParams({name});
    await mutateWithReload(() => api(`/reservations/${reservationId}?${params.toString()}`, {method: "DELETE"}), "예약을 삭제했습니다.");

    if (Number(state.editingReservationId) === Number(reservationId)) {
        cancelReservationEditing();
    }
}

async function startReservationEdit(reservationId) {
    const reservation = state.myReservations.find((item) => Number(item.id) === Number(reservationId));

    if (!reservation) {
        showToast("선택한 예약을 찾을 수 없습니다.", "error");
        return;
    }

    state.editingReservationId = reservation.id;
    state.guestName = reservation.name;
    state.selectedThemeId = reservation.theme.id;
    state.selectedDate = reservation.date;
    state.selectedTimeId = reservation.time.id;
    render();
    await loadAvailableTimes();
}

async function mutateWithReload(request, successMessage) {
    state.submitting = true;
    render();

    try {
        await request();
        await loadAllData({silent: true});
        ensureSelectedTheme();
        if (state.selectedThemeId) {
            await loadAvailableTimes({silent: true});
        }
        showToast(successMessage, "success");
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        state.submitting = false;
        render();
    }
}

function ensureSelectedTheme() {
    if (!state.selectedThemeId || selectedTheme()) {
        return;
    }

    state.selectedThemeId = null;
    state.selectedTimeId = null;
    state.availableTimes = [];
}

function closeBookingPanel() {
    state.selectedThemeId = null;
    state.selectedTimeId = null;
    state.editingReservationId = null;
    state.availableTimes = [];
    state.loading.times = false;
}

function cancelReservationEditing() {
    state.editingReservationId = null;
    state.selectedTimeId = null;
    render();
}

function shouldCloseBookingFromBlankClick(event) {
    if (state.route !== "reserve" || !state.selectedThemeId) {
        return false;
    }

    if (!event.target.closest(".page-shell")) {
        return false;
    }

    return !event.target.closest("button, input, label, .booking-panel, .popular-panel");
}

function scrollToPageTop() {
    const scroll = () => {
        window.scrollTo({
            top: 0,
            left: 0,
            behavior: "auto"
        });
        document.documentElement.scrollTop = 0;
        document.body.scrollTop = 0;
    };

    requestAnimationFrame(() => {
        scroll();
        setTimeout(scroll, 120);
    });
}

function syncReservationButton() {
    const button = appEl.querySelector("#reservation-form .submit-button");
    if (!button) {
        return;
    }

    button.disabled = !canSubmitReservation();
}

async function loadReservations(options = {}) {
    const name = state.guestName.trim();

    if (!name) {
        state.myReservations = [];
        state.myWaitings = [];
        if (!options.silent) {
            render();
        }
        return;
    }

    try {
        const params = new URLSearchParams({name});
        [state.myReservations, state.myWaitings] = await Promise.all([
            api(`/reservations?${params.toString()}`),
            api(`/waitings?${params.toString()}`)
        ]);
    } catch (error) {
        state.myReservations = [];
        state.myWaitings = [];
        showToast(error.message, "error");
    } finally {
        if (!options.silent) {
            render();
        }
    }
}

async function submitReservationLookup(form) {
    const formData = new FormData(form);
    state.guestName = String(formData.get("name") || "").trim();
    await loadReservations();
}

function openConfirm(title, body, onConfirm) {
    state.confirm = {title, body, onConfirm};
    render();
}

function closeConfirm() {
    state.confirm = null;
    render();
}

function showToast(message, type = "success") {
    const toast = {message, type, key: Date.now()};
    state.toast = toast;
    render();

    window.setTimeout(() => {
        if (state.toast?.key === toast.key) {
            state.toast = null;
            render();
        }
    }, 3200);
}
