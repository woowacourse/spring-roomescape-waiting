import {api, findPaymentHistories} from "./api.js";
import {appEl} from "./dom.js";
import {ADMIN_TABS, canSubmitReservation, isPastReservation, selectedTheme, selectedTime, state} from "./state.js";
import {render, syncThemeFilter} from "./render.js";
import {
    approveTossRedirectPayment,
    cleanupTossFailRedirectPayment,
    isPaymentPendingReservation,
    parseTossFailRedirectParams,
    parseTossRedirectParams,
    preloadTossPayments,
    rememberPendingPayment,
    requestTossPayment
} from "./payment.js";

window.addEventListener("hashchange", handleRoute);
document.addEventListener("click", handleClick);
document.addEventListener("submit", handleSubmit);
document.addEventListener("change", handleChange);
document.addEventListener("input", handleInput);
document.addEventListener("error", handleImageError, true);

boot();

async function boot() {
    const routeResult = applyRoute();
    state.loading.boot = true;
    render();
    if (routeResult.paymentProcessing) {
        void processPaymentRedirect(routeResult.paymentParams);
    }
    if (routeResult.paymentFailure) {
        void processPaymentFailureCleanup(routeResult.paymentParams);
    }

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
        preloadTossPayments();
    }
}

function handleRoute() {
    const routeResult = applyRoute();
    render({animate: true});
    if (routeResult.paymentProcessing) {
        void processPaymentRedirect(routeResult.paymentParams);
    }
    if (routeResult.paymentFailure) {
        void processPaymentFailureCleanup(routeResult.paymentParams);
    }
}

function applyRoute() {
    const {segments} = parseRouteHash(location.hash);
    const [route = "reserve", tab = "themes"] = segments;

    if (route === "admin") {
        state.route = "admin";
        state.adminTab = ADMIN_TABS.has(tab) ? tab : "themes";
        return {paymentProcessing: false, paymentFailure: false};
    }

    if (route === "payment" && tab === "processing") {
        state.route = "payment-processing";
        return {
            paymentProcessing: true,
            paymentFailure: false,
            paymentParams: parseTossRedirectParams(location.search, location.hash)
        };
    }

    if (route === "payment" && tab === "fail") {
        state.route = "payment-fail";
        const paymentParams = parseTossFailRedirectParams(location.search, location.hash);
        applyPaymentFailRoute(paymentParams);
        return {
            paymentProcessing: false,
            paymentFailure: true,
            paymentParams
        };
    }

    state.route = "reserve";
    return {paymentProcessing: false, paymentFailure: false};
}

