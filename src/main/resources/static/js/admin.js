const ERROR_MESSAGES = {
    // Common
    "COMMON_001": "입력 데이터 형식이 올바르지 않습니다. 다시 확인해주세요.",
    "COMMON_002": "지원하지 않는 요청 방식입니다.",
    "COMMON_003": "요청을 처리할 수 없습니다.",
    "COMMON_004": "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
    "COMMON_005": "입력값이 올바르지 않습니다.",
    "COMMON_006": "입력값이 유효하지 않습니다. 내용을 다시 확인해주세요.",
    "COMMON_007": "데이터 처리 중 오류가 발생하여 요청을 완료할 수 없습니다.",

    // Reservation
    "RES_001": "예약 ID가 누락되어 요청을 처리할 수 없습니다.",
    "RES_002": "예약자 이름을 입력해주세요.",
    "RES_003": "예약 날짜를 선택해주세요.",
    "RES_004": "예약 시간을 선택해주세요.",
    "RES_005": "테마를 선택해주세요.",
    "RES_006": "해당 예약 정보를 찾을 수 없습니다.",
    "RES_007": "선택하신 일정은 이미 예약이 완료되었습니다. 다른 시간을 선택해주세요.",
    "RES_008": "이미 취소된 예약은 변경하거나 다시 취소할 수 없습니다.",
    "RES_009": "본인의 예약만 취소하거나 변경할 수 있습니다.",
    "RES_010": "이미 지난 예약은 변경하거나 취소할 수 없습니다.",
    "RES_011": "과거 날짜나 시간으로는 예약할 수 없습니다.",
    "RES_012": "과거 날짜나 시간으로 일정을 변경할 수 없습니다.",
};

async function handleResponseError(response, defaultMessage, overrides = {}) {
    try {
        const errorData = await response.json();
        const errorCode = errorData.errorCode;
        const message = overrides[errorCode] || ERROR_MESSAGES[errorCode] || errorData.message || defaultMessage;
        alert(message);
    } catch (e) {
        alert(defaultMessage);
    }
}

async function authFetch(url, options = {}) {
    const token = localStorage.getItem("token");
    if (!token) {
        location.href = "/admin-login";
        return;
    }

    const headers = {
        ...options.headers,
        "Authorization": `Bearer ${token}`
    };

    const response = await fetch(url, { ...options, headers });
    if (response.status === 401 || response.status === 403) {
        alert("권한이 없습니다. 다시 로그인해주세요.");
        location.href = "/admin-login";
        return;
    }
    return response;
}

document.addEventListener("DOMContentLoaded", () => {
    if (!localStorage.getItem("token")) {
        location.href = "/admin-login";
        return;
    }
    initTabs();
    initDatePicker();
    initTimeSelectBox();

    loadPopularThemes();
    loadDates();
    loadTimes();
    loadThemes();
    loadReservations();
});

function initTabs() {
    const tabButtons = document.querySelectorAll(".tab-button");
    const panels = document.querySelectorAll(".panel");

    tabButtons.forEach(button => {
        button.addEventListener("click", () => {
            tabButtons.forEach(tab => tab.classList.remove("active"));
            panels.forEach(panel => panel.classList.remove("active"));

            button.classList.add("active");

            const panelId = button.dataset.panel;
            document.getElementById(panelId).classList.add("active");

            if (panelId === "slot-panel") {
                loadSlotSelectionData();
            }
        });
    });
}

function initDatePicker() {
    const dateInput = document.getElementById("date-input");
    const datePickerButton = document.getElementById("date-picker-button");
    const selectedDateText = document.getElementById("selected-date-text");

    datePickerButton.addEventListener("click", () => {
        if (dateInput.showPicker) {
            dateInput.showPicker();
            return;
        }

        dateInput.click();
    });

    dateInput.addEventListener("change", () => {
        if (!dateInput.value) {
            selectedDateText.textContent = "날짜를 선택하세요";
            return;
        }

        selectedDateText.textContent = dateInput.value;
    });
}

function initTimeSelectBox() {
    const hourSelect = document.getElementById("hour-select");
    const minuteSelect = document.getElementById("minute-select");

    for (let hour = 0; hour < 24; hour++) {
        const option = document.createElement("option");
        option.value = String(hour).padStart(2, "0");
        option.textContent = `${hour}시`;
        hourSelect.appendChild(option);
    }

    for (let minute = 0; minute < 60; minute += 10) {
        const option = document.createElement("option");
        option.value = String(minute).padStart(2, "0");
        option.textContent = `${minute}분`;
        minuteSelect.appendChild(option);
    }

    hourSelect.value = "12";
    minuteSelect.value = "00";
}

