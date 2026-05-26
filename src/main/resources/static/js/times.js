document.addEventListener("DOMContentLoaded", () => {
    const state = {
        themes: [],
        dates: [],
        rankedThemes: [],
        times: [],
        selectedThemeId: null,
        selectedDateId: null,
        userReservationName: "",
        userReservations: [],
        editingReservationId: null,
        editDateByReservationId: {},
        editTimesByReservationId: {}
    };

    const modalTriggers = document.querySelectorAll("[data-modal-open]");
    const modalClosers = document.querySelectorAll("[data-modal-close]");
    const themeList = document.getElementById("theme-list");
    const dateList = document.getElementById("date-list");
    const rankList = document.getElementById("rank-list");
    const timeList = document.getElementById("time-list");
    const timeSection = document.getElementById("time-section");
    const selectionSummary = document.getElementById("selection-summary");
    const reservationFormSummary = document.getElementById("reservation-form-summary");
    const selectedThemeInput = document.getElementById("selected-theme-id");
    const selectedDateInput = document.getElementById("selected-date-id");
    const showTimesButton = document.getElementById("show-times-button");
    const resetSelectionButton = document.getElementById("reset-selection-button");
    const statusStrip = document.getElementById("status-strip");
    const userReservationSearchForm = document.getElementById("user-reservation-search-form");
    const userReservationNameInput = document.getElementById("user-reservation-name");
    const userReservationMessage = document.getElementById("user-reservation-message");
    const userReservationList = document.getElementById("user-reservation-list");

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

    function updateStatus(message) {
        statusStrip.querySelector("p").textContent = message;
    }

    function formatDate(date) {
        return date;
    }

    function formatShortDate(date) {
        return date.slice(5).replace("-", ".");
    }

    function escapeHtml(value) {
        return String(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function findTheme() {
        return state.themes.find((theme) => theme.id === state.selectedThemeId);
    }

    function findDate() {
        return state.dates.find((date) => date.id === state.selectedDateId);
    }

    function renderThemes() {
        themeList.innerHTML = state.themes.map((theme) => `
            <label class="theme-card theme-card-refined${state.selectedThemeId === theme.id ? " selected" : ""}">
                <input type="radio" name="themeId" value="${theme.id}" ${state.selectedThemeId === theme.id ? "checked" : ""}>
                <img class="theme-thumbnail" src="/images/theme-placeholder.svg" alt="${theme.name}">
                <span class="theme-name">${theme.name}</span>
                <span class="theme-description">${theme.content}</span>
            </label>
        `).join("");

        themeList.querySelectorAll('input[name="themeId"]').forEach((input) => {
            input.addEventListener("change", () => {
                state.selectedThemeId = Number(input.value);
                renderThemes();
            });
        });
    }

    function renderDates() {
        dateList.innerHTML = state.dates.map((date) => `
            <label class="date-card date-card-refined${state.selectedDateId === date.id ? " selected" : ""}">
                <input type="radio" name="dateId" value="${date.id}" ${state.selectedDateId === date.id ? "checked" : ""}>
                <span class="date-day">${formatShortDate(date.reservationDate)}</span>
                <span class="date-full">${formatDate(date.reservationDate)}</span>
            </label>
        `).join("");

        dateList.querySelectorAll('input[name="dateId"]').forEach((input) => {
            input.addEventListener("change", () => {
                state.selectedDateId = Number(input.value);
                renderDates();
            });
        });
    }

    function renderRankedThemes() {
        rankList.innerHTML = state.rankedThemes.map((theme, index) => `
            <li>
                <img class="rank-thumbnail" src="/images/theme-placeholder.svg" alt="${theme.themeName}">
                <span class="rank-number">${index + 1}</span>
                <div>
                    <strong>${theme.themeName}</strong>
                </div>
            </li>
        `).join("");
    }

    function renderTimes() {
        timeList.innerHTML = state.times.map((time) => `
            <label class="time-card time-card-refined ${time.available ? "available" : "unavailable"}">
                <input type="radio" name="timeId" value="${time.timeId}" form="reservation-form" ${time.available ? "" : "disabled"}>
                <span class="card-pill">${time.available ? "OPEN" : "CLOSED"}</span>
                <span class="time-value">${time.startAt}</span>
                <span class="time-status">${time.available ? "예약 가능" : "마감"}</span>
            </label>
        `).join("");
    }

    function renderUserReservations() {
        if (!state.userReservationName) {
            userReservationList.innerHTML = "";
            return;
        }

        if (state.userReservations.length === 0) {
            userReservationList.innerHTML = `
                <div class="empty-state">
                    <strong>${escapeHtml(state.userReservationName)}</strong>님의 예약이 없습니다.
                </div>
            `;
            return;
        }

        userReservationList.innerHTML = state.userReservations.map((reservation) => {
            const isEditing = state.editingReservationId === reservation.reservationId;
            const selectedStartWhen = state.editDateByReservationId[reservation.reservationId] || reservation.date.startWhen;
            const editTimes = state.editTimesByReservationId[reservation.reservationId] || [];
            const dateOptions = state.dates.map((date) => `
                <option value="${date.reservationDate}" data-date-id="${date.id}" ${date.reservationDate === selectedStartWhen ? "selected" : ""}>
                    ${date.reservationDate}
                </option>
            `).join("");
            const timeOptions = buildEditTimeOptions(reservation, editTimes);

            return `
                <article class="user-reservation-card">
                    <div class="user-reservation-main">
                        <div>
                            <span class="card-pill">예약 번호 ${reservation.reservationId}</span>
                            <h3>${escapeHtml(reservation.theme.name)}</h3>
                            <p>${escapeHtml(reservation.theme.content)}</p>
                        </div>
                        <div class="reservation-date-time">
                            <strong>${reservation.date.startWhen}</strong>
                            <span>${reservation.time.startAt}</span>
                        </div>
                    </div>
                    ${isEditing ? `
                        <form class="reservation-edit-form" data-edit-form data-id="${reservation.reservationId}">
                            <label>
                                날짜
                                <select name="startWhen" data-edit-date data-id="${reservation.reservationId}">
                                    ${dateOptions}
                                </select>
                            </label>
                            <label>
                                시간
                                <select name="startAt" data-edit-time>
                                    ${timeOptions}
                                </select>
                            </label>
                            <div class="reservation-card-actions">
                                <button type="submit" class="primary-button">변경 저장</button>
                                <button type="button" class="secondary-button" data-edit-cancel>취소</button>
                            </div>
                        </form>
                    ` : `
                        <div class="reservation-card-actions">
                            <button type="button" class="secondary-button" data-edit-id="${reservation.reservationId}">변경</button>
                            <button type="button" class="danger-button" data-cancel-id="${reservation.reservationId}">예약 취소</button>
                        </div>
                    `}
                </article>
            `;
        }).join("");

        bindUserReservationEvents();
    }

    function buildEditTimeOptions(reservation, times) {
        const options = new Map();
        options.set(reservation.time.startAt, reservation.time.startAt);
        times
            .filter((time) => time.available || time.startAt === reservation.time.startAt)
            .forEach((time) => options.set(time.startAt, time.startAt));

        return [...options.values()].map((startAt) => `
            <option value="${startAt}" ${startAt === reservation.time.startAt ? "selected" : ""}>${startAt}</option>
        `).join("");
    }

    function bindUserReservationEvents() {
        userReservationList.querySelectorAll("[data-cancel-id]").forEach((button) => {
            button.addEventListener("click", async () => {
                await cancelReservation(Number(button.dataset.cancelId));
            });
        });

        userReservationList.querySelectorAll("[data-edit-id]").forEach((button) => {
            button.addEventListener("click", async () => {
                const reservation = findUserReservation(Number(button.dataset.editId));
                state.editingReservationId = reservation.reservationId;
                state.editDateByReservationId[reservation.reservationId] = reservation.date.startWhen;
                renderUserReservations();
                await loadEditTimes(reservation, reservation.date.id);
            });
        });

        userReservationList.querySelectorAll("[data-edit-cancel]").forEach((button) => {
            button.addEventListener("click", () => {
                state.editingReservationId = null;
                renderUserReservations();
            });
        });

        userReservationList.querySelectorAll("[data-edit-date]").forEach((select) => {
            select.addEventListener("change", async () => {
                const reservation = findUserReservation(Number(select.dataset.id));
                const selectedOption = select.options[select.selectedIndex];
                state.editDateByReservationId[reservation.reservationId] = selectedOption.value;
                await loadEditTimes(reservation, Number(selectedOption.dataset.dateId));
            });
        });

        userReservationList.querySelectorAll("[data-edit-form]").forEach((form) => {
            form.addEventListener("submit", async (event) => {
                event.preventDefault();
                const formData = new FormData(form);
                await updateReservation(Number(form.dataset.id), {
                    startWhen: formData.get("startWhen"),
                    startAt: formData.get("startAt")
                });
            });
        });
    }

    function findUserReservation(id) {
        return state.userReservations.find((reservation) => reservation.reservationId === id);
    }

    function setUserReservationMessage(text, type = "") {
        userReservationMessage.textContent = text;
        userReservationMessage.className = "form-message";
        if (type) {
            userReservationMessage.classList.add(type);
        }
    }

    async function parseResponse(response) {
        const text = await response.text();
        if (!text) {
            return null;
        }
        return JSON.parse(text);
    }

    async function fetchJson(url) {
        const response = await fetch(url, { headers: { Accept: "application/json" } });
        if (!response.ok) {
            throw new Error(`Request failed: ${url}`);
        }
        return response.json();
    }

    async function loadUserReservations(name) {
        const response = await fetch(`/reservations?name=${encodeURIComponent(name)}`, {
            headers: { Accept: "application/json" }
        });
        const result = await parseResponse(response);

        if (!response.ok) {
            throw new Error(result?.message || "예약 목록을 불러오지 못했습니다.");
        }

        state.userReservationName = result.name;
        state.userReservations = result.reservation;
        state.editingReservationId = null;
        state.editDateByReservationId = {};
        state.editTimesByReservationId = {};
        renderUserReservations();
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
            setUserReservationMessage(result?.message || "예약 취소 중 문제가 발생했습니다.", "error");
            return;
        }

        setUserReservationMessage("예약이 취소되었습니다.", "success");
        await loadUserReservations(state.userReservationName);
    }

    async function updateReservation(id, payload) {
        const response = await fetch(`/reservations/${id}`, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify(payload)
        });
        const result = await parseResponse(response);

        if (!response.ok) {
            setUserReservationMessage(result?.message || "예약 변경 중 문제가 발생했습니다.", "error");
            return;
        }

        setUserReservationMessage("예약이 변경되었습니다.", "success");
        await loadUserReservations(state.userReservationName);
    }

    async function loadEditTimes(reservation, dateId) {
        try {
            const times = await fetchJson(
                `/reservation-times/availability?themeId=${reservation.theme.id}&dateId=${dateId}`
            );
            state.editTimesByReservationId[reservation.reservationId] = times;
            renderUserReservations();
        } catch (error) {
            setUserReservationMessage("변경 가능한 시간을 불러오지 못했습니다.", "error");
        }
    }

    async function loadInitialData() {
        const [themes, dates, rankedThemes] = await Promise.all([
            fetchJson("/themes"),
            fetchJson("/reservation-dates"),
            fetchJson("/themes/rank")
        ]);

        state.themes = themes;
        state.dates = dates;
        state.rankedThemes = rankedThemes;

        renderThemes();
        renderDates();
        renderRankedThemes();
    }

    showTimesButton.addEventListener("click", async () => {
        if (!state.selectedThemeId || !state.selectedDateId) {
            updateStatus("테마와 날짜를 모두 선택해 주세요.");
            return;
        }

        const selectedTheme = findTheme();
        const selectedDate = findDate();

        state.times = await fetchJson(
            `/reservation-times/availability?themeId=${state.selectedThemeId}&dateId=${state.selectedDateId}`
        );
        renderTimes();
        selectedThemeInput.value = String(state.selectedThemeId);
        selectedDateInput.value = String(state.selectedDateId);
        selectionSummary.textContent = `${selectedTheme.name} · ${selectedDate.reservationDate} 기준으로 가능한 시간입니다.`;
        reservationFormSummary.textContent = `${selectedTheme.name} · ${selectedDate.reservationDate}`;
        timeSection.hidden = false;
        updateStatus("가능한 시간을 불러왔습니다.");
        timeSection.scrollIntoView({ behavior: "smooth", block: "start" });
    });

    resetSelectionButton.addEventListener("click", () => {
        state.selectedThemeId = null;
        state.selectedDateId = null;
        state.times = [];
        renderThemes();
        renderDates();
        timeSection.hidden = true;
        updateStatus("테마와 날짜를 먼저 선택해 주세요.");
    });

    const reservationForm = document.getElementById("reservation-form");
    const message = document.getElementById("reservation-message");

    reservationForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const formData = new FormData(reservationForm);
        const payload = {
            name: formData.get("name"),
            themeId: Number(formData.get("themeId")),
            dateId: Number(formData.get("dateId")),
            timeId: Number(formData.get("timeId"))
        };

        message.textContent = "";
        message.className = "form-message";

        try {
            const response = await fetch("/reservations", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify(payload)
            });

            const result = await response.json();

            if (!response.ok) {
                message.textContent = result.message || "예약 요청 중 문제가 발생했습니다.";
                message.classList.add("error");
                return;
            }

            message.textContent = result.name + "님의 예약이 완료되었습니다.";
            message.classList.add("success");

            userReservationNameInput.value = result.name;
            await loadUserReservations(result.name);

            window.setTimeout(() => {
                window.location.reload();
            }, 600);
        } catch (error) {
            message.textContent = "서버와 통신하지 못했습니다. 잠시 후 다시 시도해 주세요.";
            message.classList.add("error");
        }
    });

    userReservationSearchForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const name = new FormData(userReservationSearchForm).get("name").trim();
        if (!name) {
            setUserReservationMessage("예약자 이름을 입력해 주세요.", "error");
            return;
        }

        try {
            setUserReservationMessage("");
            await loadUserReservations(name);
        } catch (error) {
            setUserReservationMessage(error.message, "error");
        }
    });

    loadInitialData().catch(() => {
        updateStatus("초기 데이터를 불러오지 못했습니다.");
    });
});
