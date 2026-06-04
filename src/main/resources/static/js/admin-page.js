const reservationList = document.getElementById("reservation-list");
const timeList = document.getElementById("time-list");
const themeList = document.getElementById("theme-list");

const reservationFeedback = document.getElementById("reservation-admin-feedback");
const timeFeedback = document.getElementById("time-feedback");
const themeFeedback = document.getElementById("theme-feedback");
const reservationCount = document.getElementById("reservation-count");
const timeCount = document.getElementById("time-count");
const themeCount = document.getElementById("theme-count");

const timeForm = document.getElementById("time-form");
const themeForm = document.getElementById("theme-form");

function showFeedback(element, type, message) {
    element.hidden = false;
    element.className = `feedback ${type}`;
    element.textContent = message;
}

function clearFeedback(element) {
    element.hidden = true;
    element.className = "feedback";
    element.textContent = "";
}

async function request(url, options = {}) {
    const response = await fetch(url, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers ?? {})
        },
        ...options
    });

    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "요청 처리 중 문제가 발생했습니다.");
    }

    if (response.status === 204) {
        return null;
    }

    const contentLength = response.headers.get("content-length");
    if (contentLength === "0") {
        return null;
    }

    const contentType = response.headers.get("content-type") ?? "";
    if (!contentType.includes("application/json")) {
        const text = await response.text();
        return text || null;
    }

    return response.json();
}

function formatTime(time) {
    return (time ?? "").slice(0, 5);
}

function formatStatus(status) {
    if (status === "ACTIVE") {
        return "확정";
    }
    if (status === "PENDING") {
        return "대기";
    }
    if (status === "CANCELED") {
        return "취소";
    }
    return status ?? "상태 없음";
}

function statusClass(status) {
    return `status-badge ${String(status ?? "").toLowerCase()}`;
}

function renderReservations(reservations) {
    reservationCount.textContent = String(reservations.length);

    if (reservations.length === 0) {
        reservationList.innerHTML = '<div class="empty-card">아직 등록된 예약이 없습니다.</div>';
        return;
    }

    reservationList.innerHTML = reservations.map((reservation) => `
        <article class="list-row reservation-row">
            <div>
                <div class="list-title">
                    <strong>${reservation.name}</strong>
                    <span class="${statusClass(reservation.status)}">${formatStatus(reservation.status)}</span>
                </div>
                <p>${reservation.theme.name} · ${reservation.date} · ${formatTime(reservation.time.startAt)}</p>
            </div>
            <button class="button danger" type="button" data-action="delete-reservation" data-id="${reservation.id}" data-name="${reservation.name}" data-status="${reservation.status}">
                예약 취소
            </button>
        </article>
    `).join("");
}

function renderTimes(times) {
    timeCount.textContent = String(times.length);

    if (times.length === 0) {
        timeList.innerHTML = '<div class="empty-card">등록된 예약 시간이 없습니다.</div>';
        return;
    }

    timeList.innerHTML = times.map((time) => `
        <article class="list-row">
            <div>
                <strong>${formatTime(time.startAt)}</strong>
            </div>
            <button class="button danger" type="button" data-action="delete-time" data-id="${time.id}">
                삭제
            </button>
        </article>
    `).join("");
}

function renderThemes(themes) {
    themeCount.textContent = String(themes.length);

    if (themes.length === 0) {
        themeList.innerHTML = '<div class="empty-card">등록된 테마가 없습니다.</div>';
        return;
    }

    themeList.innerHTML = themes.map((theme) => `
        <article class="theme-card admin">
            <img src="${theme.thumbnailImageUrl}" alt="${theme.name}" loading="lazy">
            <div class="theme-card-body">
                <div class="theme-card-head">
                    <strong>${theme.name}</strong>
                    <span>${formatTime(theme.durationTime)}</span>
                </div>
                <p>${theme.description}</p>
                <button class="button danger" type="button" data-action="delete-theme" data-id="${theme.id}">
                    테마 삭제
                </button>
            </div>
        </article>
    `).join("");
}

async function refreshReservations() {
    const reservations = await request("/admin/reservations", { method: "GET" });
    renderReservations(reservations);
}

async function refreshTimes() {
    const times = await request("/times", { method: "GET" });
    renderTimes(times);
}

async function refreshThemes() {
    const themes = await request("/themes", { method: "GET" });
    renderThemes(themes);
}

timeForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearFeedback(timeFeedback);

    const payload = {
        startAt: document.getElementById("time-start-at").value
    };

    try {
        await request("/admin/times", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        timeForm.reset();
        await refreshTimes();
        showFeedback(timeFeedback, "success", "예약 시간이 추가되었습니다.");
    } catch (error) {
        showFeedback(timeFeedback, "error", error.message);
    }
});

themeForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearFeedback(themeFeedback);

    const payload = {
        name: document.getElementById("theme-name").value.trim(),
        thumbnailImageUrl: document.getElementById("theme-thumbnail-image-url").value.trim(),
        description: document.getElementById("theme-description").value.trim(),
        durationTime: document.getElementById("theme-duration-time").value
    };

    try {
        await request("/admin/themes", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        themeForm.reset();
        await refreshThemes();
        showFeedback(themeFeedback, "success", "테마가 추가되었습니다.");
    } catch (error) {
        showFeedback(themeFeedback, "error", error.message);
    }
});

reservationList.addEventListener("click", async (event) => {
    const target = event.target.closest('[data-action="delete-reservation"]');
    if (!target) {
        return;
    }

    if (!window.confirm("이 예약을 취소할까요?")) {
        return;
    }

    clearFeedback(reservationFeedback);

    try {
        const params = new URLSearchParams({
            username: target.dataset.name,
            status: target.dataset.status
        });
        await request(`/reservations/${target.dataset.id}?${params}`, { method: "DELETE" });
        await refreshReservations();
        showFeedback(reservationFeedback, "success", "예약이 취소되었습니다.");
    } catch (error) {
        showFeedback(reservationFeedback, "error", error.message);
    }
});

timeList.addEventListener("click", async (event) => {
    const target = event.target.closest('[data-action="delete-time"]');
    if (!target) {
        return;
    }

    if (!window.confirm("이 예약 시간을 삭제할까요?")) {
        return;
    }

    clearFeedback(timeFeedback);

    try {
        await request(`/admin/times/${target.dataset.id}`, { method: "DELETE" });
        await refreshTimes();
        showFeedback(timeFeedback, "success", "예약 시간이 삭제되었습니다.");
    } catch (error) {
        showFeedback(timeFeedback, "error", error.message);
    }
});

themeList.addEventListener("click", async (event) => {
    const target = event.target.closest('[data-action="delete-theme"]');
    if (!target) {
        return;
    }

    if (!window.confirm("이 테마를 삭제할까요?")) {
        return;
    }

    clearFeedback(themeFeedback);

    try {
        await request(`/admin/themes/${target.dataset.id}`, { method: "DELETE" });
        await refreshThemes();
        showFeedback(themeFeedback, "success", "테마가 삭제되었습니다.");
    } catch (error) {
        showFeedback(themeFeedback, "error", error.message);
    }
});