// 날짜 관리
async function loadDates() {
    const response = await authFetch("/admin/dates");
    if (!response || !response.ok) return;

    const dates = await response.json();

    const tbody = document.getElementById("date-table-body");
    tbody.innerHTML = "";

    dates.forEach(date => {
        const badgeClass = date.isActive ? "active" : "inactive";
        const badgeText = date.isActive ? "활성" : "비활성";
        const nextStatus = !date.isActive;
        const buttonText = date.isActive ? "비활성화" : "활성화";

        tbody.insertAdjacentHTML("beforeend", `
            <tr>
                <td>${date.id}</td>
                <td>${date.date}</td>
                <td>
                    <span class="badge ${badgeClass}">${badgeText}</span>
                </td>
                <td class="align-right">
                    <button class="status-button" type="button"
                            onclick="updateDateStatus(${date.id}, ${nextStatus})">
                        ${buttonText}
                    </button>
                </td>
            </tr>
        `);
    });
}

async function createDate() {
    const dateInput = document.getElementById("date-input");
    const selectedDateText = document.getElementById("selected-date-text");
    const date = dateInput.value;

    if (!date) {
        alert("날짜를 선택해주세요.");
        return;
    }

    const response = await authFetch("/admin/dates", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ date })
    });

    if (!response || !response.ok) {
        if (response) await handleResponseError(response, "날짜 추가에 실패했습니다.");
        return;
    }

    dateInput.value = "";
    selectedDateText.textContent = "날짜를 선택하세요";
    await loadDates();
}

async function updateDateStatus(id, isActive) {
    const message = isActive ? "해당 날짜를 활성화하시겠습니까?" : "해당 날짜를 비활성화하시겠습니까?";
    if (!confirm(message)) return;

    const response = await authFetch(`/admin/dates/${id}/status`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ isActive })
    });

    if (!response || !response.ok) {
        if (response) await handleResponseError(response, "날짜 상태 변경에 실패했습니다.");
        return;
    }
    await loadDates();
}

// 시간 관리
async function loadTimes() {
    const response = await authFetch("/admin/times");
    if (!response || !response.ok) return;

    const times = await response.json();
    const tbody = document.getElementById("time-table-body");
    tbody.innerHTML = "";

    times.forEach(time => {
        const badgeClass = time.isActive ? "active" : "inactive";
        const badgeText = time.isActive ? "활성" : "비활성";
        const nextStatus = !time.isActive;
        const buttonText = time.isActive ? "비활성화" : "활성화";

        tbody.insertAdjacentHTML("beforeend", `
            <tr>
                <td>${time.id}</td>
                <td>${formatTime(time.startAt)}</td>
                <td>
                    <span class="badge ${badgeClass}">${badgeText}</span>
                </td>
                <td class="align-right">
                    <button class="status-button" type="button"
                            onclick="updateTimeStatus(${time.id}, ${nextStatus})">
                        ${buttonText}
                    </button>
                </td>
            </tr>
        `);
    });
}

async function createTime() {
    const hour = document.getElementById("hour-select").value;
    const minute = document.getElementById("minute-select").value;
    const startAt = `${hour}:${minute}:00`;

    const response = await authFetch("/admin/times", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ startAt })
    });

    if (!response || !response.ok) {
        if (response) await handleResponseError(response, "시간 추가에 실패했습니다.");
        return;
    }
    await loadTimes();
}

async function updateTimeStatus(id, isActive) {
    const message = isActive ? "해당 시간을 활성화하시겠습니까?" : "해당 시간을 비활성화하시겠습니까?";
    if (!confirm(message)) return;

    const response = await authFetch(`/admin/times/${id}/status`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ isActive })
    });

    if (!response || !response.ok) {
        if (response) await handleResponseError(response, "시간 상태 변경에 실패했습니다.");
        return;
    }
    await loadTimes();
}

