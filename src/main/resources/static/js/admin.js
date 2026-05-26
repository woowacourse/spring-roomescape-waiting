document.addEventListener("DOMContentLoaded", () => {
    const TOKEN_KEY = "roomescape-admin-token";
    const state = {
        token: localStorage.getItem(TOKEN_KEY) || ""
    };

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

    const ERROR_MAP = {
        "INVALID_INPUT_VALUE": "입력하신 정보가 규정된 형식에 맞지 않습니다. 입력 규칙을 확인하고 다시 입력해 주세요.",
        "INVALID_JSON_FORMAT": "데이터 처리 중 문법 오류가 발생했습니다. 잠시 후 다시 시도하거나 관리자에게 문의해 주세요.",
        "INVALID_PARAMETER_TYPE": "잘못된 접근 경로입니다. 정상적인 경로를 통해 다시 시도해 주세요.",
        "MISSING_REQUIRED_PARAMETER": "필수 요청 정보가 누락되었습니다. 누락된 항목이 없는지 확인해 주세요.",
        "MISSING_PATH_VARIABLE": "요청 경로 정보가 부족합니다. URL 주소가 정확한지 확인해 주세요.",
        "DATA_INTEGRITY_VIOLATION": "이미 등록된 데이터와 충돌하거나 제약 조건을 위반했습니다. 데이터 중복 여부를 확인해 주세요.",
        "METHOD_NOT_ALLOWED": "허용되지 않은 요청 방식입니다. 올바른 방법으로 접근해 주세요.",
        "NOT_FOUND": "요청하신 정보를 시스템에서 찾을 수 없습니다. 경로를 다시 확인해 주세요.",
        "INTERNAL_SERVER_ERROR": "서버 내부에서 알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도하거나 시스템 관리자에게 문의해 주세요.",
        "UNAUTHORIZED": "접근 권한이 없습니다. 관리자 토큰을 확인하거나 다시 로그인해 주세요.",

        "INVALID_RESERVATION_NAME": "예약자 이름이 올바르지 않습니다. 성함을 공백 없이 정확히 입력해 주세요.",
        "INVALID_RESERVATION_DATE": "예약 날짜가 선택되지 않았습니다. 방문하실 날짜를 목록에서 선택해 주세요.",
        "RESERVATION_NOT_FOUND": "조회 요청하신 예약 내역을 찾을 수 없습니다. 예약 번호를 다시 확인해 주세요.",
        "RESERVATION_CANNOT_CANCEL": "예약 취소/변경이 불가능한 상태입니다. 취소는 방문 전날 자정까지만 가능하니 예약 정책을 확인해 주세요.",
        "RESERVATION_CANNOT_UPDATE": "예약 변경이 불가능한 상태입니다. 변경은 방문 전날 자정까지만 가능하니 예약 정책을 확인해 주세요.",
        "RESERVATION_DUPLICATED": "선택하신 시간대에 이미 다른 예약이 존재합니다. 다른 시간이나 테마를 선택해 주세요.",

        "RESERVATION_DATE_DUPLICATED": "이미 시스템에 등록된 날짜입니다. 목록에 없는 새로운 날짜를 등록해 주세요.",
        "RESERVATION_TIME_DUPLICATED": "이미 시스템에 등록된 시간입니다. 목록에 없는 새로운 시간을 등록해 주세요.",
        "PAST_TIME_NOT_ALLOWED": "현재보다 이전 시간은 등록할 수 없습니다. 현재 시각 이후의 시간을 선택해 주세요.",
        "THEME_IN_USE": "현재 예약 건이 연결되어 있는 테마는 삭제할 수 없습니다. 관련 예약을 먼저 처리해 주세요.",
        "RESERVATION_DATE_IN_USE": "해당 날짜에 연결된 예약이 존재하여 삭제할 수 없습니다. 예약을 먼저 취소하거나 변경해 주세요.",
        "RESERVATION_TIME_IN_USE": "해당 시간에 연결된 예약이 존재하여 삭제할 수 없습니다. 예약을 먼저 취소하거나 변경해 주세요."
    };

    function getFriendlyErrorMessage(error, defaultMsg) {
        if (!error || !error.code) {
            return defaultMsg;
        }
        const friendlyMessage = ERROR_MAP[error.code];
        if (friendlyMessage) {
            console.error(`[Developer Message] ${error.message}\n[Action Guide] ${error.action}`);
            return friendlyMessage;
        }
        return error.message || defaultMsg;
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

    function getThemeImage() {
        return "/images/theme-placeholder.svg";
    }

    async function loadThemes() {
        const response = await adminFetch("/admin/themes", { method: "GET" });
        if (!response.ok) {
            const error = await parseResponse(response);
            throw new Error(getFriendlyErrorMessage(error, "테마 목록을 불러오지 못했습니다."));
        }
        const themes = await parseResponse(response);
        themesList.innerHTML = themes.map((theme) => `
            <article class="admin-item">
                <div class="theme-item">
                    <img class="theme-thumb" src="${getThemeImage()}" alt="${theme.name}">
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
        if (!response.ok) {
            const error = await parseResponse(response);
            throw new Error(getFriendlyErrorMessage(error, "날짜 목록을 불러오지 못했습니다."));
        }
        const dates = await parseResponse(response);
        datesList.innerHTML = dates.map((date) => `
            <article class="admin-item">
                <div class="item-main">
                    <h3 class="item-title">${date.playDay}</h3>
                </div>
                <div class="item-actions">
                    <button type="button" class="danger-button" data-delete-type="date" data-id="${date.id}">삭제</button>
                </div>
            </article>
        `).join("");
    }

    async function loadTimes() {
        const response = await adminFetch("/admin/times", { method: "GET" });
        if (!response.ok) {
            const error = await parseResponse(response);
            throw new Error(getFriendlyErrorMessage(error, "시간 목록을 불러오지 못했습니다."));
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
        if (!response.ok) {
            const error = await parseResponse(response);
            throw new Error(getFriendlyErrorMessage(error, "예약 목록을 불러오지 못했습니다."));
        }
        const reservations = await parseResponse(response);
        reservationsList.innerHTML = reservations.map((reservation) => `
            <article class="admin-item reservation-item-admin">
                <div class="item-main">
                    <h3 class="item-title">${reservation.name}</h3>
                    <p class="item-subtext">${reservation.theme.name}</p>
                    <p class="item-subtext">${reservation.date} · ${reservation.time.startAt}</p>
                </div>
                <div class="item-actions">
                    <button type="button" class="danger-button" data-delete-type="reservation" data-id="${reservation.id}">삭제</button>
                </div>
            </article>
        `).join("");
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
                    openModal(getFriendlyErrorMessage(error, "삭제에 실패했습니다."));
                    return;
                }

                await refreshAll();
            });
        });
    }

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
            setMessage(document.getElementById("theme-form-message"), getFriendlyErrorMessage(result, "테마 추가에 실패했습니다."), "error");
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
            playDay: formData.get("playDay")
        };

        const response = await adminFetch("/admin/reservation-dates", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        const result = await parseResponse(response);
        if (!response.ok) {
            setMessage(document.getElementById("date-form-message"), getFriendlyErrorMessage(result, "날짜 추가에 실패했습니다."), "error");
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
            setMessage(document.getElementById("time-form-message"), getFriendlyErrorMessage(result, "시간 추가에 실패했습니다."), "error");
            return;
        }
        timeForm.reset();
        setMessage(document.getElementById("time-form-message"), "예약 시간을 추가했습니다.", "success");
        await refreshAll();
    });

    if (state.token) {
        refreshAll().catch((error) => {
            alert(error.message);
            window.location.href = "/";
        });
    } else {
        alert("관리자 토큰이 필요합니다.");
        window.location.href = "/";
    }
});
