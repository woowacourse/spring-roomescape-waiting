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
    if (reservation.status === "RESERVED") {
        return "예약 확정";
    }
    if (reservation.status === "WAITING") {
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
        const contentType = response.headers.get("content-type") ?? "";
        if (contentType.includes("application/json")) {
            const error = await response.json();
            throw new Error(error.message || "요청 처리 중 문제가 발생했습니다.");
        }
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
let topTimes = [];

function updateTopSubmitState() {
    reservationSubmitButton.disabled = !topSelectedTimeId;
}

async function loadTopAvailableTimes() {
    clearFeedback(reservationFeedback);
    const themeId = themeSelect.value;
    const date = reservationDateInput.value;

    if (!themeId || !date) {
        topTimes = [];
        topSelectedTimeId = null;
        availabilitySummary.textContent = "날짜와 테마를 선택하면 예약 가능한 시간이 표시됩니다.";
        availableTimesContainer.className = "time-grid empty-state";
        availableTimesContainer.textContent = "아직 조회된 시간이 없습니다.";
        updateTopSubmitState();
        return;
    }

    availabilitySummary.textContent = "예약 가능 시간을 조회하는 중입니다.";
    try {
        const [allTimes, availableData] = await Promise.all([
            request("/times", { method: "GET" }),
            request(`/times/available?themeId=${themeId}&date=${date}`, { method: "GET" })
        ]);
        const availableTimeIds = new Set((availableData.times ?? []).map((time) => time.id));
        topTimes = (allTimes ?? []).map((time) => ({
            ...time,
            available: availableTimeIds.has(time.id)
        }));
        topSelectedTimeId = null;
        const availableCount = topTimes.filter((time) => time.available).length;
        const waitingCount = topTimes.length - availableCount;
        availabilitySummary.textContent = `${availableData.theme.name} 테마의 ${date} 확정 가능 ${availableCount}개 · 대기 가능 ${waitingCount}개`;

        if (topTimes.length === 0) {
            availableTimesContainer.className = "time-grid empty-state";
            availableTimesContainer.textContent = "등록된 운영 시간이 없습니다.";
        } else {
            availableTimesContainer.className = "time-grid";
            availableTimesContainer.innerHTML = "";
            topTimes.forEach((time) => {
                const button = document.createElement("button");
                button.type = "button";
                button.className = `time-chip ${time.available ? "" : "waiting"}`;
                button.innerHTML = `${formatTime(time.startAt)} <span>${time.available ? "확정" : "대기"}</span>`;
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

    const selectedTime = topTimes.find((time) => time.id === Number(topSelectedTimeId));
    if (selectedTime && !selectedTime.available) {
        const wantsWaiting = window.confirm("이미 확정된 예약이 있는 시간입니다. 대기 예약으로 신청하시겠습니까?");
        if (!wantsWaiting) {
            return;
        }
    }

    const payload = {
        name: reservationNameInput.value.trim(),
        date: reservationDateInput.value,
        timeId: Number(topSelectedTimeId),
        themeId: Number(themeSelect.value)
    };

    try {
        const reservation = await request("/reservations", { method: "POST", body: JSON.stringify(payload) });
        showFeedback(reservationFeedback, "success", reservation.status === "WAITING"
            ? "이미 예약된 시간이라 대기 예약으로 등록되었습니다."
            : "예약이 등록되었습니다.");
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
    times: [],
    selectedTimeId: null
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

                    <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                        <input type="text" class="edit-name" value="${reservation.name}" placeholder="예약자 이름" required style="flex: 1; min-width: 80px; padding: 0.5rem; border: 1px solid #cbd5e1; border-radius: 4px;">
                        <input type="date" class="edit-date" value="${reservation.date}" required style="flex: 1; min-width: 120px; padding: 0.5rem; border: 1px solid #cbd5e1; border-radius: 4px;">
                        <select class="edit-theme" required style="flex: 1; min-width: 120px; padding: 0.5rem; border: 1px solid #cbd5e1; border-radius: 4px;">
                            ${themesHtml}
                        </select>
                    </div>

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

            const loadInlineTimes = async () => {
                saveBtn.disabled = true;
                inlineEditState.selectedTimeId = null;
                const date = dateInput.value;
                const themeId = themeInput.value;

                timesContainer.innerHTML = "조회 중...";
                timesContainer.className = "edit-times time-grid empty-state";

                try {
                    const [allTimes, availableData] = await Promise.all([
                        request("/times", { method: "GET" }),
                        request(`/times/available?themeId=${themeId}&date=${date}`)
                    ]);
                    const availableTimeIds = new Set((availableData.times ?? []).map((time) => time.id));
                    inlineEditState.times = (allTimes ?? []).map((time) => {
                        const isCurrentActiveReservation = reservation.status === "RESERVED"
                            && date === reservation.date
                            && String(themeId) === String(reservation.theme.id)
                            && time.id === reservation.time.id;
                        return {
                            ...time,
                            available: availableTimeIds.has(time.id) || isCurrentActiveReservation
                        };
                    });

                    if (date === reservation.date && String(themeId) === String(reservation.theme.id)) {
                        inlineEditState.selectedTimeId = reservation.time.id;
                    }

                    renderInlineTimes();
                } catch (e) {
                    timesContainer.innerHTML = "시간 조회 실패";
                }
            };

            const renderInlineTimes = () => {
                if (inlineEditState.times.length === 0) {
                    timesContainer.innerHTML = "등록된 운영 시간이 없습니다.";
                    saveBtn.disabled = true;
                    return;
                }

                timesContainer.className = "edit-times time-grid";
                timesContainer.innerHTML = "";
                inlineEditState.times.forEach(t => {
                    const btn = document.createElement("button");
                    btn.type = "button";
                    btn.className = `time-chip ${t.available ? "" : "waiting"} ${inlineEditState.selectedTimeId === t.id ? 'selected' : ''}`;
                    btn.innerHTML = `${formatTime(t.startAt)} <span>${t.available ? "확정" : "대기"}</span>`;
                    btn.onclick = () => {
                        inlineEditState.selectedTimeId = t.id;
                        renderInlineTimes();
                    };
                    timesContainer.appendChild(btn);
                });

                saveBtn.disabled = !inlineEditState.selectedTimeId;
            };

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
                const selectedTime = inlineEditState.times.find((time) => time.id === Number(inlineEditState.selectedTimeId));
                const isUnchanged = newName === reservation.name
                    && dateInput.value === reservation.date
                    && String(themeInput.value) === String(reservation.theme.id)
                    && Number(inlineEditState.selectedTimeId) === reservation.time.id;
                if (isUnchanged) {
                    inlineEditState.reservationId = null;
                    renderMyReservations(reservations, username);
                    return;
                }
                if (selectedTime && !selectedTime.available) {
                    const wantsWaiting = window.confirm("이미 확정된 예약이 있는 시간입니다. 대기 예약으로 변경하시겠습니까?");
                    if (!wantsWaiting) {
                        return;
                    }
                }

                const payload = {
                    username: newName,
                    date: dateInput.value,
                    themeId: Number(themeInput.value),
                    timeId: inlineEditState.selectedTimeId
                };
                try {
                    const changedReservation = await request(`/reservations/${reservation.id}`, {
                        method: "PATCH",
                        body: JSON.stringify(payload)
                    });

                    showFeedback(checkFeedback, "success", changedReservation.status === "WAITING"
                        ? "이미 예약된 시간이라 대기 상태로 변경되었습니다."
                        : "예약이 확정 상태로 변경되었습니다.");
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
