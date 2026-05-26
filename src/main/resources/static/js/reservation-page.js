const reservationForm = document.getElementById("reservation-form");
const reservationNameInput = document.getElementById("reservation-name");
const reservationDateInput = document.getElementById("reservation-date");
const themeSelect = document.getElementById("theme-select");
const availableTimesContainer = document.getElementById("available-times");
const availabilitySummary = document.getElementById("availability-summary");
const reservationFeedback = document.getElementById("reservation-feedback");
const reservationSubmitButton = document.getElementById("reservation-submit");

const checkForm = document.getElementById("check-form");
const checkNameInput = document.getElementById("check-name");
const myReservationList = document.getElementById("my-reservation-list");
const checkFeedback = document.getElementById("check-feedback");

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

function formatTime(time) {
    return (time ?? "").slice(0, 5);
}

function formatStatus(reservation) {
    if (reservation.status === "ACTIVE") {
        return "예약 확정";
    }
    if (reservation.status === "PENDING") {
        return reservation.pendingOrder ? `예약 대기 ${reservation.pendingOrder}순위` : "예약 대기";
    }
    if (reservation.status === "CANCELED") {
        return "예약 취소";
    }
    return reservation.status ?? "상태 없음";
}

function statusClass(status) {
    return `status-badge ${String(status ?? "").toLowerCase()}`;
}

async function request(url, options = {}) {
    const response = await fetch(url, {
        headers: { "Content-Type": "application/json", ...(options.headers ?? {}) },
        ...options
    });
    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "요청 처리 중 문제가 발생했습니다.");
    }
    if (response.status === 204) return null;
    return response.json();
}


// ==========================================
// 1. [상단] 신규 예약 만들기 영역 (POST)
// ==========================================
let topSelectedTimeId = null;
let topAvailableTimes = [];

function updateTopSubmitState() {
    reservationSubmitButton.disabled = !topSelectedTimeId;
}

async function loadTopAvailableTimes() {
    clearFeedback(reservationFeedback);
    const themeId = themeSelect.value;
    const date = reservationDateInput.value;

    if (!themeId || !date) {
        topAvailableTimes = [];
        topSelectedTimeId = null;
        availabilitySummary.textContent = "날짜와 테마를 선택하면 예약 가능한 시간이 표시됩니다.";
        availableTimesContainer.className = "time-grid empty-state";
        availableTimesContainer.textContent = "아직 조회된 시간이 없습니다.";
        updateTopSubmitState();
        return;
    }

    availabilitySummary.textContent = "예약 가능 시간을 조회하는 중입니다.";
    try {
        const data = await request(`/times/available?themeId=${themeId}&date=${date}`, { method: "GET" });
        topAvailableTimes = data.times ?? [];
        topSelectedTimeId = null;
        availabilitySummary.textContent = `${data.theme.name} 테마의 ${date} 예약 가능 시간 ${topAvailableTimes.length}개`;

        if (topAvailableTimes.length === 0) {
            availableTimesContainer.className = "time-grid empty-state";
            availableTimesContainer.textContent = "선택한 날짜와 테마에 예약 가능한 시간이 없습니다.";
        } else {
            availableTimesContainer.className = "time-grid";
            availableTimesContainer.innerHTML = "";
            topAvailableTimes.forEach((time) => {
                const button = document.createElement("button");
                button.type = "button";
                button.className = "time-chip";
                button.textContent = formatTime(time.startAt);
                button.onclick = () => {
                    topSelectedTimeId = time.id;
                    Array.from(availableTimesContainer.children).forEach(btn => btn.classList.remove("selected"));
                    button.classList.add("selected");
                    updateTopSubmitState();
                };
                availableTimesContainer.appendChild(button);
            });
        }
        updateTopSubmitState();
    } catch (error) {
        showFeedback(reservationFeedback, "error", error.message);
    }
}

reservationForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearFeedback(reservationFeedback);

    const payload = {
        name: reservationNameInput.value.trim(),
        date: reservationDateInput.value,
        timeId: Number(topSelectedTimeId),
        themeId: Number(themeSelect.value)
    };

    try {
        await request("/reservations", { method: "POST", body: JSON.stringify(payload) });
        showFeedback(reservationFeedback, "success", "예약이 등록되었습니다.");
        reservationNameInput.value = "";
        await loadTopAvailableTimes();

        // 내 예약 목록 갱신
        if (checkNameInput.value) checkForm.dispatchEvent(new Event("submit"));
    } catch (error) {
        showFeedback(reservationFeedback, "error", error.message);
    }
});

themeSelect.addEventListener("change", loadTopAvailableTimes);
reservationDateInput.addEventListener("change", loadTopAvailableTimes);
if (themeSelect.options.length > 1 && !themeSelect.value) themeSelect.selectedIndex = 1;
loadTopAvailableTimes();