// 테마 관리
async function loadThemes() {
    const response = await authFetch("/admin/themes");
    if (!response || !response.ok) return;
    const themes = await response.json();

    const tbody = document.getElementById("theme-table-body");
    tbody.innerHTML = "";

    themes.forEach(theme => {
        const badgeClass = theme.isActive ? "active" : "inactive";
        const badgeText = theme.isActive ? "활성" : "비활성";

        tbody.insertAdjacentHTML("beforeend", `
            <tr>
                <td>${theme.id}</td>
                <td>${theme.name}</td>
                <td>${theme.amount?.toLocaleString() || 0}원</td>
                <td>
                    <span class="badge ${badgeClass}">${badgeText}</span>
                </td>
                <td class="align-right">
                    <button class="status-button" type="button"
                            onclick="toggleThemeStatus(${theme.id}, ${!theme.isActive})">
                        상태 변경
                    </button>
                </td>
            </tr>
        `);
    });
}

async function createTheme() {
    const name = document.getElementById("theme-name-input").value;
    const description = document.getElementById("theme-description-input").value;
    const thumbnailUrl = document.getElementById("theme-thumbnail-input").value;
    const amount = document.getElementById("theme-amount-input").value;

    if (!name || !description || !thumbnailUrl || !amount) {
        alert("테마 정보를 모두 입력해주세요.");
        return;
    }

    const response = await authFetch("/admin/themes", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, description, thumbnailUrl, amount: Number(amount) })
    });

    if (!response || !response.ok) {
        if (response) await handleResponseError(response, "테마 추가에 실패했습니다.");
        return;
    }

    document.getElementById("theme-name-input").value = "";
    document.getElementById("theme-description-input").value = "";
    document.getElementById("theme-thumbnail-input").value = "";
    document.getElementById("theme-amount-input").value = "";
    await loadThemes();
}

async function toggleThemeStatus(id, isActive) {
    const response = await authFetch(`/admin/themes/${id}`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ isActive })
    });

    if (!response || !response.ok) {
        if (response) await handleResponseError(response, "테마 상태 변경에 실패했습니다.");
        return;
    }
    await loadThemes();
}

// 예약 조회
let reservations = [];
let reschedulingReservation = null;
let selectedDate = null;
let selectedTime = null;

async function loadReservations() {
    const response = await authFetch("/admin/reservations");
    if (!response || !response.ok) return;
    reservations = await response.json();

    const tbody = document.getElementById("reservation-table-body");
    tbody.innerHTML = "";

    reservations.forEach(reservation => {
        const isCanceled = reservation.status === "CANCELED";
        const isPendingPayment = reservation.status === "PENDING_PAYMENT";
        
        const statusText = isPendingPayment ? "결제 대기" : reservation.status;
        const statusBadgeClass = isPendingPayment ? "inactive" : ""; // Reuse inactive for now or leave as is

        tbody.insertAdjacentHTML("beforeend", `
        <tr>
            <td>${reservation.id}</td>
            <td>${reservation.name}</td>
            <td>${reservation.date}</td>
            <td>${formatTime(reservation.time)}</td>
            <td>${reservation.themeName}</td>
            <td>${statusText}</td>
            <td class="align-right">
                <button class="reschedule-button" type="button" 
                        ${(isCanceled || isPendingPayment) ? "style='display: none;'" : ""}
                        onclick="openRescheduleModal(${reservation.id})">
                    변경
                </button>
                <button class="status-button" type="button" 
                        ${isCanceled ? "disabled" : ""}
                        onclick="cancelReservation(${reservation.slotId}, ${reservation.id})">
                    ${isCanceled ? "취소 완료" : "취소"}
                </button>
            </td>
        </tr>
    `);
    });
}

function openRescheduleModal(reservationId) {
    reschedulingReservation = reservations.find(r => r.id === reservationId);
    if (!reschedulingReservation) {
        alert("예약 정보를 찾을 수 없습니다.");
        return;
    }
    
    selectedDate = null;
    selectedTime = null;

    document.getElementById("reschedule-time-list").innerHTML = "<p style='color: #a9a9a9;'>날짜를 먼저 선택해주세요.</p>";
    document.getElementById("reschedule-modal").style.display = "block";

    loadRescheduleDates();
}

function closeRescheduleModal() {
    document.getElementById("reschedule-modal").style.display = "none";
    reschedulingReservation = null;
    selectedDate = null;
    selectedTime = null;
}

