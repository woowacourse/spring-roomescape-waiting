document.addEventListener("DOMContentLoaded", () => {
    const state = {
        themes: [],
        dates: [],
        rankedThemes: [],
        times: [],
        selectedThemeId: null,
        selectedDateId: null,
        selectedTimeAvailable: null,
        updateState: {
            reservationId: null,
            themeId: null,
            dateId: null,
            timeId: null
        }
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
    const adminLoginButton = document.getElementById("admin-login-button");

    const ERROR_MAP = {
        "INVALID_INPUT_VALUE": "입력하신 정보가 규정된 형식에 맞지 않습니다. 입력 규칙을 확인하고 다시 입력해 주세요.",
        "INVALID_JSON_FORMAT": "데이터 처리 중 문법 오류가 발생했습니다. 잠시 후 다시 시도하거나 고객센터로 문의해 주세요.",
        "INVALID_PARAMETER_TYPE": "잘못된 접근 경로입니다. 정상적인 경로를 통해 다시 시도해 주세요.",
        "MISSING_REQUIRED_PARAMETER": "필수 요청 정보가 누락되었습니다. 누락된 항목이 없는지 확인해 주세요.",
        "MISSING_PATH_VARIABLE": "요청 경로 정보가 부족합니다. URL 주소가 정확한지 확인해 주세요.",
        "DATA_INTEGRITY_VIOLATION": "이미 존재하거나 처리할 수 없는 데이터 조건입니다. 입력 값을 다시 확인해 주세요.",
        "METHOD_NOT_ALLOWED": "지원하지 않는 요청 방식입니다. 올바른 방법으로 접근해 주세요.",
        "NOT_FOUND": "요청하신 정보를 찾을 수 없습니다. 경로를 다시 확인해 주세요.",
        "INTERNAL_SERVER_ERROR": "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도하거나 고객센터로 문의해 주세요.",
        "UNAUTHORIZED": "인증 권한이 없습니다. 다시 로그인해 주세요.",

        "INVALID_RESERVATION_NAME": "예약자 이름이 올바르지 않습니다. 성함을 공백 없이 정확히 입력해 주세요.",
        "INVALID_RESERVATION_DATE": "예약 날짜가 선택되지 않았습니다. 방문하실 날짜를 목록에서 선택해 주세요.",
        "RESERVATION_NOT_FOUND": "조회 요청하신 예약 내역을 찾을 수 없습니다. 예약 번호를 다시 확인해 주세요.",
        "RESERVATION_CANNOT_CANCEL": "예약 취소/변경이 불가능한 상태입니다. 취소는 방문 전날 자정까지만 가능하니 예약 정책을 확인해 주세요.",
        "RESERVATION_CANNOT_UPDATE": "예약 변경이 불가능한 상태입니다. 변경은 방문 전날 자정까지만 가능하니 예약 정책을 확인해 주세요.",
        "RESERVATION_DUPLICATED": "선택하신 시간대에 이미 다른 예약이 존재합니다. 다른 시간이나 테마를 선택해 주세요.",

        "RESERVATION_DATE_DUPLICATED": "이미 등록된 날짜입니다. 다른 날짜를 선택해 주세요.",
        "RESERVATION_TIME_DUPLICATED": "이미 등록된 시간입니다. 다른 시간을 선택해 주세요.",
        "PAST_TIME_NOT_ALLOWED": "현재보다 이전 시간은 예약할 수 없습니다. 현재 시각 이후의 시간을 선택해 주세요.",
        "THEME_IN_USE": "현재 예약이 진행 중인 테마는 변경할 수 없습니다. 관리자에게 문의해 주세요.",
        "RESERVATION_DATE_IN_USE": "예약이 존재하는 날짜는 변경할 수 없습니다. 예약을 먼저 확인해 주세요.",
        "RESERVATION_TIME_IN_USE": "예약이 존재하는 시간은 변경할 수 없습니다. 예약을 먼저 확인해 주세요.",

        "DUPLICATE_WAITING_RESERVATION": "이미 해당 슬롯에 대기 신청이 되어 있습니다.",
        "AVAILABLE_SLOT_NOT_WAITABLE": "예약 가능한 시간대에는 대기를 신청할 수 없습니다. 바로 예약해 주세요.",
        "WAITING_RESERVATION_NOT_FOUND": "해당 대기 내역을 찾을 수 없습니다."
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

    const navReservation = document.getElementById("nav-reservation");
    const navMyReservation = document.getElementById("nav-my-reservation");
    const reservationView = document.getElementById("reservation-view");
    const myReservationView = document.getElementById("my-reservation-view");

    navReservation.addEventListener("click", () => {
        navReservation.classList.add("active");
        navMyReservation.classList.remove("active");
        reservationView.hidden = false;
        myReservationView.hidden = true;
    });

    navMyReservation.addEventListener("click", () => {
        navReservation.classList.remove("active");
        navMyReservation.classList.add("active");
        reservationView.hidden = true;
        myReservationView.hidden = false;
    });

    adminLoginButton.addEventListener("click", () => {
        const token = window.prompt("관리자 토큰을 입력하세요.");
        if (token) {
            localStorage.setItem("roomescape-admin-token", token);
            window.location.href = "/admin";
        }
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

    function findTheme() {
        return state.themes.find((theme) => theme.id === state.selectedThemeId);
    }

    function findDate() {
        return state.dates.find((date) => date.id === state.selectedDateId);
    }

    function getThemeImage() {
        return "/images/theme-placeholder.svg";
    }

    function renderThemes() {
        themeList.innerHTML = state.themes.map((theme) => `
            <label class="theme-card theme-card-refined${state.selectedThemeId === theme.id ? " selected" : ""}">
                <input type="radio" name="themeId" value="${theme.id}" ${state.selectedThemeId === theme.id ? "checked" : ""}>
                <img class="theme-thumbnail" src="${getThemeImage()}" alt="${theme.name}">
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
                <span class="date-day">${formatShortDate(date.playDay)}</span>
                <span class="date-full">${formatDate(date.playDay)}</span>
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
                <img class="rank-thumbnail" src="${getThemeImage()}" alt="${theme.name}">
                <span class="rank-number">${index + 1}</span>
                <div>
                    <strong>${theme.name}</strong>
                </div>
            </li>
        `).join("");
    }

    const reservationSubmitButton = document.getElementById("reservation-submit-button");

    function updateSubmitButton() {
        if (state.selectedTimeAvailable === false) {
            reservationSubmitButton.textContent = "대기 신청";
            reservationSubmitButton.classList.remove("primary-button");
            reservationSubmitButton.classList.add("secondary-button");
        } else {
            reservationSubmitButton.textContent = "예약 확정";
            reservationSubmitButton.classList.remove("secondary-button");
            reservationSubmitButton.classList.add("primary-button");
        }
    }

    function renderTimes() {
        timeList.innerHTML = state.times.map((time) => `
            <label class="time-card time-card-refined ${time.available ? "available" : "unavailable"}">
                <input type="radio" name="timeId" value="${time.timeId}" form="reservation-form" data-available="${time.available}">
                <span class="card-pill">${time.available ? "OPEN" : "CLOSED"}</span>
                <span class="time-value">${time.startAt}</span>
                <span class="time-status">${time.available ? "예약 가능" : "대기 신청 가능"}</span>
            </label>
        `).join("");

        timeList.querySelectorAll('input[name="timeId"]').forEach((input) => {
            input.addEventListener("change", () => {
                state.selectedTimeAvailable = input.dataset.available === "true";
                updateSubmitButton();
            });
        });
    }

    async function fetchJson(url) {
        const response = await fetch(url, { headers: { Accept: "application/json" } });
        if (!response.ok) {
            const error = await response.json().catch(() => ({}));
            throw new Error(getFriendlyErrorMessage(error, `요청에 실패했습니다: ${url}`));
        }
        return response.json();
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

        try {
            state.times = await fetchJson(`/times?themeId=${state.selectedThemeId}&dateId=${state.selectedDateId}`);
            renderTimes();
            selectedThemeInput.value = String(state.selectedThemeId);
            selectedDateInput.value = String(state.selectedDateId);
            selectionSummary.textContent = `${selectedTheme.name} · ${selectedDate.playDay} 기준으로 가능한 시간입니다.`;
            reservationFormSummary.textContent = `${selectedTheme.name} · ${selectedDate.playDay}`;
            timeSection.hidden = false;
            updateStatus("가능한 시간을 불러왔습니다.");
            timeSection.scrollIntoView({ behavior: "smooth", block: "start" });
        } catch (error) {
            updateStatus(error.message);
        }
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
            memberId: Number(formData.get("memberId")),
            themeId: Number(formData.get("themeId")),
            dateId: Number(formData.get("dateId")),
            timeId: Number(formData.get("timeId"))
        };

        message.textContent = "";
        message.className = "form-message";

        const isWaiting = state.selectedTimeAvailable === false;
        const url = isWaiting ? "/waiting-reservations" : "/reservations";

        try {
            const response = await fetch(url, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                },
                body: JSON.stringify(payload)
            });

            const result = await response.json();

            if (!response.ok) {
                message.textContent = getFriendlyErrorMessage(result, isWaiting ? "대기 신청 중 문제가 발생했습니다." : "예약 요청 중 문제가 발생했습니다.");
                message.classList.add("error");
                return;
            }

            message.textContent = isWaiting
                ? `${result.name}님의 대기 신청이 완료되었습니다.`
                : `${result.name}님의 예약이 완료되었습니다.`;
            message.classList.add("success");

            window.setTimeout(() => {
                window.location.reload();
            }, 600);
        } catch (error) {
            message.textContent = "서버와 통신하지 못했습니다. 잠시 후 다시 시도해 주세요.";
            message.classList.add("error");
        }
    });

    const searchForm = document.getElementById("search-form");
    const searchResultContainer = document.getElementById("search-result-container");
    const searchMessage = document.getElementById("search-message");
    const reservationsResult = document.getElementById("reservations-result");
    const waitingResult = document.getElementById("waiting-result");
    const reservationCount = document.getElementById("reservation-count");
    const waitingCount = document.getElementById("waiting-count");
    const resultTabs = document.querySelectorAll("[data-result-tab]");

    resultTabs.forEach((tab) => {
        tab.addEventListener("click", () => {
            resultTabs.forEach((t) => t.classList.remove("active"));
            tab.classList.add("active");
            const target = tab.dataset.resultTab;
            reservationsResult.hidden = target !== "reservations-result";
            waitingResult.hidden = target !== "waiting-result";
        });
    });

    searchForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const memberId = document.getElementById("search-member-id").value;
        searchMessage.textContent = "";
        searchMessage.className = "form-message";

        try {
            const [reservations, waitingReservations] = await Promise.all([
                fetchJson(`/reservations-mine?memberId=${memberId}`),
                fetchJson(`/waiting-reservations?memberId=${memberId}`)
            ]);

            if (reservations.length === 0 && waitingReservations.length === 0) {
                searchMessage.textContent = "검색 결과가 없습니다.";
                searchResultContainer.hidden = true;
                return;
            }

            reservationsResult.innerHTML = reservations.length === 0
                ? `<p class="form-message">예약 내역이 없습니다.</p>`
                : reservations.map((res) => `
                    <article class="reservation-card">
                        <div class="reservation-card-main">
                            <img class="reservation-card-thumb" src="${getThemeImage()}" alt="${res.theme.name}">
                            <div class="reservation-card-info">
                                <span class="reservation-card-tag">예약</span>
                                <h3 class="reservation-card-title">${res.theme.name}</h3>
                                <div class="reservation-card-meta">📅 <span>${res.date}</span></div>
                                <div class="reservation-card-meta">⏰ <span>${res.time.startAt}</span></div>
                                <div class="reservation-card-meta">👤 <span>${res.name}님</span></div>
                            </div>
                        </div>
                        <div class="reservation-card-actions">
                            <button type="button" class="update-button"
                                data-update-id="${res.id}"
                                data-theme-id="${res.theme.id}"
                                data-theme-name="${res.theme.name}">정보 수정</button>
                            <button type="button" class="danger-button" data-cancel-id="${res.id}">예약 취소</button>
                        </div>
                    </article>
                `).join("");

            waitingResult.innerHTML = waitingReservations.length === 0
                ? `<p class="form-message">대기 내역이 없습니다.</p>`
                : waitingReservations.map((res) => `
                    <article class="reservation-card">
                        <div class="reservation-card-main">
                            <img class="reservation-card-thumb" src="${getThemeImage()}" alt="${res.theme.name}">
                            <div class="reservation-card-info">
                                <span class="reservation-card-tag" style="background:#f59e0b">대기 ${res.rank}순위</span>
                                <h3 class="reservation-card-title">${res.theme.name}</h3>
                                <div class="reservation-card-meta">📅 <span>${res.date}</span></div>
                                <div class="reservation-card-meta">⏰ <span>${res.time.startAt}</span></div>
                                <div class="reservation-card-meta">👤 <span>${res.name}님</span></div>
                            </div>
                        </div>
                        <div class="reservation-card-actions">
                            <button type="button" class="danger-button" data-cancel-waiting-id="${res.id}">대기 취소</button>
                        </div>
                    </article>
                `).join("");

            reservationCount.textContent = reservations.length;
            waitingCount.textContent = waitingReservations.length;

            // 탭 초기 상태: 예약 탭 활성화
            resultTabs.forEach((t) => t.classList.remove("active"));
            resultTabs[0].classList.add("active");
            reservationsResult.hidden = false;
            waitingResult.hidden = true;

            searchResultContainer.hidden = false;
            bindCancelButtons();
            bindCancelWaitingButtons();
            bindUpdateButtons();
        } catch (error) {
            searchMessage.textContent = error.message;
            searchMessage.classList.add("error");
            searchResultContainer.hidden = true;
        }
    });

    function bindCancelWaitingButtons() {
        document.querySelectorAll("[data-cancel-waiting-id]").forEach((button) => {
            button.addEventListener("click", async () => {
                const id = button.dataset.cancelWaitingId;
                if (!window.confirm("정말 대기를 취소하시겠습니까?")) {
                    return;
                }

                try {
                    const response = await fetch(`/waiting-reservations/${id}`, { method: "DELETE" });
                    if (!response.ok) {
                        const error = await response.json().catch(() => ({}));
                        alert(getFriendlyErrorMessage(error, "대기 취소에 실패했습니다."));
                        return;
                    }
                    alert("대기가 취소되었습니다.");
                    searchForm.dispatchEvent(new Event("submit"));
                } catch (error) {
                    alert("서버와 통신 중 오류가 발생했습니다.");
                }
            });
        });
    }

    function bindCancelButtons() {
        document.querySelectorAll("[data-cancel-id]").forEach((button) => {
            button.addEventListener("click", async () => {
                const id = button.dataset.cancelId;
                if (!window.confirm("정말 예약을 취소하시겠습니까?")) {
                    return;
                }

                try {
                    const response = await fetch(`/reservations/${id}`, { method: "DELETE" });
                    if (!response.ok) {
                        const error = await response.json().catch(() => ({}));
                        alert(getFriendlyErrorMessage(error, "취소에 실패했습니다."));
                        return;
                    }
                    alert("예약이 취소되었습니다.");
                    searchForm.dispatchEvent(new Event("submit"));
                } catch (error) {
                    alert("서버와 통신 중 오류가 발생했습니다.");
                }
            });
        });
    }

    const updateModal = document.getElementById("reservation-update-modal");
    const updateForm = document.getElementById("update-form");
    const updateDateList = document.getElementById("update-date-list");
    const updateTimeList = document.getElementById("update-time-list");
    const updateTimeSection = document.getElementById("update-time-section");

    function bindUpdateButtons() {
        document.querySelectorAll("[data-update-id]").forEach((button) => {
            button.addEventListener("click", () => {
                state.updateState.reservationId = button.dataset.updateId;
                state.updateState.themeId = button.dataset.themeId;
                state.updateState.dateId = null;
                state.updateState.timeId = null;

                document.getElementById("update-modal-summary").textContent = `[${button.dataset.themeName}] 테마의 예약 정보를 수정합니다.`;
                renderUpdateDates();
                updateTimeSection.hidden = true;
                updateModal.hidden = false;
                document.body.classList.add("modal-open");
            });
        });
    }

    function renderUpdateDates() {
        updateDateList.innerHTML = state.dates.map((date) => `
            <label class="date-card date-card-refined${state.updateState.dateId === date.id ? " selected" : ""}">
                <input type="radio" name="updateDateId" value="${date.id}" ${state.updateState.dateId === date.id ? "checked" : ""}>
                <span class="date-day">${formatShortDate(date.playDay)}</span>
                <span class="date-full">${formatDate(date.playDay)}</span>
            </label>
        `).join("");

        updateDateList.querySelectorAll('input[name="updateDateId"]').forEach((input) => {
            input.addEventListener("change", async () => {
                state.updateState.dateId = Number(input.value);
                renderUpdateDates();
                await loadUpdateTimes();
            });
        });
    }

    async function loadUpdateTimes() {
        try {
            const times = await fetchJson(`/times?themeId=${state.updateState.themeId}&dateId=${state.updateState.dateId}`);
            updateTimeList.innerHTML = times.map((time) => `
                <label class="time-card time-card-refined ${time.available ? "available" : "unavailable"}">
                    <input type="radio" name="updateTimeId" value="${time.timeId}" ${time.available ? "" : "disabled"} ${state.updateState.timeId === time.timeId ? "checked" : ""}>
                    <span class="card-pill">${time.available ? "OPEN" : "CLOSED"}</span>
                    <span class="time-value">${time.startAt}</span>
                    <span class="time-status">${time.available ? "예약 가능" : "마감"}</span>
                </label>
            `).join("");

            updateTimeList.querySelectorAll('input[name="updateTimeId"]').forEach((input) => {
                input.addEventListener("change", () => {
                    state.updateState.timeId = Number(input.value);
                });
            });

            updateTimeSection.hidden = false;
        } catch (error) {
            alert(error.message);
        }
    }

    updateForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (!state.updateState.dateId || !state.updateState.timeId) {
            alert("수정할 날짜와 시간을 모두 선택해 주세요.");
            return;
        }

        const payload = {
            dateId: state.updateState.dateId,
            timeId: state.updateState.timeId
        };

        try {
            const response = await fetch(`/reservations/${state.updateState.reservationId}`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            if (!response.ok) {
                const error = await response.json().catch(() => ({}));
                alert(getFriendlyErrorMessage(error, "수정에 실패했습니다."));
                return;
            }

            alert("예약이 수정되었습니다.");
            updateModal.hidden = true;
            document.body.classList.remove("modal-open");
            searchForm.dispatchEvent(new Event("submit"));
        } catch (error) {
            alert("서버와 통신 중 오류가 발생했습니다.");
        }
    });

    loadInitialData().catch((error) => {
        updateStatus(error.message);
    });
});