function parseRouteHash(hash) {
    const normalized = String(hash || "").replace(/^#\/?/, "");
    const [path = "reserve"] = normalized.split("?");
    const segments = path.split("/").filter(Boolean);

    return {
        segments: segments.length ? segments : ["reserve"]
    };
}

async function processPaymentRedirect(parsedParams) {
    if (!parsedParams.ok) {
        state.payment.processing = {
            status: "failed",
            orderId: "",
            paymentKey: "",
            amount: null,
            message: parsedParams.message
        };
        state.payment.result = {
            status: "failed",
            message: parsedParams.message
        };
        showToast(parsedParams.message, "error");
        render();
        return;
    }

    try {
        const approval = approveTossRedirectPayment(parsedParams.params);
        render();
        const result = await approval;

        const searchReloadResult = await refreshAfterSuccessfulPayment(parsedParams.params.orderId);
        routeToReserveAfterSuccessfulPayment();

        if (!result.skipped) {
            showMutationToast("결제가 완료되었습니다.", searchReloadResult);
        }
    } catch (error) {
        showToast(error.message || "결제 승인을 완료하지 못했습니다.", "error");
    } finally {
        render({animate: true});
    }
}

async function retryPaymentApproval() {
    const params = state.payment.result?.params;

    if (!params) {
        showToast("다시 시도할 결제 승인 정보를 찾을 수 없습니다.", "error");
        return;
    }

    await processPaymentRedirect({ok: true, params});
}

async function refreshAfterSuccessfulPayment(orderId) {
    let dataRefreshFailed = false;
    let searchReloadResult = {failed: false, partialFailure: false};

    try {
        await loadAllData({silent: true});
        ensureSelectedTheme();
        if (state.selectedThemeId) {
            await loadAvailableTimes({silent: true});
        }
        searchReloadResult = await reloadReservationSearchIfNeeded();
    } catch (error) {
        dataRefreshFailed = true;
    }

    reconcileSuccessfulPayment(orderId);

    return {
        failed: dataRefreshFailed || searchReloadResult.failed,
        partialFailure: searchReloadResult.partialFailure
    };
}

function reconcileSuccessfulPayment(orderId) {
    if (isMatchingPendingPayment(state.payment.pendingContext?.reservation, orderId)) {
        state.payment.pendingContext = null;
    }

    state.reservations = state.reservations.map((reservation) => reconcilePaidReservation(reservation, orderId));
    state.searchedReservations = state.searchedReservations.map((reservation) => reconcilePaidReservation(reservation, orderId));
}

function reconcilePaidReservation(reservation, orderId) {
    if (!isMatchingPendingPayment(reservation, orderId)) {
        return reservation;
    }

    return {
        ...reservation,
        status: "CONFIRMED",
        payment: null
    };
}

function isMatchingPendingPayment(reservation, orderId) {
    return isPaymentPendingReservation(reservation) && reservation.payment?.orderId === orderId;
}

function routeToReserveAfterSuccessfulPayment() {
    state.route = "reserve";
    history.replaceState(null, "", `${location.pathname}#/reserve`);
}

function applyPaymentFailRoute(params) {
    state.payment.failure = params;
    state.payment.processing = {
        status: "failed",
        orderId: params.orderId,
        paymentKey: "",
        amount: null,
        message: params.message
    };
    state.payment.result = {
        status: "failed",
        message: params.message
    };
}

async function processPaymentFailureCleanup(params) {
    try {
        const result = await cleanupTossFailRedirectPayment(params);

        if (result.skipped) {
            return;
        }

        await refreshAfterFailedPaymentCleanup(result.orderId);
    } catch (error) {
        showToast(error.message || "결제 실패 기록을 정리하지 못했습니다.", "error");
    } finally {
        render();
    }
}

async function refreshAfterFailedPaymentCleanup(orderId) {
    try {
        await loadAllData({silent: true});
        ensureSelectedTheme();
        if (state.selectedThemeId) {
            await loadAvailableTimes({silent: true});
        }
        await reloadReservationSearchIfNeeded();
    } finally {
        reconcileFailedPaymentCleanup(orderId);
    }
}

function reconcileFailedPaymentCleanup(orderId) {
    if (!orderId) {
        return;
    }

    if (isMatchingPendingPayment(state.payment.pendingContext?.reservation, orderId)) {
        state.payment.pendingContext = null;
    }

    state.reservations = state.reservations.filter((reservation) => !isMatchingPendingPayment(reservation, orderId));
    state.searchedReservations = state.searchedReservations.filter((reservation) => !isMatchingPendingPayment(reservation, orderId));
}

async function handleClick(event) {
    const routeButton = event.target.closest("[data-route]");
    if (routeButton) {
        const route = routeButton.dataset.route;
        location.hash = route === "admin" ? `#/admin/${state.adminTab}` : "#/reserve";
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
            const themeId = Number(actionTarget.dataset.themeId);

            if (hasReservationsForTheme(themeId)) {
                showToast("예약이 연결된 테마는 삭제할 수 없습니다.", "error");
                return;
            }

            openConfirm("테마를 삭제할까요?", "예약이 없는 테마만 삭제됩니다. 삭제한 테마는 되돌릴 수 없습니다.", () => deleteTheme(themeId));
            return;
        }

        if (action === "delete-time") {
            const timeId = Number(actionTarget.dataset.timeId);

            if (hasReservationsForTime(timeId)) {
                showToast("예약이 연결된 시간은 삭제할 수 없습니다.", "error");
                return;
            }

            openConfirm("시간을 삭제할까요?", "예약이 없는 시간만 삭제됩니다. 삭제한 시간은 되돌릴 수 없습니다.", () => deleteTime(timeId));
            return;
        }

        if (action === "delete-reservation") {
            const reservationId = Number(actionTarget.dataset.reservationId);
            const reservation = findReservation(reservationId);

            if (!reservation) {
                showToast("예약 정보를 찾을 수 없습니다.", "error");
                return;
            }

            if (isPastReservation(reservation)) {
                showToast("이미 지난 예약은 삭제할 수 없습니다.", "error");
                return;
            }

            const title = actionTarget.dataset.deleteMode === "cancel" ? "예약을 취소할까요?" : "예약을 삭제할까요?";
            const body = actionTarget.dataset.deleteMode === "cancel"
                ? "취소한 예약은 되돌릴 수 없습니다."
                : "삭제한 예약은 되돌릴 수 없습니다.";

            const successMessage = actionTarget.dataset.deleteMode === "cancel"
                ? "예약을 취소했습니다."
                : "예약을 삭제했습니다.";

            const confirmLabel = actionTarget.dataset.deleteMode === "cancel" ? "예약 취소" : "삭제";

            openConfirm(title, body, () => deleteReservation(reservationId, successMessage), confirmLabel);
            return;
        }

        if (action === "delete-waiting") {
            const waitingId = Number(actionTarget.dataset.waitingId);
            const waiting = findWaiting(waitingId);

            if (!waiting) {
                showToast("대기 정보를 찾을 수 없습니다.", "error");
                return;
            }

            if (isPastReservation(waiting)) {
                showToast("이미 지난 대기는 취소할 수 없습니다.", "error");
                return;
            }

            openConfirm(
                "대기 신청을 취소할까요?",
                "취소한 대기 신청은 되돌릴 수 없습니다.",
                () => deleteWaiting(waitingId),
                "대기 취소"
            );
            return;
        }

        if (action === "edit-reservation") {
            await startReservationEdit(Number(actionTarget.dataset.reservationId));
            return;
        }

        if (action === "start-payment") {
            await preparePayment(actionTarget);
            return;
        }

        if (action === "retry-payment-approval") {
            await retryPaymentApproval();
            return;
        }

        if (action === "cancel-reservation-edit") {
            clearReservationEdit();
            render();
            return;
        }

        if (action === "select-edit-time") {
            state.reservationEdit.timeId = Number(actionTarget.dataset.timeId);
            render();
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

        if (action === "clear-reservation-search") {
            clearReservationSearch();
        }

        return;
    }

    const timeButton = event.target.closest("[data-time-slot-id]");
    if (timeButton && !timeButton.disabled) {
        state.selectedTimeId = Number(timeButton.dataset.timeSlotId);
        render();
        appEl.querySelector("#guest-name")?.focus();
        return;
    }

    if (shouldCloseBookingFromBlankClick(event)) {
        closeBookingPanel();
        render();
    }
}

async function handleSubmit(event) {
    event.preventDefault();

    if (event.target.id === "reservation-form") {
        await submitReservation(event.target);
        return;
    }

    if (event.target.id === "reservation-search-form") {
        await submitReservationSearch(event.target);
        return;
    }

    if (event.target.id === "reservation-edit-form") {
        await submitReservationEdit(event.target);
        return;
    }

    if (event.target.matches("[data-waiting-postpone-form]")) {
        await submitWaitingPostpone(event.target);
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
    if (event.target.id === "reservation-date") {
        state.selectedDate = event.target.value;
        state.selectedTimeId = null;
        render();
        await loadAvailableTimes();
        return;
    }

    if (event.target.id === "reservation-edit-date") {
        state.reservationEdit.date = event.target.value;
        state.reservationEdit.timeId = null;
        await loadReservationEditTimes();
    }
}

function handleInput(event) {
    if (event.target.id === "guest-name") {
        state.guestName = event.target.value;
        syncReservationButton();
        return;
    }

    if (event.target.id === "reservation-search-name") {
        state.reservationSearchName = event.target.value;
        syncReservationSearchButton();
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
        await reloadReservationSearchIfNeeded();
        showToast("데이터를 새로 불러왔습니다.", "success");
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        state.loading.boot = false;
        render();
    }
}

async function loadAllData() {
    const [themes, popularThemes, adminTimes, reservationPage] = await Promise.all([
        api("/themes"),
        api("/themes/popular-top-10"),
        api("/admin/times"),
        api("/admin/reservations")
    ]);

    state.themes = themes;
    state.popularThemes = popularThemes;
    state.adminTimes = adminTimes;
    state.reservations = reservationPage.content || [];
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

        if (!selectedTime()) {
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
    const formData = new FormData(form);
    const name = String(formData.get("name") || "").trim();
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
        const time = selectedTime();
        const isWaitingApplication = time && !time.available;
        const endpoint = isWaitingApplication ? "/waitings" : "/reservations";

        const reservation = await api(endpoint, {
            method: "POST",
            body: {
                name,
                date: state.selectedDate,
                themeId: state.selectedThemeId,
                timeId: state.selectedTimeId
            }
        });

        state.guestName = "";
        state.selectedThemeId = null;
        state.selectedTimeId = null;
        state.availableTimes = [];
        await loadAllData({silent: true});
        const searchReloadResult = await reloadReservationSearchIfNeeded();
        const paymentPending = !isWaitingApplication && isPaymentPendingReservation(reservation);
        if (paymentPending) {
            rememberPendingPayment(reservation);
        }
        const successMessage = isWaitingApplication
            ? "예약 대기를 신청했습니다."
            : (paymentPending ? "예약이 접수되었습니다. 결제를 진행해 주세요." : "예약이 완료되었습니다.");
        showMutationToast(successMessage, searchReloadResult);
        if (paymentPending) {
            await startPayment(reservation);
        }
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

async function preparePayment(actionTarget) {
    const reservation = findPaymentReservation(actionTarget.dataset.reservationId);

    if (!reservation) {
        showToast("결제할 예약 정보를 찾을 수 없습니다.", "error");
        return;
    }

    if (!isPaymentPendingReservation(reservation)) {
        showToast("결제 대기 중인 예약만 결제할 수 있습니다.", "error");
        return;
    }

    rememberPendingPayment(reservation);
    state.payment.processing = {
        status: "ready",
        orderId: reservation.payment?.orderId || "",
        message: "결제창을 준비하고 있습니다."
    };
    render();

    await startPayment(reservation);
}

async function startPayment(reservation) {
    const orderId = reservation.payment?.orderId || "";

    state.payment.processing = {
        status: "ready",
        orderId,
        message: "결제창을 준비하고 있습니다."
    };
    render();

    try {
        const result = await requestTossPayment(reservation);

        if (!result.ok) {
            state.payment.processing = {
                status: "failed",
                orderId,
                message: result.message
            };
            showToast(result.message, "error");
            render();
            return;
        }

        state.payment.processing = {
            status: "requested",
            orderId,
            message: "결제창에서 결제를 진행해 주세요."
        };
        render();
    } catch (error) {
        const message = error.message || "결제창을 열지 못했습니다. 잠시 후 다시 시도해 주세요.";
        state.payment.processing = {
            status: "failed",
            orderId,
            message
        };
        showToast(message, "error");
        render();
    }
}

async function submitReservationSearch(form) {
    const formData = new FormData(form);
    const username = String(formData.get("username") || "").trim();

    state.reservationSearchName = username;

    if (!username) {
        showToast("예약자 이름을 입력해 주세요.", "error");
        syncReservationSearchButton();
        return;
    }

    await loadSearchedReservations();
}

async function startReservationEdit(reservationId) {
    const reservation = findReservation(reservationId);

    if (!reservation) {
        showToast("예약 정보를 찾을 수 없습니다.", "error");
        return;
    }

    if (isPastReservation(reservation)) {
        showToast("이미 지난 예약은 변경할 수 없습니다.", "error");
        return;
    }

    state.reservationEdit.id = reservation.id;
    state.reservationEdit.date = reservation.date;
    state.reservationEdit.timeId = reservation.time.id;
    state.reservationEdit.times = [];
    state.reservationEdit.loading = false;
    render();
    await loadReservationEditTimes();
}

async function loadReservationEditTimes() {
    const reservation = findReservation(state.reservationEdit.id);

    if (!reservation || !state.reservationEdit.date) {
        state.reservationEdit.times = [];
        state.reservationEdit.timeId = null;
        state.reservationEdit.loading = false;
        render();
        return;
    }

    state.reservationEdit.loading = true;
    render();

    try {
        const query = new URLSearchParams({
            themeId: reservation.theme.id,
            date: state.reservationEdit.date
        }).toString();

        state.reservationEdit.times = await api(`/times?${query}`);

        if (!isSelectableEditTime(reservation, state.reservationEdit.timeId)) {
            state.reservationEdit.timeId = null;
        }
    } catch (error) {
        state.reservationEdit.times = [];
        state.reservationEdit.timeId = null;
        showToast(error.message, "error");
    } finally {
        state.reservationEdit.loading = false;
        render();
    }
}

async function submitReservationEdit(form) {
    const reservationId = Number(form.dataset.reservationId);
    const reservation = findReservation(reservationId);
    const date = state.reservationEdit.date;
    const timeId = Number(state.reservationEdit.timeId);

    if (!reservation) {
        showToast("예약 정보를 찾을 수 없습니다.", "error");
        return;
    }

    if (!date || !timeId) {
        showToast("변경할 날짜와 시간을 선택해 주세요.", "error");
        return;
    }

    if (isPastReservation(reservation)) {
        showToast("이미 지난 예약은 변경할 수 없습니다.", "error");
        clearReservationEdit();
        render();
        return;
    }

    state.submitting = true;
    render();

    try {
        await api(`/reservations/${reservationId}`, {
            method: "PATCH",
            body: {date, timeId}
        });

        clearReservationEdit();
        await loadAllData({silent: true});
        ensureSelectedTheme();
        if (state.selectedThemeId) {
            await loadAvailableTimes({silent: true});
        }
        await reloadReservationSearchIfNeeded();
        showToast("예약을 변경했습니다.", "success");
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        state.submitting = false;
        render();
    }
}

async function submitTheme(form) {
    const formData = new FormData(form);
    const name = String(formData.get("name") || "").trim();
    const description = String(formData.get("description") || "").trim();
    const thumbnailImgUrl = String(formData.get("thumbnailImgUrl") || "").trim();

    if (!name || !description || !thumbnailImgUrl) {
        showToast("테마명, 설명, 이미지 URL을 입력해 주세요.", "error");
        return;
    }

    state.submitting = true;
    render();

    try {
        const createdTheme = await api("/admin/themes", {
            method: "POST",
            body: {name, description, thumbnailImgUrl}
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

async function deleteReservation(reservationId, successMessage = "예약을 삭제했습니다.") {
    await mutateWithReload(() => api(`/reservations/${reservationId}`, {method: "DELETE"}), successMessage);
}

async function deleteWaiting(waitingId) {
    await mutateWithReload(() => api(`/waitings/${waitingId}`, {method: "DELETE"}), "대기 신청을 취소했습니다.");
}

async function submitWaitingPostpone(form) {
    const waitingId = Number(form.dataset.waitingId);
    const waiting = findWaiting(waitingId);
    const formData = new FormData(form);
    const steps = Number(formData.get("steps"));

    if (!waiting) {
        showToast("대기 정보를 찾을 수 없습니다.", "error");
        return;
    }

    if (isPastReservation(waiting)) {
        showToast("이미 지난 대기는 미룰 수 없습니다.", "error");
        return;
    }

    const maxPostponeSteps = Number(waiting.totalRankCount) - Number(waiting.rank);

    if (!Number.isInteger(steps) || steps <= 0) {
        showToast("미룰 칸 수를 입력해 주세요.", "error");
        return;
    }

    if (!Number.isInteger(maxPostponeSteps) || steps > maxPostponeSteps) {
        showToast("자신보다 뒤에 있는 순번까지만 미룰 수 있습니다.", "error");
        return;
    }

    await mutateWithReload(
        () => api(`/waitings/${waitingId}/postpone?${new URLSearchParams({steps})}`, {method: "POST"}),
        `${steps}칸 뒤로 미뤘습니다.`
    );
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
        const searchReloadResult = await reloadReservationSearchIfNeeded();
        showMutationToast(successMessage, searchReloadResult);
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        state.submitting = false;
        render();
    }
}

async function loadSearchedReservations(options = {}) {
    const username = String(options.username || state.reservationSearchName).trim();

    if (!username) {
        state.searchedReservations = [];
        state.searchedWaitings = [];
        state.searchedPayments = [];
        state.reservationSearchSubmitted = false;
        state.reservationSearchSubmittedName = "";
        render();
        return {failed: false, partialFailure: false};
    }

    state.loading.searchedReservations = true;
    state.loading.searchedPayments = true;
    if (!options.silent) {
        render();
    }

    try {
        const query = new URLSearchParams({username}).toString();
        const [reservationResult, waitingResult, paymentResult] = await Promise.allSettled([
            api(`/reservations?${query}`),
            api(`/waitings?${query}`),
            findPaymentHistories(username)
        ]);
        const applicationResults = [reservationResult, waitingResult];
        const failedApplicationResults = applicationResults.filter((result) => result.status === "rejected");
        const failedResults = [reservationResult, waitingResult, paymentResult]
            .filter((result) => result.status === "rejected");

        if (failedApplicationResults.length === applicationResults.length) {
            throw failedApplicationResults[0].reason;
        }

        state.searchedReservations = reservationResult.status === "fulfilled"
            ? reservationResult.value
            : (options.silent ? state.searchedReservations : []);
        state.searchedWaitings = waitingResult.status === "fulfilled"
            ? waitingResult.value
            : (options.silent ? state.searchedWaitings : []);
        state.searchedPayments = paymentResult.status === "fulfilled"
            ? paymentResult.value
            : (options.silent ? state.searchedPayments : []);
        state.reservationSearchSubmitted = true;
        state.reservationSearchSubmittedName = username;
        syncReservationEditWithSearchResults();

        if (!options.silent) {
            const message = `예약 ${state.searchedReservations.length}건, 대기 ${state.searchedWaitings.length}건을 찾았습니다.`;
            showToast(failedResults.length ? `일부 조회에 실패했습니다. ${message}` : message, failedResults.length ? "error" : "success");
        }

        return {failed: false, partialFailure: failedResults.length > 0};
    } catch (error) {
        if (!options.silent) {
            state.searchedReservations = [];
            state.searchedWaitings = [];
            state.searchedPayments = [];
            state.reservationSearchSubmitted = false;
            state.reservationSearchSubmittedName = "";
            clearReservationEdit();
            showToast(error.message, "error");
        }

        return {failed: true, partialFailure: false};
    } finally {
        state.loading.searchedReservations = false;
        state.loading.searchedPayments = false;
        render();
    }
}

async function reloadReservationSearchIfNeeded() {
    const username = state.reservationSearchSubmittedName.trim();

    if (!state.reservationSearchSubmitted || !username) {
        return {failed: false, partialFailure: false};
    }

    return loadSearchedReservations({silent: true, username});
}

function showMutationToast(successMessage, searchReloadResult) {
    const searchRefreshFailed = searchReloadResult?.failed || searchReloadResult?.partialFailure;

    showToast(
        searchRefreshFailed ? `${successMessage} 일부 조회 결과를 새로고침하지 못했습니다.` : successMessage,
        searchRefreshFailed ? "error" : "success"
    );
}

function clearReservationSearch() {
    state.reservationSearchName = "";
    state.searchedReservations = [];
    state.searchedWaitings = [];
    state.searchedPayments = [];
    state.reservationSearchSubmitted = false;
    state.reservationSearchSubmittedName = "";
    state.loading.searchedReservations = false;
    state.loading.searchedPayments = false;
    clearReservationEdit();
    render();
}

function clearReservationEdit() {
    state.reservationEdit.id = null;
    state.reservationEdit.date = "";
    state.reservationEdit.timeId = null;
    state.reservationEdit.times = [];
    state.reservationEdit.loading = false;
}

function syncReservationEditWithSearchResults() {
    if (!state.reservationEdit.id) {
        return;
    }

    const stillExists = state.searchedReservations.some(
        (reservation) => Number(reservation.id) === Number(state.reservationEdit.id)
    );

    if (!stillExists) {
        clearReservationEdit();
    }
}

function findReservation(reservationId) {
    return [...state.searchedReservations, ...state.reservations]
        .find((reservation) => Number(reservation.id) === Number(reservationId)) || null;
}

function findPaymentReservation(reservationId) {
    const contextReservation = state.payment.pendingContext?.reservation;

    if (contextReservation && Number(contextReservation.id) === Number(reservationId)) {
        return contextReservation;
    }

    return findReservation(reservationId);
}

function findWaiting(waitingId) {
    return state.searchedWaitings
        .find((waiting) => Number(waiting.id) === Number(waitingId)) || null;
}

function isSelectableEditTime(reservation, timeId) {
    const time = state.reservationEdit.times.find((candidate) => Number(candidate.id) === Number(timeId));

    if (!time) {
        return false;
    }

    return time.available || isCurrentReservationTime(reservation, time.id);
}

function isCurrentReservationTime(reservation, timeId) {
    return reservation.date === state.reservationEdit.date &&
        Number(reservation.time.id) === Number(timeId);
}

function hasReservationsForTheme(themeId) {
    return state.reservations.some((reservation) => Number(reservation.theme?.id) === Number(themeId));
}

function hasReservationsForTime(timeId) {
    return state.reservations.some((reservation) => Number(reservation.time?.id) === Number(timeId));
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
    state.availableTimes = [];
    state.loading.times = false;
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

function syncReservationSearchButton() {
    const button = appEl.querySelector("#reservation-search-form .submit-button");
    if (!button) {
        return;
    }

    button.disabled = !state.reservationSearchName.trim() || state.loading.searchedReservations;
}

function openConfirm(title, body, onConfirm, confirmLabel = "삭제") {
    state.confirm = {title, body, onConfirm, confirmLabel};
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