async function loadRescheduleDates() {
    const response = await authFetch("/member/dates");
    if (!response || !response.ok) return;

    const dates = await response.json();
    const dateList = document.getElementById("reschedule-date-list");
    dateList.innerHTML = "";

    dates.forEach(date => {
        const localDate = new Date(date.date);
        const month = localDate.toLocaleString("en-US", { month: "short" }).toUpperCase();
        const day = localDate.getDate();

        const button = document.createElement("button");
        button.className = "date-card";
        button.type = "button";
        button.innerHTML = `<span class="month">${month}</span><span class="day">${day}</span>`;

        button.addEventListener("click", () => {
            document.querySelectorAll("#reschedule-date-list .date-card")
                .forEach(item => item.classList.remove("selected"));
            button.classList.add("selected");
            selectedDate = date;
            loadRescheduleTimes();
        });

        dateList.appendChild(button);
    });
}

async function loadRescheduleTimes() {
    const themeId = reschedulingReservation.themeId;
    const response = await authFetch(`/member/slots/times?dateId=${selectedDate.id}&themeId=${themeId}`);
    if (!response || !response.ok) return;

    const times = await response.json();
    const timeList = document.getElementById("reschedule-time-list");
    timeList.innerHTML = "";
    selectedTime = null;

    if (times.length === 0) {
        timeList.innerHTML = `<p style="color: #b5b5b5; grid-column: 1 / -1;">예약 가능한 시간이 없습니다.</p>`;
        return;
    }

    times.forEach(time => {
        const button = document.createElement("button");
        button.className = "time-button";
        button.type = "button";
        button.textContent = formatTime(time.startAt);

        button.addEventListener("click", () => {
            document.querySelectorAll("#reschedule-time-list .time-button")
                .forEach(item => item.classList.remove("selected"));
            button.classList.add("selected");
            selectedTime = time;
        });

        timeList.appendChild(button);
    });
}

async function submitReschedule() {
    if (!selectedDate || !selectedTime) {
        alert("날짜와 시간을 모두 선택해주세요.");
        return;
    }

    const response = await authFetch(`/admin/slots/${reschedulingReservation.slotId}/reservations/${reschedulingReservation.id}/reschedule`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ newSlotId: selectedTime.slotId })
    });

    if (!response || !response.ok) {
        if (response) await handleResponseError(response, "예약 변경에 실패했습니다.");
        return;
    }

    alert("예약이 변경되었습니다.");
    closeRescheduleModal();
    await loadReservations();
}

async function cancelReservation(slotId, reservationId) {
    if (!confirm("해당 예약을 취소하시겠습니까?")) return;

    const response = await authFetch(`/admin/slots/${slotId}/reservations/${reservationId}/cancel`, { method: "PATCH" });
    if (!response || !response.ok) {
        if (response) await handleResponseError(response, "예약 취소에 실패했습니다.", {
            "COMMON_004": "취소 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
        });
        return;
    }
    await loadReservations();
}

async function loadPopularThemes() {
    const popularThemeList = document.getElementById("popular-theme-list");
    const response = await authFetch("/admin/themes/popular?top=10");
    if (!response || !response.ok) {
        popularThemeList.innerHTML = `<div class="popular-admin-empty">인기 테마를 불러오지 못했습니다.</div>`;
        return;
    }

    const themes = await response.json();
    popularThemeList.innerHTML = "";

    if (themes.length === 0) {
        popularThemeList.innerHTML = `<div class="popular-admin-empty">아직 인기 테마 데이터가 없습니다.</div>`;
        return;
    }

    themes.forEach((theme, index) => {
        const statusClass = theme.isActive ? "active" : "inactive";
        const statusText = theme.isActive ? "활성" : "비활성";

        popularThemeList.insertAdjacentHTML("beforeend", `
            <article class="popular-admin-card">
                <div class="popular-admin-rank">${index + 1}</div>
                <img src="${theme.thumbnailUrl}" alt="${theme.name}">
                <div class="popular-admin-content">
                    <div class="popular-admin-title-row">
                        <h3>${theme.name}</h3>
                        <span class="badge ${statusClass}">${statusText}</span>
                    </div>
                    <p>${theme.description}</p>
                    <div class="popular-admin-count">
                        <span>예약 횟수</span>
                        <strong>${theme.reservationCount}</strong>
                    </div>
                </div>
            </article>
        `);
    });
}