// ==========================================
// 2. [하단] 내 예약 확인 / 인라인 수정 / 취소 영역 (GET, PATCH, DELETE)
// ==========================================
let inlineEditState = {
    reservationId: null,
    mode: "active",
    times: [],
    selectedTimeId: null,
    originalData: null
};

function renderMyReservations(reservations, username) {
    if (reservations.length === 0) {
        myReservationList.innerHTML = '<div class="empty-card">조회된 예약이 없습니다.</div>';
        return;
    }

    myReservationList.innerHTML = "";

    reservations.forEach((reservation) => {
        const isEditing = inlineEditState.reservationId === reservation.id;
        const article = document.createElement("article");
        article.style = "background: white; border: 1px solid #e2e8f0; padding: 1rem; border-radius: 8px; margin-bottom: 0.5rem;";

        if (isEditing) {
            const themesHtml = Array.from(themeSelect.options)
                .filter(opt => opt.value !== "")
                .map(opt => `<option value="${opt.value}" ${opt.value == reservation.theme.id ? 'selected' : ''}>${opt.text}</option>`)
                .join("");

            article.innerHTML = `
                <div style="display: flex; flex-direction: column; gap: 0.75rem;">
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <strong style="color: #0f172a;">예약 변경</strong>
                        <span class="${statusClass(reservation.status)}">${formatStatus(reservation)}</span>
                    </div>

                    <div class="mode-toggle" role="group" aria-label="예약 변경 방식">
                        <button type="button" class="mode-btn active" data-mode="active">확정 예약으로 변경</button>
                        <button type="button" class="mode-btn" data-mode="pending">대기 예약으로 변경</button>
                    </div>

                    <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                        <input type="text" class="edit-name" value="${reservation.name}" placeholder="예약자 이름" required style="flex: 1; min-width: 80px; padding: 0.5rem; border: 1px solid #cbd5e1; border-radius: 4px;">
                        <input type="date" class="edit-date" value="${reservation.date}" required style="flex: 1; min-width: 120px; padding: 0.5rem; border: 1px solid #cbd5e1; border-radius: 4px;">
                        <select class="edit-theme" required style="flex: 1; min-width: 120px; padding: 0.5rem; border: 1px solid #cbd5e1; border-radius: 4px;">
                            ${themesHtml}
                        </select>
                    </div>

                    <div class="edit-help hint">확정 변경은 빈 시간만 선택할 수 있고, 대기 변경은 이미 확정 예약이 있는 시간에 신청할 수 있습니다.</div>
                    <div class="edit-times time-grid empty-state" style="padding: 0.5rem; background: #f8fafc; border-radius: 4px; border: 1px dashed #cbd5e1;">
                        시간을 조회 중입니다...
                    </div>
                    <div style="display: flex; gap: 0.5rem; justify-content: flex-end; margin-top: 0.5rem;">
                        <button class="button ghost cancel-edit-btn" type="button">취소</button>
                        <button class="button primary save-edit-btn" type="button" disabled>수정 완료</button>
                    </div>
                </div>
            `;

            const nameInput = article.querySelector('.edit-name');
            const dateInput = article.querySelector('.edit-date');
            const themeInput = article.querySelector('.edit-theme');
            const timesContainer = article.querySelector('.edit-times');
            const saveBtn = article.querySelector('.save-edit-btn');
            const cancelBtn = article.querySelector('.cancel-edit-btn');
            const modeButtons = Array.from(article.querySelectorAll('.mode-btn'));
            modeButtons.forEach((modeButton) => {
                modeButton.classList.toggle("active", modeButton.dataset.mode === inlineEditState.mode);
            });

            const loadInlineTimes = async () => {
                saveBtn.disabled = true;
                inlineEditState.selectedTimeId = null;
                const date = dateInput.value;
                const themeId = themeInput.value;
                const mode = inlineEditState.mode;

                timesContainer.innerHTML = "조회 중...";
                timesContainer.className = "edit-times time-grid empty-state";

                try {
                    if (mode === "active") {
                        const data = await request(`/times/available?themeId=${themeId}&date=${date}`);
                        inlineEditState.times = data.times || [];
                    } else {
                        inlineEditState.times = await request("/times", { method: "GET" });
                        if (date === reservation.date && String(themeId) === String(reservation.theme.id)) {
                            inlineEditState.selectedTimeId = reservation.time.id;
                        }
                    }

                    if (mode === "active" && date === reservation.date && String(themeId) === String(reservation.theme.id)) {
                        const hasTime = inlineEditState.times.some(t => t.id === reservation.time.id);
                        if (!hasTime) {
                            inlineEditState.times.push(reservation.time);
                            inlineEditState.times.sort((a, b) => a.startAt.localeCompare(b.startAt));
                        }
                        inlineEditState.selectedTimeId = reservation.time.id;
                    }

                    renderInlineTimes();
                } catch (e) {
                    timesContainer.innerHTML = "시간 조회 실패";
                }
            };

            const renderInlineTimes = () => {
                if (inlineEditState.times.length === 0) {
                    timesContainer.innerHTML = inlineEditState.mode === "active"
                        ? "선택한 날짜/테마에 빈 시간이 없습니다."
                        : "등록된 운영 시간이 없습니다.";
                    saveBtn.disabled = true;
                    return;
                }

                timesContainer.className = "edit-times time-grid";
                timesContainer.innerHTML = "";
                inlineEditState.times.forEach(t => {
                    const btn = document.createElement("button");
                    btn.type = "button";
                    btn.className = `time-chip ${inlineEditState.selectedTimeId === t.id ? 'selected' : ''}`;
                    btn.textContent = formatTime(t.startAt);
                    btn.onclick = () => {
                        inlineEditState.selectedTimeId = t.id;
                        renderInlineTimes();
                    };
                    timesContainer.appendChild(btn);
                });

                saveBtn.disabled = !inlineEditState.selectedTimeId;
            };

            modeButtons.forEach((button) => {
                button.onclick = () => {
                    inlineEditState.mode = button.dataset.mode;
                    modeButtons.forEach((modeButton) => modeButton.classList.toggle("active", modeButton === button));
                    loadInlineTimes();
                };
            });
            dateInput.addEventListener('change', loadInlineTimes);
            themeInput.addEventListener('change', loadInlineTimes);

            cancelBtn.onclick = () => {
                inlineEditState.reservationId = null;
                renderMyReservations(reservations, username);
            };

            saveBtn.onclick = async () => {
                const newName = nameInput.value.trim();

                if (!newName) {
                    alert("예약자 이름을 입력해주세요.");
                    nameInput.focus();
                    return;
                }

                clearFeedback(checkFeedback);
                try {
                    await request(`/reservations/${reservation.id}/${inlineEditState.mode}`, {
                        method: "PATCH",
                        body: JSON.stringify({
                            username: newName,
                            date: dateInput.value,
                            themeId: Number(themeInput.value),
                            timeId: inlineEditState.selectedTimeId
                        })
                    });

                    showFeedback(checkFeedback, "success", inlineEditState.mode === "active"
                        ? "예약이 확정 상태로 변경되었습니다."
                        : "예약이 대기 상태로 변경되었습니다.");
                    inlineEditState.reservationId = null;

                    // 이름을 변경했다면, 검색창 이름도 갱신하여 재검색 유도
                    if (username !== newName) {
                        checkNameInput.value = newName;
                    }
                    checkForm.dispatchEvent(new Event("submit"));
                    loadTopAvailableTimes();

                } catch (error) {
                    showFeedback(checkFeedback, "error", error.message);
                }
            };

            loadInlineTimes();

        } else {
            article.innerHTML = `
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <div>
                        <div style="display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap;">
                            <strong>${reservation.theme.name}</strong>
                            <span class="${statusClass(reservation.status)}">${formatStatus(reservation)}</span>
                        </div>
                        <p style="margin: 0.25rem 0 0; color: #64748b; font-size: 0.875rem;">
                            ${reservation.date} · ${formatTime(reservation.time.startAt)}
                        </p>
                    </div>
                    <div style="display: flex; gap: 0.5rem;">
                        <button class="button secondary inline-edit-btn" type="button">수정</button>
                        <button class="button danger inline-cancel-btn" type="button">취소</button>
                    </div>
                </div>
            `;

            article.querySelector('.inline-edit-btn').onclick = () => {
                inlineEditState.reservationId = reservation.id;
                inlineEditState.mode = reservation.status === "PENDING" ? "pending" : "active";
                inlineEditState.originalData = reservation;
                renderMyReservations(reservations, username);
            };

            article.querySelector('.inline-cancel-btn').onclick = async () => {
                if (!window.confirm("이 예약을 정말 취소하시겠습니까?")) return;
                clearFeedback(checkFeedback);
                try {
                    await request(`/reservations/${reservation.id}?username=${encodeURIComponent(username)}`, { method: "DELETE" });
                    showFeedback(checkFeedback, "success", "예약이 성공적으로 취소되었습니다.");
                    checkForm.dispatchEvent(new Event("submit"));
                    loadTopAvailableTimes();
                } catch (error) {
                    showFeedback(checkFeedback, "error", error.message);
                }
            };
        }

        myReservationList.appendChild(article);
    });
}

checkForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    clearFeedback(checkFeedback);
    inlineEditState.reservationId = null; // 검색 시 진행중이던 수정 모드 초기화

    const name = checkNameInput.value.trim();
    try {
        const reservations = await request(`/reservations?username=${encodeURIComponent(name)}`, { method: "GET" });
        renderMyReservations(reservations, name);
    } catch (error) {
        showFeedback(checkFeedback, "error", error.message);
    }
});
