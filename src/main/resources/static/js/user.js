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

    // Reservation Date
    "RES_DATE_001": "날짜 ID가 누락되었습니다.",
    "RES_DATE_002": "날짜를 입력해주세요.",
    "RES_DATE_003": "오늘 이전의 날짜는 예약 가능 날짜로 등록할 수 없습니다.",
    "RES_DATE_004": "등록된 날짜 정보를 찾을 수 없습니다.",
    "RES_DATE_005": "이미 등록되어 있는 날짜입니다. 목록에서 확인해주세요.",
    "RES_DATE_006": "날짜 상태 변경 중 오류가 발생했습니다.",

    // Reservation Time
    "RES_TIME_001": "시간 ID가 누락되었습니다.",
    "RES_TIME_002": "시작 시간을 입력해주세요.",
    "RES_TIME_003": "등록된 시간 정보를 찾을 수 없습니다.",
    "RES_TIME_004": "이미 등록되어 있는 시간입니다. 목록에서 확인해주세요.",
    "RES_TIME_005": "시간 상태 변경 중 오류가 발생했습니다.",

    // Theme
    "THEME_001": "테마 ID가 누락되었습니다.",
    "THEME_002": "테마 이름을 입력해주세요.",
    "THEME_003": "테마 설명을 입력해주세요.",
    "THEME_004": "테마 썸네일 URL을 입력해주세요.",
    "THEME_005": "테마 정보를 찾을 수 없습니다.",
    "THEME_006": "동일한 이름의 테마가 이미 등록되어 있습니다. 다른 이름을 사용해주세요.",
    "THEME_007": "테마 상태 변경 중 오류가 발생했습니다."
};

async function handleResponseError(response, defaultMessage) {
    try {
        const errorData = await response.json();
        const errorCode = errorData.errorCode;
        const message = ERROR_MESSAGES[errorCode] || errorData.message || defaultMessage;
        alert(message);
    } catch (e) {
        alert(defaultMessage);
    }
}

let selectedDate = null;
let selectedTheme = null;
let selectedTime = null;
let currentReservation = null;

// Toss Payments Widget Initialization
const customerKey = Math.random().toString(36).substring(2, 12);
let paymentWidget = null;

async function authFetch(url, options = {}) {
    const token = localStorage.getItem("token");
    if (!token) {
        location.href = "/";
        return;
    }

    const headers = {
        ...options.headers,
        "Authorization": `Bearer ${token}`
    };

    return fetch(url, { ...options, headers });
}

document.addEventListener("DOMContentLoaded", async () => {
    if (!localStorage.getItem("token")) {
        location.href = "/";
        return;
    }

    // Initialize Toss PaymentWidget using global tossClientKey from Thymeleaf
    if (typeof PaymentWidget !== "undefined" && typeof tossClientKey !== "undefined") {
        paymentWidget = PaymentWidget(tossClientKey, customerKey);
    }

    await loadThemes();
    await loadPopularThemes();
});

async function loadPopularThemes() {
    const popularThemeList = document.getElementById("popular-theme-list");

    const response = await authFetch("/member/themes/popular?top=10");

    if (!response.ok) {
        popularThemeList.innerHTML = `
            <div class="popular-empty-message">
                인기 테마를 불러오지 못했습니다.
            </div>
        `;
        return;
    }

    const themes = await response.json();
    popularThemeList.innerHTML = "";

    if (themes.length === 0) {
        popularThemeList.innerHTML = `
            <div class="popular-empty-message">
                아직 인기 테마 데이터가 없습니다.
            </div>
        `;
        return;
    }

    themes.forEach((theme, index) => {
        const article = document.createElement("article");
        article.className = "popular-theme-card";
        article.dataset.themeId = theme.id;

        article.innerHTML = `
            <img src="${theme.thumbnailUrl}" alt="${theme.name}">
            <div class="popular-rank-badge">${index + 1}</div>
            <div class="popular-theme-content">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <h3>${theme.name}</h3>
                    <span class="popular-theme-price">${theme.amount?.toLocaleString() || 0}원</span>
                </div>
                <p>${theme.description}</p>
            </div>
        `;

        article.addEventListener("click", () => {
            selectTheme(theme);
        });

        popularThemeList.appendChild(article);
    });
}

async function loadThemes() {
    const response = await authFetch("/member/themes");

    if (!response.ok) {
        await handleResponseError(response, "테마 목록을 불러오지 못했습니다.");
        return;
    }

    const themes = await response.json();

    const themeList = document.getElementById("theme-list");
    themeList.innerHTML = "";

    themes.forEach(theme => {
        const article = document.createElement("article");
        article.className = "theme-card";

        article.dataset.themeId = theme.id;
        article.dataset.themeName = theme.name;
        article.dataset.themeDescription = theme.description;
        article.dataset.themeThumbnailUrl = theme.thumbnailUrl;
        article.dataset.themeAmount = theme.amount;

        article.innerHTML = `
            <img src="${theme.thumbnailUrl}" alt="${theme.name}">
            <div class="theme-card-content">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <h3>${theme.name}</h3>
                    <span class="theme-price">${theme.amount?.toLocaleString() || 0}원</span>
                </div>
                <p>${theme.description}</p>
            </div>
        `;

        article.addEventListener("click", () => {
            selectTheme(theme);
        });

        themeList.appendChild(article);
    });
}

