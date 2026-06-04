document.addEventListener("DOMContentLoaded", () => {
    const TOKEN_KEY = "roomescape-admin-token";
    const state = {
        token: localStorage.getItem(TOKEN_KEY) || ""
    };

    const tokenInput = document.getElementById("admin-token");
    const tokenForm = document.getElementById("admin-token-form");
    const tokenMessage = document.getElementById("token-message");
    const themesList = document.getElementById("themes-list");
    const datesList = document.getElementById("dates-list");
    const timesList = document.getElementById("times-list");
    const reservationsList = document.getElementById("reservations-list");
    const themeForm = document.getElementById("theme-form");
    const dateForm = document.getElementById("date-form");
    const timeForm = document.getElementById("time-form");
    const panels = document.querySelectorAll(".admin-panel");
    const tabButtons = document.querySelectorAll("[data-tab-target]");
    const refreshButtons = document.querySelectorAll("[data-refresh-target]");
    const adminModal = document.getElementById("admin-modal");
    const adminModalMessage = document.getElementById("admin-modal-message");
    const adminModalClosers = document.querySelectorAll("[data-admin-modal-close]");

    tokenInput.value = state.token;

    function openModal(message) {
        adminModalMessage.textContent = message;
        adminModal.hidden = false;
    }

    function closeModal() {
        adminModal.hidden = true;
    }

    function setMessage(element, message, kind = "") {
        element.textContent = message;
        element.className = kind ? `${element.className.split(" ")[0]} ${kind}` : element.className.split(" ")[0];
    }

    adminModalClosers.forEach((closer) => {
        closer.addEventListener("click", closeModal);
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            closeModal();
        }
    });

    function adminFetch(url, options = {}) {
        return fetch(url, {
            ...options,
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json",
                "X-ADMIN-TOKEN": state.token,
                ...(options.headers || {})
            }
        });
    }

    async function parseResponse(response) {
        if (response.status === 204) {
            return null;
        }
        const text = await response.text();
        return text ? JSON.parse(text) : null;
    }

    async function loadThemes() {
        const response = await adminFetch("/admin/themes", { method: "GET" });
        if (response.status === 401) {
            throw new Error("관리자 토큰이 올바르지 않습니다.");
        }
        const themes = await parseResponse(response);
        themesList.innerHTML = themes.map((theme) => `
            <article class="admin-item">
                <div class="theme-item">
                    <img class="theme-thumb" src="/images/theme-placeholder.svg" alt="${theme.name}">
                    <div class="item-main">
                        <h3 class="item-title">${theme.name}</h3>
                        <p class="item-subtext">${theme.content}</p>
                        <p class="item-subtext">${theme.url}</p>
                    </div>
                </div>
                <div class="item-actions">
                    <button type="button" class="danger-button" data-delete-type="theme" data-id="${theme.id}">삭제</button>
                </div>
            </article>
        `).join("");
    }

    async function loadDates() {
        const response = await adminFetch("/admin/reservation-dates", { method: "GET" });
        if (response.status === 401) {
            throw new Error("관리자 토큰이 올바르지 않습니다.");
        }
        const dates = await parseResponse(response);
        datesList.innerHTML = dates.map((date) => `
            <article class="admin-item">
                <div class="item-main">
                    <h3 class="item-title">${date.reservationDate}</h3>
                </div>
                <div class="item-actions">
                    <button type="button" class="danger-button" data-delete-type="date" data-id="${date.id}">삭제</button>
                </div>
            </article>
        `).join("");
    }

    async function loadTimes() {
        const response = await adminFetch("/admin/times", { method: "GET" });
        if (response.status === 401) {
            throw new Error("관리자 토큰이 올바르지 않습니다.");
        }
        const times = await parseResponse(response);
        timesList.innerHTML = times.map((time) => `
            <article class="admin-item">
                <div class="item-main">
                    <h3 class="item-title">${time.startAt}</h3>
                </div>
                <div class="item-actions">
                    <button type="button" class="danger-button" data-delete-type="time" data-id="${time.id}">삭제</button>
                </div>
            </article>
        `).join("");
    }

    async function loadReservations() {
        const response = await adminFetch("/admin/reservations", { method: "GET" });
        if (response.status === 401) {
            throw new Error("관리자 토큰이 올바르지 않습니다.");
        }
        const reservations = await parseResponse(response);
        reservationsList.innerHTML = reservations.map((reservation) => `
            <article class="admin-item reservation-item-admin">
                <div class="item-main">
                    <h3 class="item-title">${reservation.userName}</h3>
                    <p class="item-subtext">${reservation.theme.name}</p>
                    <p class="item-subtext">${reservation.date} · ${reservation.time.startAt}</p>
                    <p class="item-subtext">${formatReservationStatus(reservation.reservationStatus, reservation.waitingNumber)}</p>
                </div>
                <div class="item-actions">
                    ${renderReservationAction(reservation)}
                </div>
            </article>
        `).join("");
    }

    function formatReservationStatus(status, waitingNumber) {
        if (status === "CONFIRMED") {
            return "예약 확정";
        }
        if (status === "WAITING") {
            return waitingNumber ? `대기 ${waitingNumber}번` : "예약 대기";
        }
        if (status === "CANCELED") {
            return "예약 취소";
        }
        return status;
    }

    function renderReservationAction(reservation) {
        if (reservation.reservationStatus === "CANCELED") {
            return "";
        }
        return `<button type="button" class="danger-button" data-delete-type="reservation" data-id="${reservation.id}">삭제</button>`;
    }

    async function refreshAll() {
        if (!state.token) {
            return;
        }
        await Promise.all([loadThemes(), loadDates(), loadTimes(), loadReservations()]);
        bindDeleteButtons();
    }

    function bindDeleteButtons() {
        document.querySelectorAll("[data-delete-type]").forEach((button) => {
            button.addEventListener("click", async () => {
                const type = button.dataset.deleteType;
                const id = button.dataset.id;
                const endpoint = type === "theme"
                    ? `/admin/themes/${id}`
                    : type === "date"
                        ? `/admin/reservation-dates/${id}`
                        : type === "time"
                            ? `/admin/times/${id}`
                            : `/admin/reservations/${id}`;

                const confirmed = window.confirm("정말 삭제하시겠습니까?");
                if (!confirmed) {
                    return;
                }

                const response = await adminFetch(endpoint, { method: "DELETE" });
                if (response.status === 401) {
                    openModal("관리자 토큰이 올바르지 않습니다.");
                    return;
                }
                if (!response.ok) {
                    const error = await parseResponse(response);
                    openModal(error?.message || "삭제에 실패했습니다.");
                    return;
                }

                await refreshAll();
            });
        });
    }

    tokenForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        state.token = tokenInput.value.trim();
        localStorage.setItem(TOKEN_KEY, state.token);
        setMessage(tokenMessage, "토큰을 저장했습니다.", "success");
        try {
            await refreshAll();
        } catch (error) {
            setMessage(tokenMessage, error.message, "error");
        }
    });

    tabButtons.forEach((button) => {
        button.addEventListener("click", () => {
            const target = button.dataset.tabTarget;
            tabButtons.forEach((tab) => tab.classList.remove("active"));
            panels.forEach((panel) => {
                panel.hidden = panel.id !== target;
            });
            button.classList.add("active");
        });
    });

    refreshButtons.forEach((button) => {
        button.addEventListener("click", async () => {
            try {
                const target = button.dataset.refreshTarget;
                if (target === "themes") {
                    await loadThemes();
                }
                if (target === "dates") {
                    await loadDates();
                }
                if (target === "times") {
                    await loadTimes();
                }
                if (target === "reservations") {
                    await loadReservations();
                }
                bindDeleteButtons();
            } catch (error) {
                openModal(error.message);
            }
        });
    });

    themeForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const formData = new FormData(themeForm);
        const payload = {
            name: formData.get("name"),
            content: formData.get("content"),
            url: formData.get("url")
        };

        const response = await adminFetch("/admin/themes", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        const result = await parseResponse(response);
        if (!response.ok) {
            setMessage(document.getElementById("theme-form-message"), result?.message || "테마 추가에 실패했습니다.", "error");
            return;
        }
        themeForm.reset();
        setMessage(document.getElementById("theme-form-message"), "테마를 추가했습니다.", "success");
        await refreshAll();
    });

    dateForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const formData = new FormData(dateForm);
        const payload = {
            reservationDate: formData.get("reservationDate")
        };

        const response = await adminFetch("/admin/reservation-dates", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        const result = await parseResponse(response);
        if (!response.ok) {
            setMessage(document.getElementById("date-form-message"), result?.message || "날짜 추가에 실패했습니다.", "error");
            return;
        }
        dateForm.reset();
        setMessage(document.getElementById("date-form-message"), "예약 날짜를 추가했습니다.", "success");
        await refreshAll();
    });

    timeForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const formData = new FormData(timeForm);
        const payload = {
            startAt: formData.get("startAt")
        };

        const response = await adminFetch("/admin/times", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        const result = await parseResponse(response);
        if (!response.ok) {
            setMessage(document.getElementById("time-form-message"), result?.message || "시간 추가에 실패했습니다.", "error");
            return;
        }
        timeForm.reset();
        setMessage(document.getElementById("time-form-message"), "예약 시간을 추가했습니다.", "success");
        await refreshAll();
    });

    if (state.token) {
        refreshAll().catch((error) => {
            setMessage(tokenMessage, error.message, "error");
            openModal(error.message);
        });
    }
});
