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

  // 인기 테마 조회 (서버 데이터만 사용)[cite: 13]
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
        img.onerror = () => { img.src = PLACEHOLDER_IMG; };
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

  // 예약용 테마 목록 (서버 데이터만 사용)[cite: 13]
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
        img.onerror = () => { img.src = PLACEHOLDER_IMG; };
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

  // ... 나머지 예약 관련 함수 (collectAvailableDates, renderCalendar 등 기존 유지)[cite: 13]
  async function collectAvailableDates(themeId) {
    const start = new Date();
    start.setHours(0, 0, 0, 0);
    const dates = [];
    for (let i = 0; i < DATE_SCAN_DAYS; i++) {
      const d = new Date(start);
      d.setDate(start.getDate() + i);
      dates.push(formatYmd(d));
    }
    const available = new Set();
    for (let i = 0; i < dates.length; i += CHUNK) {
      const chunk = dates.slice(i, i + CHUNK);
      const results = await Promise.all(
          chunk.map(async (dateStr) => {
            const slots = await fetchJson(
                `/themes/${themeId}/available-time?date=${dateStr}`
            );
            return Array.isArray(slots) && slots.length > 0 ? dateStr : null;
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
    try {
      const slots = await fetchJson(
          `/themes/${state.selectedTheme.id}/available-time?date=${dateStr}`
      );
      if (!slots.length) {
        timeSelect.disabled = true;
        reserveMessage.textContent =
            "선택한 날짜에 예약 가능한 시간이 없습니다. 날짜를 다시 선택해 주세요.";
        reserveMessage.classList.add("message--err");
        return;
      }
      timeSelect.disabled = false;
      slots.forEach((s) => {
        const opt = document.createElement("option");
        opt.value = String(s.id);
        opt.textContent = formatTimeLabel(s.startAt);
        timeSelect.appendChild(opt);
      });
    } catch (e) {
      timeSelect.disabled = true;
      reserveMessage.textContent = "시간 목록을 불러오지 못했습니다.";
      reserveMessage.classList.add("message--err");
    }
  }

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
    try {
      await fetchJson("/reservations", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          name,
          date: state.selectedDate,
          timeId,
          themeId: state.selectedTheme.id,
        }),
      });
      reserveMessage.textContent = "예약이 완료되었습니다.";
      reserveMessage.classList.add("message--ok");
      loadPopular();
      state.availableDates = await collectAvailableDates(state.selectedTheme.id);
      renderCalendar();
      setStep(2);
    } catch (e) {
      reserveMessage.textContent =
          "예약에 실패했습니다. 이미 예약된 시간이거나 입력값을 확인해 주세요.";
      reserveMessage.classList.add("message--err");
    }
  });

  const lookupForm = document.getElementById("lookup-form");
  const lookupNameInput = document.getElementById("lookup-name-input");
  const lookupMessage = document.getElementById("lookup-message");
  const lookupReservations = document.getElementById("lookup-reservations");
  const lookupWaitings = document.getElementById("lookup-waitings");

  function formatTime(startAt) {
    if (!startAt) return "";
    const parts = String(startAt).split(":");
    return `${parts[0]}:${parts[1] || "00"}`;
  }

  function renderReservationItems(items) {
    lookupReservations.innerHTML = "";
    if (!items.length) {
      const empty = document.createElement("li");
      empty.className = "lookup-list__empty";
      empty.textContent = "예약 내역이 없습니다.";
      lookupReservations.appendChild(empty);
      return;
    }
    items.forEach((r) => {
      const li = document.createElement("li");
      li.className = "lookup-list__item";
      li.textContent = `${r.themeResponse.name} · ${r.date} · ${formatTime(r.timeResponse.startAt)}`;
      lookupReservations.appendChild(li);
    });
  }

  function renderWaitingItems(items) {
    lookupWaitings.innerHTML = "";
    if (!items.length) {
      const empty = document.createElement("li");
      empty.className = "lookup-list__empty";
      empty.textContent = "예약 대기 내역이 없습니다.";
      lookupWaitings.appendChild(empty);
      return;
    }
    items.forEach((w) => {
      const li = document.createElement("li");
      li.className = "lookup-list__item";
      const info = document.createElement("span");
      info.className = "lookup-list__info";
      info.textContent = `${w.themeResponse.name} · ${w.date} · ${formatTime(w.timeResponse.startAt)}`;
      const rank = document.createElement("span");
      rank.className = "lookup-list__rank";
      rank.textContent = `대기 ${w.sequence}번`;
      li.append(info, rank);
      lookupWaitings.appendChild(li);
    });
  }

  lookupForm.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    lookupMessage.textContent = "";
    lookupMessage.className = "message";
    const userName = lookupNameInput.value.trim();
    if (!userName) return;
    try {
      const data = await fetchJson(
          `/reservations?userName=${encodeURIComponent(userName)}`
      );
      renderReservationItems(data.reservationResponses || []);
      renderWaitingItems(data.waitingDetailResponses || []);
    } catch (e) {
      lookupReservations.innerHTML = "";
      lookupWaitings.innerHTML = "";
      lookupMessage.textContent = "조회에 실패했습니다. 잠시 후 다시 시도해 주세요.";
      lookupMessage.classList.add("message--err");
    }
  });

  loadPopular();
  loadThemesForBooking();
})();
