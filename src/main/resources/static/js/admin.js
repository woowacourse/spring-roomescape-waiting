document.addEventListener("DOMContentLoaded", () => {
    const state = {
        themes: [],
        slots: []
    };

    const themesList = document.getElementById("themes-list");
    const timesList = document.getElementById("times-list");
    const reservationsList = document.getElementById("reservations-list");
    const themeForm = document.getElementById("theme-form");
    const timeForm = document.getElementById("time-form");
    const reservationForm = document.getElementById("reservation-form");
    const reservationThemeSelect = document.getElementById("admin-reservation-theme");
    const reservationDateInput = document.getElementById("admin-reservation-date");
    const reservationSlotList = document.getElementById("admin-reservation-slot-list");
    const reservationFormMessage = document.getElementById("reservation-form-message");
    const panels = document.querySelectorAll(".admin-panel");
    const tabButtons = document.querySelectorAll("[data-tab-target]");
    const refreshButtons = document.querySelectorAll("[data-refresh-target]");
    const adminModal = document.getElementById("admin-modal");
    const adminModalMessage = document.getElementById("admin-modal-message");
    const adminModalClosers = document.querySelectorAll("[data-admin-modal-close]");
    const loginPanel = document.getElementById("login-panel");
    const loginForm = document.getElementById("login-form");
    const loginMessage = document.getElementById("login-form-message");
    const adminNav = document.getElementById("admin-nav");
    const logoutButton = document.getElementById("logout-button");

    reservationDateInput.value = localDateString(new Date());

    function localDateString(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return `${year}-${month}-${day}`;
    }

    function openModal(message) {
        adminModalMessage.textContent = message;
        adminModal.hidden = false;
    }

    function closeModal() {
        adminModal.hidden = true;
    }

    function setMessage(element, message, kind = "") {
        element.textContent = message;
        element.className = "form-message";
        if (kind) {
            element.classList.add(kind);
        }
    }

    function adminFetch(url, options = {}) {
        return fetch(url, {
            ...options,
            headers: {
                Accept: "application/json",
                ...(options.body ? { "Content-Type": "application/json" } : {}),
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

    function renderThemeSelect() {
        const selectedValue = reservationThemeSelect.value;
        reservationThemeSelect.innerHTML = `
            <option value="">테마를 선택하세요</option>
            ${state.themes.map((theme) => `
                <option value="${theme.id}" ${String(theme.id) === selectedValue ? "selected" : ""}>
                    ${theme.name}
                </option>
            `).join("")}
        `;
        if (selectedValue) {
            reservationThemeSelect.value = selectedValue;
        }
    }

    function renderThemes() {
        themesList.innerHTML = state.themes.map((theme) => `
            <article class="admin-item">
                <div class="theme-item">
                    <img class="theme-thumb" src="${theme.url}" alt="${theme.name}">
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

    function renderTimes(times) {
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

    function renderReservations(reservations) {
        reservationsList.innerHTML = reservations.map((reservation) => `
            <article class="admin-item reservation-item-admin">
                <div class="item-main">
                    <h3 class="item-title">${reservation.username}</h3>
                    <p class="item-subtext">${reservation.slot.theme.name}</p>
                    <p class="item-subtext">${reservation.slot.date} · ${reservation.slot.startAt.startAt}</p>
                    <p class="item-subtext">${formatReservationStatus(reservation.status, reservation.waitingNumber)}</p>
                </div>
                <div class="item-actions">
                    <button type="button" class="danger-button" data-delete-type="reservation" data-id="${reservation.id}">삭제</button>
                </div>
            </article>
        `).join("");
    }

    function renderSlots() {
        if (!state.slots.length) {
            reservationSlotList.innerHTML = `
                <div class="empty-state">
                    <strong>선택한 조건에 가능한 슬롯이 없습니다.</strong>
                </div>
            `;
            return;
        }

        reservationSlotList.innerHTML = state.slots.map((slot, index) => `
            <label class="slot-card${index === 0 ? " selected" : ""}">
                <input type="radio" name="slotId" value="${slot.slotId}" form="reservation-form" ${index === 0 ? "checked" : ""}>
                <span class="slot-pill">${slot.waitingNumber === 0 ? "CONFIRMED" : "WAITING"}</span>
                <strong>${slot.startAt}</strong>
                <span class="slot-status">${formatReservationStatus(slot.waitingNumber === 0 ? "CONFIRMED" : "WAITING", slot.waitingNumber)}</span>
            </label>
        `).join("");
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

    async function loadThemes() {
        const response = await adminFetch("/admin/themes", { method: "GET" });
        if (response.status === 401 || response.status === 403) {
            throw new Error("로그인이 필요하거나 관리자 권한이 없습니다.");
        }
        const result = await parseResponse(response);
        state.themes = result.themes;
        renderThemes();
        renderThemeSelect();
    }

    async function loadTimes() {
        const response = await adminFetch("/admin/times", { method: "GET" });
        if (response.status === 401 || response.status === 403) {
            throw new Error("로그인이 필요하거나 관리자 권한이 없습니다.");
        }
        const result = await parseResponse(response);
        renderTimes(result.times);
    }

    async function loadReservations() {
        const response = await adminFetch("/admin/reservations", { method: "GET" });
        if (response.status === 401 || response.status === 403) {
            throw new Error("로그인이 필요하거나 관리자 권한이 없습니다.");
        }
        const result = await parseResponse(response);
        renderReservations(result.reservations);
    }

    async function loadReservationSlots() {
        if (!reservationThemeSelect.value || !reservationDateInput.value) {
            reservationSlotList.innerHTML = "";
            state.slots = [];
            return;
        }

        const response = await fetch(
            `/reservation-slots?themeId=${reservationThemeSelect.value}&date=${reservationDateInput.value}`,
            { headers: { Accept: "application/json" } }
        );
        const result = await parseResponse(response);
        if (!response.ok) {
            throw new Error(result?.message || "예약 슬롯을 불러오지 못했습니다.");
        }

        state.slots = result.reservationSlots;
        renderSlots();
    }

    function showLogin(visible) {
        if (visible) {
            loginPanel.hidden = false;
            adminNav.hidden = true;
            panels.forEach((panel) => {
                if (panel !== loginPanel) {
                    panel.hidden = true;
                }
            });
        } else {
            loginPanel.hidden = true;
            adminNav.hidden = false;
            const activeTab = document.querySelector(".admin-nav-button.active");
            const targetId = activeTab ? activeTab.dataset.tabTarget : "themes-panel";
            panels.forEach((panel) => {
                panel.hidden = panel.id !== targetId;
            });
        }
    }

    async function refreshAll() {
        try {
            await Promise.all([loadThemes(), loadTimes(), loadReservations()]);
            await loadReservationSlots().catch(() => {
                // 선택 조건이 없거나 슬롯을 불러오지 못해도 전체 새로고침은 유지한다.
            });
            bindDeleteButtons();
            showLogin(false);
        } catch (error) {
            if (error.message === "로그인이 필요하거나 관리자 권한이 없습니다.") {
                showLogin(true);
            } else {
                openModal(error.message);
            }
        }
    }

    function bindDeleteButtons() {
        document.querySelectorAll("[data-delete-type]").forEach((button) => {
            button.addEventListener("click", async () => {
                const type = button.dataset.deleteType;
                const id = button.dataset.id;
                const endpoint = type === "theme"
                    ? `/admin/themes/${id}`
                    : type === "time"
                        ? `/admin/times/${id}`
                        : `/admin/reservations/${id}`;

                if (!window.confirm("정말 삭제하시겠습니까?")) {
                    return;
                }

                 const response = await adminFetch(endpoint, { method: "DELETE" });
                if (response.status === 401 || response.status === 403) {
                    openModal("로그인이 필요하거나 관리자 권한이 없습니다.");
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

    adminModalClosers.forEach((closer) => {
        closer.addEventListener("click", closeModal);
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            closeModal();
        }
    });

    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const formData = new FormData(loginForm);
        const payload = {
            name: String(formData.get("name") || "").trim(),
            password: String(formData.get("password") || "").trim()
        };

        try {
            const response = await fetch("/login", {
                method: "POST",
                headers: { "Content-Type": "application/json", Accept: "application/json" },
                body: JSON.stringify(payload)
            });
            const result = await parseResponse(response);
            if (!response.ok) {
                throw new Error(result?.message || "로그인에 실패했습니다.");
            }
            if (result.role !== "ADMIN") {
                throw new Error("관리자 권한이 없습니다.");
            }
            setMessage(loginMessage, "로그인 성공!", "success");
            loginForm.reset();
            await refreshAll();
        } catch (error) {
            setMessage(loginMessage, error.message, "error");
        }
    });

    logoutButton.addEventListener("click", async () => {
        try {
            await fetch("/logout", { method: "DELETE" });
        } catch (e) {}
        showLogin(true);
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
                if (target === "times") {
                    await loadTimes();
                }
                if (target === "reservations") {
                    await loadReservations();
                }
                await loadReservationSlots().catch(() => {
                    // refresh 대상이 아니어도 재예약 폼은 유지한다.
                });
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
            thumbnailUrl: formData.get("url")
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

    reservationThemeSelect.addEventListener("change", async () => {
        try {
            await loadReservationSlots();
        } catch (error) {
            setMessage(reservationFormMessage, error.message, "error");
        }
    });

    reservationDateInput.addEventListener("change", async () => {
        try {
            await loadReservationSlots();
        } catch (error) {
            setMessage(reservationFormMessage, error.message, "error");
        }
    });

    reservationSlotList.addEventListener("change", (event) => {
        if (event.target.name !== "slotId") {
            return;
        }
        const cards = reservationSlotList.querySelectorAll(".slot-card");
        cards.forEach((card) => card.classList.remove("selected"));
        event.target.closest(".slot-card")?.classList.add("selected");
    });

    reservationForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const formData = new FormData(reservationForm);
        const payload = {
            username: formData.get("username"),
            slotId: Number(formData.get("slotId"))
        };

        if (!payload.slotId) {
            setMessage(reservationFormMessage, "예약할 슬롯을 선택해 주세요.", "error");
            return;
        }

        const response = await adminFetch("/admin/reservations", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        const result = await parseResponse(response);
        if (!response.ok) {
            setMessage(reservationFormMessage, result?.message || "예약 추가에 실패했습니다.", "error");
            return;
        }

        reservationForm.reset();
        reservationDateInput.value = localDateString(new Date());
        setMessage(reservationFormMessage, "예약을 추가했습니다.", "success");
        await refreshAll();
    });

    refreshAll().catch((error) => {
        console.error("Failed to load admin data: ", error.message);
    });
});
