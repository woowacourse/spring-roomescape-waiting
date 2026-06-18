(function () {
    "use strict";

    const POPULAR_LIMIT = 10;
    const DATE_SCAN_DAYS = 120;
    const CHUNK = 12;

    const popularList = document.getElementById("popular-list");
    const themeGrid = document.getElementById("theme-grid");
    const step1 = document.getElementById("step-1");
    const step2 = document.getElementById("step-2");
    const step3 = document.getElementById("step-3");
    const calendarRoot = document.getElementById("calendar-root");
    const calendarLoading = document.getElementById("calendar-loading");
    const selectedThemeName = document.getElementById("selected-theme-name");
    const summaryTheme = document.getElementById("summary-theme");
    const summaryDate = document.getElementById("summary-date");
    const timeSelect = document.getElementById("time-select");
    const nameInput = document.getElementById("name-input");
    const reserveForm = document.getElementById("reserve-form");
    const reserveMessage = document.getElementById("reserve-message");
    const slotStatusHint = document.getElementById("slot-status-hint");
    const reserveSubmitBtn = document.getElementById("reserve-submit-btn");
    const btnBackThemes = document.getElementById("btn-back-to-themes");
    const btnBackCalendar = document.getElementById("btn-back-to-calendar");
    const stepIndicators = document.querySelectorAll(".steps__item");

    const PLACEHOLDER_IMG =
        "data:image/svg+xml," +
        encodeURIComponent(
            '<svg xmlns="http://www.w3.org/2000/svg" width="200" height="120" viewBox="0 0 200 120"><rect fill="#2a3040" width="200" height="120"/><text x="100" y="62" fill="#8b93a7" font-size="12" text-anchor="middle" font-family="sans-serif">No image</text></svg>'
        );

    let state = {
        themes: [],
        selectedTheme: null,
        selectedDate: null,
        availableDates: new Set(),
        calendarMonth: new Date(),
        currentSlots: [],
    };

    function formatYmd(d) {
        const y = d.getFullYear();
        const m = String(d.getMonth() + 1).padStart(2, "0");
        const day = String(d.getDate()).padStart(2, "0");
        return `${y}-${m}-${day}`;
    }

    function setStep(n) {
        step1.classList.toggle("is-hidden", n !== 1);
        step2.classList.toggle("is-hidden", n !== 2);
        step3.classList.toggle("is-hidden", n !== 3);
        stepIndicators.forEach((el, i) => {
            el.classList.toggle("is-active", i + 1 === n);
        });
    }

    function themeImageUrl(url) {
        if (!url) return PLACEHOLDER_IMG;
        if (url.startsWith("http") || url.startsWith("data:")) return url;
        return url;
    }

    async function fetchJson(url, options) {
        const res = await fetch(url, options);
        if (!res.ok) {
            const t = await res.text();
            throw new Error(t || res.statusText);
        }
        if (res.status === 204) return null;
        return res.json();
    }

    // 백엔드: GET /times/available-times?themeId=&date=
    function availableTimesUrl(themeId, date) {
        return `/times/available-times?themeId=${themeId}&date=${date}`;
    }

    // 인기 테마 조회
    async function loadPopular() {
        try {
            const themes = await fetchJson(`/themes/popular?limit=${POPULAR_LIMIT}`);

            popularList.innerHTML = "";
            if (!themes || !themes.length) {
                popularList.innerHTML =
                    '<p class="section-desc" style="margin:0">아직 집계할 인기 테마가 없습니다.</p>';
                return;
            }
            themes.forEach((t) => {
                const div = document.createElement("div");
                div.className = "popular-card";
                const img = document.createElement("img");
                img.src = themeImageUrl(t.url);
                img.alt = "";
                img.onerror = () => {
                    img.src = PLACEHOLDER_IMG;
                };
                const cap = document.createElement("span");
                cap.textContent = t.name;
                div.append(img, cap);
                popularList.appendChild(div);
            });
        } catch (e) {
            popularList.innerHTML =
                '<p class="section-desc message--err" style="margin:0">인기 테마를 불러오지 못했습니다.</p>';
        }
    }

    // 예약용 테마 목록
    async function loadThemesForBooking() {
        themeGrid.innerHTML = '<p class="panel-hint">테마 목록을 불러오는 중…</p>';
        try {
            const apiThemes = await fetchJson(`/themes`);
            state.themes = [...(apiThemes || [])].sort((a, b) =>
                a.name.localeCompare(b.name, "ko")
            );
            themeGrid.innerHTML = "";
            if (!state.themes.length) {
                themeGrid.innerHTML = '<p class="panel-hint">표시할 테마가 없습니다.</p>';
                return;
            }
            state.themes.forEach((t) => {
                const btn = document.createElement("button");
                btn.type = "button";
                btn.className = "theme-card";
                const img = document.createElement("img");
                img.src = themeImageUrl(t.url);
                img.alt = "";
                img.onerror = () => {
                    img.src = PLACEHOLDER_IMG;
                };
                const body = document.createElement("div");
                body.className = "theme-card__body";
                const h3 = document.createElement("h3");
                h3.className = "theme-card__title";
                h3.textContent = t.name;
                const p = document.createElement("p");
                p.className = "theme-card__desc";
                p.textContent = t.description || "";
                body.append(h3, p);
                btn.append(img, body);
                btn.addEventListener("click", () => onSelectTheme(t));
                themeGrid.appendChild(btn);
            });
        } catch (e) {
            themeGrid.innerHTML = '<p class="panel-hint message--err">테마 목록을 불러오지 못했습니다.</p>';
        }
    }

    async function collectAvailableDates(themeId) {
        const start = new Date();
        start.setHours(0, 0, 0, 0);
        const dates = [];
        for (let i = 0; i < DATE_SCAN_DAYS; i++) {
            const d = new Date(start);
            d.setDate(start.getDate() + i);
            dates.push(formatYmd(d));
        }
        // 대기 신청까지 허용하므로 시간 슬롯이 하나라도 있으면(=시간 자체가 정의되어 있으면) 선택 가능한 날짜로 간주
        const available = new Set();
        for (let i = 0; i < dates.length; i += CHUNK) {
            const chunk = dates.slice(i, i + CHUNK);
            const results = await Promise.all(
                chunk.map(async (dateStr) => {
                    try {
                        const slots = await fetchJson(availableTimesUrl(themeId, dateStr));
                        return Array.isArray(slots) && slots.length > 0 ? dateStr : null;
                    } catch {
                        return null;
                    }
                })
            );
            results.forEach((d) => {
                if (d) available.add(d);
            });
        }
        return available;
    }

    async function onSelectTheme(theme) {
        state.selectedTheme = theme;
        state.selectedDate = null;
        state.availableDates = new Set();
        state.calendarMonth = new Date();
        selectedThemeName.textContent = theme.name;
        setStep(2);
        calendarRoot.innerHTML = "";
        calendarLoading.classList.remove("is-hidden");
        try {
            state.availableDates = await collectAvailableDates(theme.id);
            calendarLoading.classList.add("is-hidden");
            renderCalendar();
        } catch (e) {
            calendarLoading.classList.add("is-hidden");
            calendarRoot.innerHTML =
                '<p class="message message--err">예약 가능 날짜를 계산하지 못했습니다.</p>';
        }
    }

    function renderCalendar() {
        const y = state.calendarMonth.getFullYear();
        const m = state.calendarMonth.getMonth();
        const first = new Date(y, m, 1);
        const last = new Date(y, m + 1, 0);
        const startPad = (first.getDay() + 6) % 7;

        const header = document.createElement("div");
        header.className = "calendar__header";
        const prev = document.createElement("button");
        prev.type = "button";
        prev.className = "btn btn--ghost";
        prev.textContent = "이전";
        prev.addEventListener("click", () => {
            state.calendarMonth = new Date(y, m - 1, 1);
            renderCalendar();
        });
        const next = document.createElement("button");
        next.type = "button";
        next.className = "btn btn--ghost";
        next.textContent = "다음";
        next.addEventListener("click", () => {
            state.calendarMonth = new Date(y, m + 1, 1);
            renderCalendar();
        });
        const label = document.createElement("h3");
        label.className = "calendar__month-label";
        label.textContent = `${y}년 ${m + 1}월`;
        header.append(prev, label, next);

        const grid = document.createElement("div");
        grid.className = "calendar__grid";
        const dows = ["월", "화", "수", "목", "금", "토", "일"];
        dows.forEach((d) => {
            const c = document.createElement("div");
            c.className = "calendar__dow";
            c.textContent = d;
            grid.appendChild(c);
        });

        const today = new Date();
        today.setHours(0, 0, 0, 0);

        for (let i = 0; i < startPad; i++) {
            const empty = document.createElement("div");
            empty.className = "calendar__day is-empty";
            grid.appendChild(empty);
        }

        for (let day = 1; day <= last.getDate(); day++) {
            const cellDate = new Date(y, m, day);
            const ds = formatYmd(cellDate);
            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = "calendar__day";
            btn.textContent = String(day);
            if (formatYmd(today) === ds) btn.classList.add("is-today");

            const isPast = cellDate < today;
            const hasSlot = state.availableDates.has(ds);
            btn.disabled = isPast || !hasSlot;
            if (!btn.disabled) {
                btn.addEventListener("click", () => onSelectDate(ds));
            }
            grid.appendChild(btn);
        }

        calendarRoot.innerHTML = "";
        calendarRoot.append(header, grid);
    }

    function formatTimeLabel(startAt) {
        if (!startAt) return "";
        const parts = String(startAt).split(":");
        return `${parts[0]}:${parts[1] || "00"}`;
    }

    function updateSlotHint() {
        const currentId = Number(timeSelect.value);
        const slot = state.currentSlots.find((s) => s.id === currentId);
        if (!slot) {
            slotStatusHint.textContent = "";
            slotStatusHint.className = "slot-status-hint";
            reserveSubmitBtn.textContent = "예약 완료";
            reserveSubmitBtn.classList.remove("btn--waiting");
            return;
        }
        if (slot.status === "CONFIRMED") {
            slotStatusHint.textContent =
                "이미 다른 사용자가 예약한 시간대입니다. 신청하시면 대기 명단에 등록됩니다.";
            slotStatusHint.className = "slot-status-hint is-waiting";
            reserveSubmitBtn.textContent = "대기 신청";
            reserveSubmitBtn.classList.add("btn--waiting");
        } else {
            slotStatusHint.textContent = "선택하신 시간은 바로 예약할 수 있습니다.";
            slotStatusHint.className = "slot-status-hint is-available";
            reserveSubmitBtn.textContent = "예약 완료";
            reserveSubmitBtn.classList.remove("btn--waiting");
        }
    }

    async function onSelectDate(dateStr) {
        state.selectedDate = dateStr;
        if (!state.selectedTheme) return;
        setStep(3);
        summaryTheme.textContent = state.selectedTheme.name;
        summaryDate.textContent = dateStr;
        reserveMessage.textContent = "";
        reserveMessage.className = "message";
        nameInput.value = "";
        timeSelect.innerHTML = "";
        slotStatusHint.textContent = "";
        slotStatusHint.className = "slot-status-hint";
        reserveSubmitBtn.textContent = "예약 완료";
        reserveSubmitBtn.classList.remove("btn--waiting");

        try {
            const slots = await fetchJson(
                availableTimesUrl(state.selectedTheme.id, dateStr)
            );
            state.currentSlots = Array.isArray(slots) ? slots : [];
            if (!state.currentSlots.length) {
                timeSelect.disabled = true;
                reserveMessage.textContent =
                    "선택한 날짜에 사용 가능한 시간이 없습니다. 날짜를 다시 선택해 주세요.";
                reserveMessage.classList.add("message--err");
                return;
            }
            timeSelect.disabled = false;
            state.currentSlots.forEach((s) => {
                const opt = document.createElement("option");
                opt.value = String(s.id);
                const tag = s.status === "CONFIRMED" ? " (대기 신청)" : "";
                opt.textContent = formatTimeLabel(s.startAt) + tag;
                timeSelect.appendChild(opt);
            });
            updateSlotHint();
        } catch (e) {
            timeSelect.disabled = true;
            reserveMessage.textContent = "시간 목록을 불러오지 못했습니다.";
            reserveMessage.classList.add("message--err");
        }
    }

    timeSelect.addEventListener("change", updateSlotHint);

    btnBackThemes.addEventListener("click", () => {
        state.selectedTheme = null;
        state.selectedDate = null;
        setStep(1);
    });

    btnBackCalendar.addEventListener("click", () => {
        state.selectedDate = null;
        setStep(2);
        renderCalendar();
    });

    reserveForm.addEventListener("submit", async (ev) => {
        ev.preventDefault();
        reserveMessage.textContent = "";
        reserveMessage.className = "message";
        if (!state.selectedTheme || !state.selectedDate) return;
        const timeId = Number(timeSelect.value, 10);
        const name = nameInput.value.trim();
        if (!timeId || !name) return;
        const currentSlot = state.currentSlots.find((s) => s.id === timeId);
        const willBeWaiting = currentSlot && currentSlot.status === "CONFIRMED";

        try {
            const saved = await fetchJson(willBeWaiting ? "/waitings" : "/reservations", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({
                    name,
                    date: state.selectedDate,
                    timeId,
                    themeId: state.selectedTheme.id,
                }),
            });
            const isWaiting = (saved && saved.status === "WAITING") || willBeWaiting;
            reserveMessage.textContent = isWaiting
                ? "대기 신청이 완료되었습니다. ‘내 예약’에서 대기 순번을 확인할 수 있어요."
                : "예약이 완료되었습니다.";
            reserveMessage.classList.add("message--ok");
            loadPopular();
            state.availableDates = await collectAvailableDates(state.selectedTheme.id);
            renderCalendar();
            setStep(2);
        } catch (e) {
            reserveMessage.textContent = willBeWaiting
                ? "대기 신청에 실패했습니다. 이미 같은 슬롯에 대기 중이거나 입력값을 확인해 주세요."
                : "예약에 실패했습니다. 이미 예약된 시간이거나 입력값을 확인해 주세요.";
            reserveMessage.classList.add("message--err");
        }
    });

    loadPopular();
    loadThemesForBooking();

    // ===== 내 예약 =====
    const navBooking = document.getElementById("nav-booking");
    const navMy = document.getElementById("nav-my");
    const bookingSection = document.querySelector(".popular-section");
    const bookingSection2 = document.querySelector(".booking-section");
    const mySection = document.getElementById("my-reservation-section");

    function showBooking() {
        bookingSection.classList.remove("is-hidden");
        bookingSection2.classList.remove("is-hidden");
        mySection.classList.add("is-hidden");
        navBooking.classList.add("active");
        navMy.classList.remove("active");
    }

    function showMyReservation() {
        bookingSection.classList.add("is-hidden");
        bookingSection2.classList.add("is-hidden");
        mySection.classList.remove("is-hidden");
        navMy.classList.add("active");
        navBooking.classList.remove("active");
    }

    navBooking.addEventListener("click", (e) => {
        e.preventDefault();
        showBooking();
    });
    navMy.addEventListener("click", (e) => {
        e.preventDefault();
        showMyReservation();
    });

    // 내 예약 요소
    const mySearchForm = document.getElementById("my-search-form");
    const myNameInput = document.getElementById("my-name-input");
    const mySearchMsg = document.getElementById("my-search-msg");
    const myResultArea = document.getElementById("my-result-area");
    const myList = document.getElementById("my-list");
    const myEditPanel = document.getElementById("my-edit-panel");
    const myEditForm = document.getElementById("my-edit-form");
    const myEditDate = document.getElementById("my-edit-date");
    const myEditTime = document.getElementById("my-edit-time");
    const myEditMsg = document.getElementById("my-edit-msg");
    const myBtnEditCancel = document.getElementById("my-btn-edit-cancel");

    let myReservations = [];
    let editingReservation = null;
    let currentSearchName = "";

    function setMyMsg(el, text, ok) {
        el.textContent = text;
        el.className = "message " + (text ? (ok ? "message--ok" : "message--err") : "");
    }

    function renderMyList() {
        myList.innerHTML = "";
        if (!myReservations.length) {
            const empty = document.createElement("li");
            empty.className = "my-empty";
            empty.textContent = "조회된 예약 내역이 없습니다.";
            myList.appendChild(empty);
            return;
        }

        myReservations.forEach((r) => {
            const isWaiting = (r.order ?? 0) > 0;
            const themeName = r.themeResponse?.name ?? "—";
            const timeVal = r.timeResponse?.startAt ?? null;
            const timeText = timeVal ? String(timeVal).slice(0, 5) : "—";

            const li = document.createElement("li");
            li.className = "my-card";
            if (isWaiting) li.classList.add("my-card--waiting");

            const badge = document.createElement("div");
            badge.className = "my-card__badge " + (isWaiting ? "my-card__badge--waiting" : "my-card__badge--confirmed");
            badge.textContent = r.status ?? (isWaiting ? `대기 ${r.order ?? "?"}번` : "예약 확정");

            const grid = document.createElement("div");
            grid.className = "my-card__grid";
            grid.innerHTML = `
        <div class="my-card__field">
          <span class="my-card__label">예약자</span>
          <span class="my-card__value">${escapeHtml(r.name || "—")}</span>
        </div>
        <div class="my-card__field">
          <span class="my-card__label">테마</span>
          <span class="my-card__value">${escapeHtml(themeName)}</span>
        </div>
        <div class="my-card__field">
          <span class="my-card__label">날짜</span>
          <span class="my-card__value">${escapeHtml(r.date || "—")}</span>
        </div>
        <div class="my-card__field">
          <span class="my-card__label">시간</span>
          <span class="my-card__value">${escapeHtml(timeText)}</span>
        </div>
      `;

            const actions = document.createElement("div");
            actions.className = "my-card__actions";

            if (!isWaiting) {
                const editBtn = document.createElement("button");
                editBtn.type = "button";
                editBtn.className = "btn btn--ghost";
                editBtn.textContent = "날짜·시간 변경";
                editBtn.addEventListener("click", () => openEditPanel(r));
                actions.appendChild(editBtn);
            }

            const cancelBtn = document.createElement("button");
            cancelBtn.type = "button";
            cancelBtn.className = "btn my-btn-danger";
            cancelBtn.textContent = isWaiting ? "대기 취소" : "예약 취소";
            cancelBtn.addEventListener("click", () => onCancel(r));
            actions.appendChild(cancelBtn);

            li.append(badge, grid, actions);
            myList.appendChild(li);
        });
    }

    function escapeHtml(s) {
        return String(s)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    async function loadMyReservations(name) {
        currentSearchName = name;
        setMyMsg(mySearchMsg, "", true);
        myEditPanel.classList.add("is-hidden");
        editingReservation = null;
        try {
            const data = await fetchJson(`/reservations-mine?name=${encodeURIComponent(name)}`);
            myReservations = Array.isArray(data) ? data : (data ? [data] : []);
            myResultArea.classList.remove("is-hidden");
            renderMyList();
            if (!myReservations.length) {
                setMyMsg(mySearchMsg, "조회된 예약 내역이 없습니다.", false);
            }
        } catch (e) {
            myReservations = [];
            myResultArea.classList.add("is-hidden");
            setMyMsg(mySearchMsg, "예약 내역을 조회하지 못했습니다.", false);
        }
    }

    mySearchForm.addEventListener("submit", async (ev) => {
        ev.preventDefault();
        const name = myNameInput.value.trim();
        if (!name) return;
        await loadMyReservations(name);
    });

    async function onCancel(r) {
        const isWaiting = (r.order ?? 0) > 0;
        const confirmMsg = isWaiting
            ? "대기 신청을 취소하시겠습니까?"
            : "예약을 취소하시겠습니까?";
        if (!confirm(confirmMsg)) return;
        try {
            const endpoint = isWaiting ? `/waitings/${r.id}` : `/reservations/${r.id}`;
            await fetchJson(endpoint, {method: "DELETE"});
            setMyMsg(
                mySearchMsg,
                isWaiting ? "대기 신청이 취소되었습니다." : "예약이 취소되었습니다.",
                true
            );
            // 재조회 (대기 순번이 당겨질 수 있으므로 항상 새로 받아옴)
            if (currentSearchName) {
                await loadMyReservations(currentSearchName);
            }
        } catch (e) {
            setMyMsg(mySearchMsg, "취소에 실패했습니다.", false);
        }
    }

    async function openEditPanel(r) {
        editingReservation = r;
        myEditPanel.classList.remove("is-hidden");
        myEditDate.value = r.date || "";
        setMyMsg(myEditMsg, "", true);
        myEditTime.innerHTML = '<option value="">로딩 중…</option>';
        try {
            const times = await fetchJson("/times");
            myEditTime.innerHTML = "";
            times.forEach((t) => {
                const opt = document.createElement("option");
                opt.value = String(t.id);
                opt.textContent = String(t.startAt).slice(0, 5);
                if (t.id === r.timeResponse?.id) opt.selected = true;
                myEditTime.appendChild(opt);
            });
        } catch (e) {
            setMyMsg(myEditMsg, "시간 목록을 불러오지 못했습니다.", false);
        }
    }

    myBtnEditCancel.addEventListener("click", () => {
        myEditPanel.classList.add("is-hidden");
        editingReservation = null;
    });

    myEditForm.addEventListener("submit", async (ev) => {
        ev.preventDefault();
        if (!editingReservation) return;
        setMyMsg(myEditMsg, "", true);
        try {
            await fetchJson(`/reservations/${editingReservation.id}`, {
                method: "PATCH",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({
                    date: myEditDate.value,
                    timeId: Number(myEditTime.value),
                    themeId: editingReservation.themeResponse?.id,
                }),
            });
            myEditPanel.classList.add("is-hidden");
            editingReservation = null;
            setMyMsg(mySearchMsg, "예약이 변경되었습니다.", true);
            if (currentSearchName) {
                await loadMyReservations(currentSearchName);
            }
        } catch (e) {
            setMyMsg(myEditMsg, "변경에 실패했습니다. 입력값을 확인해 주세요.", false);
        }
    });
})();