function formatTime(value) {
    if (!value) return "";
    const parts = value.split(":");
    return `${parts[0]}:${parts[1]}:${parts[2] ?? "00"}`;
}

// 슬롯 관리용 선택 상태
let slotSelectedDate = null;
let slotSelectedTime = null;
let slotSelectedTheme = null;

async function loadSlotSelectionData() {
    slotSelectedDate = null;
    slotSelectedTime = null;
    slotSelectedTheme = null;

    await Promise.all([
        loadSlotSelectableDates(),
        loadSlotSelectableTimes(),
        loadSlotSelectableThemes()
    ]);
}

async function loadSlotSelectableDates() {
    const response = await authFetch("/admin/dates");
    if (!response || !response.ok) return;
    const dates = await response.json();

    const list = document.getElementById("slot-date-list");
    list.innerHTML = "";

    dates.forEach(date => {
        const item = document.createElement("div");
        item.className = `selectable-item ${date.isActive ? "" : "disabled"}`;
        item.innerHTML = `
            <div class="item-info">
                <span class="item-title">${date.date}</span>
                <span class="item-sub">ID: ${date.id}</span>
            </div>
            <span class="badge ${date.isActive ? "active" : "inactive"}">${date.isActive ? "활성" : "비활성"}</span>
        `;

        item.onclick = () => {
            if (!date.isActive) return;
            document.querySelectorAll("#slot-date-list .selectable-item").forEach(el => el.classList.remove("selected"));
            item.classList.add("selected");
            slotSelectedDate = date;
        };

        list.appendChild(item);
    });
}

async function loadSlotSelectableTimes() {
    const response = await authFetch("/admin/times");
    if (!response || !response.ok) return;
    const times = await response.json();

    const list = document.getElementById("slot-time-list");
    list.innerHTML = "";

    times.forEach(time => {
        const item = document.createElement("div");
        item.className = `selectable-item ${time.isActive ? "" : "disabled"}`;
        item.innerHTML = `
            <div class="item-info">
                <span class="item-title">${formatTime(time.startAt)}</span>
                <span class="item-sub">ID: ${time.id}</span>
            </div>
            <span class="badge ${time.isActive ? "active" : "inactive"}">${time.isActive ? "활성" : "비활성"}</span>
        `;

        item.onclick = () => {
            if (!time.isActive) return;
            document.querySelectorAll("#slot-time-list .selectable-item").forEach(el => el.classList.remove("selected"));
            item.classList.add("selected");
            slotSelectedTime = time;
        };

        list.appendChild(item);
    });
}

async function loadSlotSelectableThemes() {
    const response = await authFetch("/admin/themes");
    if (!response || !response.ok) return;
    const themes = await response.json();

    const list = document.getElementById("slot-theme-list");
    list.innerHTML = "";

    themes.forEach(theme => {
        const item = document.createElement("div");
        item.className = `selectable-item ${theme.isActive ? "" : "disabled"}`;
        item.innerHTML = `
            <div class="item-info">
                <span class="item-title">${theme.name}</span>
                <span class="item-sub">ID: ${theme.id}</span>
            </div>
            <span class="badge ${theme.isActive ? "active" : "inactive"}">${theme.isActive ? "활성" : "비활성"}</span>
        `;

        item.onclick = () => {
            if (!theme.isActive) return;
            document.querySelectorAll("#slot-theme-list .selectable-item").forEach(el => el.classList.remove("selected"));
            item.classList.add("selected");
            slotSelectedTheme = theme;
        };

        list.appendChild(item);
    });
}

async function createSlot() {
    if (!slotSelectedDate || !slotSelectedTime || !slotSelectedTheme) {
        alert("날짜, 시간, 테마를 모두 선택해주세요.");
        return;
    }

    const response = await authFetch("/admin/slots", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            dateId: slotSelectedDate.id,
            timeId: slotSelectedTime.id,
            themeId: slotSelectedTheme.id
        })
    });

    if (!response || !response.ok) {
        if (response) await handleResponseError(response, "슬롯 생성에 실패했습니다.");
        return;
    }

    alert("슬롯이 성공적으로 생성되었습니다.");
    // 선택 상태 초기화
    document.querySelectorAll(".selectable-item").forEach(el => el.classList.remove("selected"));
    slotSelectedDate = null;
    slotSelectedTime = null;
    slotSelectedTheme = null;
}