async function selectTheme(theme) {
    selectedTheme = theme;
    selectedDate = null;
    selectedTime = null;

    // 모든 테마 카드(일반 + 인기)에서 selected 제거 후 해당 ID만 추가
    document.querySelectorAll(".theme-card, .popular-theme-card").forEach(card => {
        const cardThemeId = card.dataset.themeId;
        if (Number(cardThemeId) === Number(theme.id)) {
            card.classList.add("selected");
        } else {
            card.classList.remove("selected");
        }
    });

    // 다음 섹션 표시 및 데이터 로드
    document.getElementById("date-select-section").classList.remove("hidden");
    document.getElementById("time-select-section").classList.add("hidden");
    
    await loadDatesForTheme(theme.id);

    document.getElementById("date-select-section").scrollIntoView({ behavior: "smooth" });
}

async function loadDatesForTheme(themeId) {
    const response = await authFetch(`/member/slot-dates?themeId=${themeId}`);

    if (!response.ok) {
        await handleResponseError(response, "날짜 목록을 불러오지 못했습니다.");
        return;
    }

    const dates = await response.json();
    const dateList = document.getElementById("date-list");
    dateList.innerHTML = "";

    if (dates.length === 0) {
        dateList.innerHTML = `<p style="color: #b5b5b5; padding: 20px;">선택한 테마로 예약 가능한 날짜가 없습니다.</p>`;
        return;
    }

    dates.forEach(date => {
        const localDate = new Date(date.date);
        const month = localDate.toLocaleString("en-US", { month: "short" }).toUpperCase();
        const day = localDate.getDate();

        const button = document.createElement("button");
        button.className = "date-card";
        button.type = "button";
        button.innerHTML = `
            <span class="month">${month}</span>
            <span class="day">${day}</span>
        `;

        button.addEventListener("click", () => {
            document.querySelectorAll(".date-card").forEach(item => item.classList.remove("selected"));
            button.classList.add("selected");
            selectDate(date);
        });

        dateList.appendChild(button);
    });
}

async function selectDate(date) {
    selectedDate = date;
    selectedTime = null;

    document.getElementById("time-select-section").classList.remove("hidden");
    await loadAvailableTimes();
    
    document.getElementById("time-select-section").scrollIntoView({ behavior: "smooth" });
}

async function loadAvailableTimes() {
    const response = await authFetch(`/member/slots/times?dateId=${selectedDate.id}&themeId=${selectedTheme.id}`);

    if (!response.ok) {
        await handleResponseError(response, "예약 가능 시간을 불러오지 못했습니다.");
        return;
    }

    const times = await response.json();

    const timeList = document.getElementById("time-list");
    timeList.innerHTML = "";
    selectedTime = null;

    if (times.length === 0) {
        timeList.innerHTML = `<p style="color: #b5b5b5;">예약 가능한 시간이 없습니다.</p>`;
        return;
    }

    times.forEach(time => {
        const button = document.createElement("button");
        button.className = "time-button";
        button.type = "button";
        button.textContent = formatTime(time.startAt);

        button.addEventListener("click", () => {
            document.querySelectorAll(".time-button").forEach(item => item.classList.remove("selected"));
            button.classList.add("selected");
            selectedTime = time;
        });

        timeList.appendChild(button);
    });
}

async function createReservation() {
    if (!selectedTime) {
        alert("방문 시간을 선택해주세요.");
        return;
    }

    const response = await authFetch(`/member/slots/${selectedTime.slotId}/reservations`, {
        method: "POST"
    });

    if (!response.ok) {
        await handleResponseError(response, "예약에 실패했습니다.");
        return;
    }

    const reservationData = await response.json();

    if (reservationData.status === "PENDING_PAYMENT") {
        openPaymentModal(reservationData);
    } else {
        alert("예약이 완료되었습니다.");
        location.href = "/reservation-lookup";
    }
}

function openPaymentModal(reservation) {
    currentReservation = reservation;
    const paymentModal = document.getElementById("payment-modal");
    paymentModal.style.display = "block";

    if (!paymentWidget) {
        alert("결제 위젯을 초기화할 수 없습니다. 잠시 후 다시 시도해주세요.");
        return;
    }

    paymentWidget.renderPaymentMethods("#payment-method", { value: reservation.amount });
    paymentWidget.renderAgreement("#agreement");

    const paymentButton = document.getElementById("payment-button");
    paymentButton.onclick = async () => {
        try {
            await paymentWidget.requestPayment({
                orderId: reservation.orderId,
                orderName: reservation.themeName,
                successUrl: window.location.origin + "/payments/success",
                failUrl: window.location.origin + "/payments/fail",
                customerName: reservation.name
            });
        } catch (error) {
            if (error.code === "USER_CANCEL") {
                location.href = `/payments/fail?code=${error.code}&message=${encodeURIComponent("결제를 취소했습니다.")}&orderId=${reservation.orderId}`;
            } else {
                alert(error.message);
            }
        }
    };
}

function closePaymentModal() {
    if (currentReservation) {
        location.href = `/payments/fail?code=USER_CANCEL&message=${encodeURIComponent("결제를 취소했습니다.")}&orderId=${currentReservation.orderId}`;
        return;
    }
    document.getElementById("payment-modal").style.display = "none";
}

function logout() {
    localStorage.removeItem("token");
    location.href = "/";
}

function formatTime(value) {
    if (!value) {
        return "";
    }

    const parts = value.split(":");
    return `${parts[0]}:${parts[1]}`;
}
