document.addEventListener("DOMContentLoaded", () => {
    const state = {
        themes: [],
        rankedThemes: [],
        slots: [],
        selectedThemeId: null,
        selectedDate: "",
        selectedSlotId: null,
        currentUserName: "",
        userReservations: [],
        editingReservationId: null,
        editSlotsByReservationId: {},
        editDateByReservationId: {},
        editSlotIdByReservationId: {}
    };

    const modalTriggers = document.querySelectorAll("[data-modal-open]");
    const modalClosers = document.querySelectorAll("[data-modal-close]");
    const themeList = document.getElementById("theme-list");
    const rankList = document.getElementById("rank-list");
    const timeList = document.getElementById("time-list");
    const timeSection = document.getElementById("time-section");
    const selectionSummary = document.getElementById("selection-summary");
    const reservationFormSummary = document.getElementById("reservation-form-summary");
    const selectedSlotInput = document.getElementById("selected-slot-id");
    const reservationDateInput = document.getElementById("reservation-date");
    const showTimesButton = document.getElementById("show-times-button");
    const resetSelectionButton = document.getElementById("reset-selection-button");
    const statusStrip = document.getElementById("status-strip");
    const authStatus = document.getElementById("auth-status");
    const currentUserSummary = document.getElementById("current-user-summary");
    const logoutButton = document.getElementById("logout-button");
    const signupForm = document.getElementById("signup-form");
    const loginForm = document.getElementById("login-form");
    const signupMessage = document.getElementById("signup-message");
    const loginMessage = document.getElementById("login-message");
    const reservationForm = document.getElementById("reservation-form");
    const reservationMessage = document.getElementById("reservation-message");
    const refreshReservationsButton = document.getElementById("refresh-reservations-button");
    const userReservationDescription = document.getElementById("user-reservation-description");
    const userReservationMessage = document.getElementById("user-reservation-message");
    const userReservationList = document.getElementById("user-reservation-list");

    const today = new Date();
    reservationDateInput.value = localDateString(today);

    document.querySelectorAll("[data-scroll-target]").forEach((button) => {
        button.addEventListener("click", () => {
            document.getElementById(button.dataset.scrollTarget)?.scrollIntoView({
                behavior: "smooth",
                block: "start"
            });
        });
    });

    modalTriggers.forEach((trigger) => {
        trigger.addEventListener("click", () => {
            const modal = document.getElementById(trigger.dataset.modalOpen);
            if (!modal) {
                return;
            }
            modal.hidden = false;
            document.body.classList.add("modal-open");
        });
    });

    modalClosers.forEach((closer) => {
        closer.addEventListener("click", () => {
            const modal = closer.closest(".modal");
            if (!modal) {
                return;
            }
            modal.hidden = true;
            document.body.classList.remove("modal-open");
        });
    });

    document.addEventListener("keydown", (event) => {
        if (event.key !== "Escape") {
            return;
        }
        document.querySelectorAll(".modal").forEach((modal) => {
            modal.hidden = true;
        });
        document.body.classList.remove("modal-open");
    });

    function localDateString(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return `${year}-${month}-${day}`;
    }

    function escapeHtml(value) {
        return String(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function formatWaitingStatus(waitingNumber) {
        if (waitingNumber === 0) {
            return "바로 예약 확정";
        }
        return `현재 대기 ${waitingNumber}명`;
    }

    function setStatus(message) {
        statusStrip.querySelector("p").textContent = message;
    }

    function setMessage(element, message, kind = "") {
        element.textContent = message;
        element.className = "form-message";
        if (kind) {
            element.classList.add(kind);
        }
    }

    function findTheme(themeId = state.selectedThemeId) {
        return state.themes.find((theme) => theme.id === themeId);
    }

    function updateAuthUi() {
        const loggedIn = Boolean(state.currentUserName);
        authStatus.textContent = loggedIn
            ? `${state.currentUserName}님으로 로그인되어 있습니다.`
            : "로그인이 필요합니다.";
        currentUserSummary.textContent = loggedIn
            ? `${state.currentUserName}님으로 예약할 수 있습니다.`
            : "로그인 후 예약할 수 있습니다.";
        userReservationDescription.textContent = loggedIn
            ? `${state.currentUserName}님의 예약 목록입니다.`
            : "로그인 후 예약 목록을 확인할 수 있습니다.";
        logoutButton.disabled = !loggedIn;
        reservationForm.querySelector('button[type="submit"]').disabled = !loggedIn;
    }

    async function parseResponse(response) {
        if (response.status === 204) {
            return null;
        }
        const text = await response.text();
        return text ? JSON.parse(text) : null;
    }

    async function fetchJson(url, options = {}) {
        const response = await fetch(url, {
            ...options,
            headers: {
                Accept: "application/json",
                ...(options.body ? { "Content-Type": "application/json" } : {}),
                ...(options.headers || {})
            }
        });
        const result = await parseResponse(response);
        if (!response.ok) {
            const error = new Error(result?.message || "요청에 실패했습니다.");
            error.status = response.status;
            error.result = result;
            throw error;
        }
        return result;
    }

    function renderThemes() {
        themeList.innerHTML = state.themes.map((theme) => `
            <label class="theme-card theme-card-refined${state.selectedThemeId === theme.id ? " selected" : ""}">
                <input type="radio" name="themeId" value="${theme.id}" ${state.selectedThemeId === theme.id ? "checked" : ""}>
                <img class="theme-thumbnail" src="${escapeHtml(theme.url)}" alt="${escapeHtml(theme.name)}">
                <span class="theme-name">${escapeHtml(theme.name)}</span>
                <span class="theme-description">${escapeHtml(theme.content)}</span>
            </label>
        `).join("");

        themeList.querySelectorAll('input[name="themeId"]').forEach((input) => {
            input.addEventListener("change", () => {
                state.selectedThemeId = Number(input.value);
                renderThemes();
            });
        });
    }

    function renderRankedThemes() {
        rankList.innerHTML = state.rankedThemes.map((theme, index) => `
            <li>
                <img class="rank-thumbnail" src="${escapeHtml(theme.thumbnailUrl)}" alt="${escapeHtml(theme.name)}">
                <span class="rank-number">${index + 1}</span>
                <div>
                    <strong>${escapeHtml(theme.name)}</strong>
                </div>
            </li>
        `).join("");
    }

    function renderSlots() {
        if (!state.slots.length) {
            timeList.innerHTML = `
                <div class="empty-state">
                    <strong>선택한 조건에 가능한 시간이 없습니다.</strong>
                </div>
            `;
            selectedSlotInput.value = "";
            return;
        }

        timeList.innerHTML = state.slots.map((slot) => `
            <label class="time-card time-card-refined available${state.selectedSlotId === slot.slotId ? " selected" : ""}">
                <input type="radio" name="slotId" value="${slot.slotId}" form="reservation-form" ${state.selectedSlotId === slot.slotId ? "checked" : ""}>
                <span class="card-pill">${slot.waitingNumber === 0 ? "CONFIRMED" : "WAITING"}</span>
                <span class="time-value">${slot.startAt}</span>
                <span class="time-status">${formatWaitingStatus(slot.waitingNumber)}</span>
            </label>
        `).join("");

        timeList.querySelectorAll('input[name="slotId"]').forEach((input) => {
            input.addEventListener("change", () => {
                state.selectedSlotId = Number(input.value);
                selectedSlotInput.value = String(state.selectedSlotId);
                renderSlots();
            });
        });

        selectedSlotInput.value = state.selectedSlotId ? String(state.selectedSlotId) : "";
    }

    function renderReservations() {
        if (!state.currentUserName) {
            userReservationList.innerHTML = `
                <div class="empty-state">
                    <strong>로그인 후 예약 목록을 확인할 수 있습니다.</strong>
                </div>
            `;
            return;
        }

        if (state.userReservations.length === 0) {
            userReservationList.innerHTML = `
                <div class="empty-state">
                    <strong>${escapeHtml(state.currentUserName)}</strong>님의 예약이 없습니다.
                </div>
            `;
            return;
        }

        userReservationList.innerHTML = state.userReservations.map((reservation) => {
            const slot = reservation.slot;
            const isEditing = state.editingReservationId === reservation.id;
            const currentDate = state.editDateByReservationId[reservation.id] || slot.date;
            const currentSlotId = state.editSlotIdByReservationId[reservation.id] || slot.id;
            const editSlots = state.editSlotsByReservationId[reservation.id] || [];
            const slotOptions = editSlots.length
                ? editSlots.map((editSlot) => `
                    <option value="${editSlot.slotId}" ${currentSlotId === editSlot.slotId ? "selected" : ""}>
                        ${editSlot.startAt} · ${formatWaitingStatus(editSlot.waitingNumber)}
                    </option>
                `).join("")
                : `<option value="">가능한 시간이 없습니다.</option>`;

            return `
                <article class="user-reservation-card">
                    <div class="user-reservation-main">
                        <div>
                            <span class="card-pill">예약 번호 ${reservation.id}</span>
                            <h3>${escapeHtml(slot.theme.name)}</h3>
                            <p>${escapeHtml(slot.theme.content)}</p>
                        </div>
                        <div class="reservation-date-time">
                            <strong>${slot.date}</strong>
                            <span>${slot.startAt.startAt}</span>
                            <span>${formatReservationStatus(reservation.status, reservation.waitingNumber)}</span>
                        </div>
                    </div>
                    ${isEditing ? `
                        <form class="reservation-edit-form" data-edit-form data-id="${reservation.id}">
                            <label>
                                날짜
                                <input type="date" name="date" data-edit-date data-id="${reservation.id}" value="${currentDate}">
                            </label>
                            <label>
                                시간
                                <select name="slotId" data-edit-slot data-id="${reservation.id}">
                                    ${slotOptions}
                                </select>
                            </label>
                            <div class="reservation-card-actions">
                                <button type="submit" class="primary-button">변경 저장</button>
                                <button type="button" class="secondary-button" data-edit-cancel>취소</button>
                            </div>
                        </form>
                    ` : `
                        <div class="reservation-card-actions">
                            <button type="button" class="secondary-button" data-edit-id="${reservation.id}">변경</button>
                            <button type="button" class="danger-button" data-cancel-id="${reservation.id}">예약 취소</button>
                        </div>
                    `}
                </article>
            `;
        }).join("");

        bindReservationEvents();
    }

    function formatReservationStatus(status, waitingNumber) {
        if (status === "CONFIRMED") {
            return "예약 확정";
        }
        if (status === "WAITING") {
            return waitingNumber ? `대기 ${waitingNumber}번` : "예약 대기";
        }
        return status;
    }

    function bindReservationEvents() {
        userReservationList.querySelectorAll("[data-cancel-id]").forEach((button) => {
            button.addEventListener("click", async () => {
                await cancelReservation(Number(button.dataset.cancelId));
            });
        });

        userReservationList.querySelectorAll("[data-edit-id]").forEach((button) => {
            button.addEventListener("click", async () => {
                const reservation = findReservation(Number(button.dataset.editId));
                if (!reservation) {
                    return;
                }
                state.editingReservationId = reservation.id;
                state.editDateByReservationId[reservation.id] = reservation.slot.date;
                state.editSlotIdByReservationId[reservation.id] = reservation.slot.id;
                renderReservations();
                await loadEditSlots(reservation, reservation.slot.date);
            });
        });

        userReservationList.querySelectorAll("[data-edit-cancel]").forEach((button) => {
            button.addEventListener("click", () => {
                state.editingReservationId = null;
                renderReservations();
            });
        });

        userReservationList.querySelectorAll("[data-edit-date]").forEach((input) => {
            input.addEventListener("change", async () => {
                const reservation = findReservation(Number(input.dataset.id));
                if (!reservation) {
                    return;
                }
                state.editDateByReservationId[reservation.id] = input.value;
                await loadEditSlots(reservation, input.value);
            });
        });

        userReservationList.querySelectorAll("[data-edit-form]").forEach((form) => {
            form.addEventListener("submit", async (event) => {
                event.preventDefault();
                const formData = new FormData(form);
                const slotId = Number(formData.get("slotId"));
                if (!slotId) {
                    setMessage(userReservationMessage, "변경할 시간을 선택해 주세요.", "error");
                    return;
                }
                await updateReservation(Number(form.dataset.id), slotId);
            });
        });
    }

    function findReservation(id) {
        return state.userReservations.find((reservation) => reservation.id === id);
    }

    async function loadThemesAndRanks() {
        const [themeResult, rankResult] = await Promise.all([
            fetchJson("/themes"),
            fetchJson("/themes/rank")
        ]);

        state.themes = themeResult.themes;
        state.rankedThemes = rankResult.popularThemes;

        renderThemes();
        renderRankedThemes();
    }

    async function loadCurrentReservations({ silent = false } = {}) {
        try {
            const result = await fetchJson("/reservations");
            state.currentUserName = result.username;
            state.userReservations = result.reservations;
            state.editingReservationId = null;
            state.editSlotsByReservationId = {};
            state.editDateByReservationId = {};
            state.editSlotIdByReservationId = {};
            updateAuthUi();
            renderReservations();
        } catch (error) {
            if (error.status === 401) {
                state.currentUserName = "";
                state.userReservations = [];
                state.editingReservationId = null;
                state.editSlotsByReservationId = {};
                state.editDateByReservationId = {};
                state.editSlotIdByReservationId = {};
                updateAuthUi();
                renderReservations();
                if (!silent) {
                    setMessage(userReservationMessage, "로그인 후 예약을 확인할 수 있습니다.", "error");
                }
                return;
            }
            if (!silent) {
                setMessage(userReservationMessage, error.message, "error");
            }
        }
    }

    async function loadSlotsForSelection() {
        if (!state.selectedThemeId || !state.selectedDate) {
            setStatus("테마와 날짜를 모두 선택해 주세요.");
            return;
        }

        const theme = findTheme();
        if (!theme) {
            setStatus("선택한 테마를 찾을 수 없습니다.");
            return;
        }

        const result = await fetchJson(
            `/reservation-slots?themeId=${state.selectedThemeId}&date=${state.selectedDate}`
        );
        state.slots = result.reservationSlots;
        state.selectedSlotId = state.slots[0]?.slotId || null;
        renderSlots();
        timeSection.hidden = false;
        selectionSummary.textContent = `${theme.name} · ${state.selectedDate} 기준으로 가능한 시간입니다.`;
        reservationFormSummary.textContent = `${theme.name} · ${state.selectedDate}`;
        setStatus(state.slots.length > 0 ? "가능한 시간을 불러왔습니다." : "선택한 조건에 가능한 시간이 없습니다.");
        timeSection.scrollIntoView({ behavior: "smooth", block: "start" });
    }

    async function loadEditSlots(reservation, date) {
        try {
            const result = await fetchJson(
                `/reservation-slots?themeId=${reservation.slot.theme.id}&date=${date}`
            );
            const slots = result.reservationSlots;
            state.editSlotsByReservationId[reservation.id] = slots;
            if (!slots.some((slot) => slot.slotId === state.editSlotIdByReservationId[reservation.id])) {
                state.editSlotIdByReservationId[reservation.id] = slots[0]?.slotId || null;
            }
            renderReservations();
        } catch (error) {
            setMessage(userReservationMessage, "변경 가능한 시간을 불러오지 못했습니다.", "error");
        }
    }

    async function cancelReservation(id) {
        if (!window.confirm("예약을 취소하시겠습니까?")) {
            return;
        }

        const response = await fetch(`/reservations/${id}`, {
            method: "DELETE",
            headers: { Accept: "application/json" }
        });
        const result = await parseResponse(response);
        if (!response.ok) {
            setMessage(userReservationMessage, result?.message || "예약 취소 중 문제가 발생했습니다.", "error");
            return;
        }

        setMessage(userReservationMessage, "예약이 취소되었습니다.", "success");
        await loadCurrentReservations({ silent: true });
    }

    async function updateReservation(id, slotId) {
        const response = await fetch(`/reservations/${id}`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                Accept: "application/json"
            },
            body: JSON.stringify({ slotId })
        });
        const result = await parseResponse(response);
        if (!response.ok) {
            setMessage(userReservationMessage, result?.message || "예약 변경 중 문제가 발생했습니다.", "error");
            return;
        }

        setMessage(userReservationMessage, "예약이 변경되었습니다.", "success");
        await loadCurrentReservations({ silent: true });
    }

    async function submitAuth(endpoint, form, messageElement) {
        const formData = new FormData(form);
        const payload = {
            name: String(formData.get("name") || "").trim(),
            password: String(formData.get("password") || "").trim()
        };

        try {
            const result = await fetchJson(endpoint, {
                method: "POST",
                body: JSON.stringify(payload)
            });
            state.currentUserName = result.name;
            setMessage(messageElement, `${result.name}님으로 로그인되었습니다.`, "success");
            form.reset();
            updateAuthUi();
            await loadCurrentReservations({ silent: true });
        } catch (error) {
            setMessage(messageElement, error.message, "error");
        }
    }

    signupForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        await submitAuth("/signup", signupForm, signupMessage);
    });

    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        await submitAuth("/login", loginForm, loginMessage);
    });

    logoutButton.addEventListener("click", async () => {
        try {
            await fetchJson("/logout", { method: "DELETE" });
        } catch (error) {
            // logout should still clear local UI state
        }
        state.currentUserName = "";
        state.userReservations = [];
        state.editingReservationId = null;
        state.editSlotsByReservationId = {};
        state.editDateByReservationId = {};
        state.editSlotIdByReservationId = {};
        setMessage(loginMessage, "로그아웃했습니다.", "success");
        updateAuthUi();
        renderReservations();
    });

    refreshReservationsButton.addEventListener("click", async () => {
        await loadCurrentReservations();
    });

    showTimesButton.addEventListener("click", async () => {
        state.selectedThemeId = state.selectedThemeId || Number(themeList.querySelector('input[name="themeId"]:checked')?.value || 0);
        state.selectedDate = reservationDateInput.value;
        if (!state.selectedThemeId || !state.selectedDate) {
            setStatus("테마와 날짜를 모두 선택해 주세요.");
            return;
        }

        try {
            await loadSlotsForSelection();
        } catch (error) {
            setStatus(error.message);
        }
    });

    resetSelectionButton.addEventListener("click", () => {
        state.selectedThemeId = null;
        state.selectedDate = reservationDateInput.value;
        state.selectedSlotId = null;
        state.slots = [];
        renderThemes();
        renderSlots();
        timeSection.hidden = true;
        setStatus("테마와 날짜를 먼저 선택해 주세요.");
    });

    reservationDateInput.addEventListener("change", () => {
        state.selectedDate = reservationDateInput.value;
    });

    reservationForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (!state.currentUserName) {
            setMessage(reservationMessage, "로그인 후 예약할 수 있습니다.", "error");
            return;
        }

        const formData = new FormData(reservationForm);
        const slotId = Number(formData.get("slotId"));
        if (!slotId) {
            setMessage(reservationMessage, "예약할 시간을 선택해 주세요.", "error");
            return;
        }

        try {
            const result = await fetchJson("/reservations", {
                method: "POST",
                body: JSON.stringify({ slotId })
            });
            setMessage(
                reservationMessage,
                `${result.theme.name} 예약이 완료되었습니다. (${result.date} ${result.startAt})`,
                "success"
            );
            await loadCurrentReservations({ silent: true });
        } catch (error) {
            setMessage(reservationMessage, error.message, "error");
        }
    });

    themeList.addEventListener("change", (event) => {
        if (event.target.name !== "themeId") {
            return;
        }
        state.selectedThemeId = Number(event.target.value);
        renderThemes();
    });

    userReservationList.addEventListener("change", async (event) => {
        const input = event.target;
        if (input.dataset.editDate === undefined) {
            return;
        }
        const reservation = findReservation(Number(input.dataset.id));
        if (!reservation) {
            return;
        }
        state.editDateByReservationId[reservation.id] = input.value;
        await loadEditSlots(reservation, input.value);
    });

    loadThemesAndRanks()
        .then(() => loadCurrentReservations({ silent: true }))
        .catch(() => {
            setStatus("초기 데이터를 불러오지 못했습니다.");
        })
        .finally(() => {
            updateAuthUi();
            renderReservations();
        });
});
