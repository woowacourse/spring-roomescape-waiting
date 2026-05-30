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
  const timeChips = document.getElementById("time-chips");
  const timeChipsEmpty = document.getElementById("time-chips-empty");
  const nameInput = document.getElementById("name-input");
  const reserveForm = document.getElementById("reserve-form");
  const reserveMessage = document.getElementById("reserve-message");
  const reserveSubmitBtn = reserveForm.querySelector('button[type="submit"]');
  const btnBackThemes = document.getElementById("btn-back-to-themes");
  const btnBackCalendar = document.getElementById("btn-back-to-calendar");
  const stepIndicators = document.querySelectorAll(".steps__item");

  const tabBooking = document.getElementById("tab-booking");
  const tabLookup = document.getElementById("tab-lookup");
  const panelBooking = document.getElementById("panel-booking");
  const panelLookup = document.getElementById("panel-lookup");

  const PLACEHOLDER_IMG =
      "data:image/svg+xml," +
      encodeURIComponent(
          '<svg xmlns="http://www.w3.org/2000/svg" width="200" height="120" viewBox="0 0 200 120"><rect fill="#f2f4f6" width="200" height="120"/><text x="100" y="62" fill="#8b95a1" font-size="12" text-anchor="middle" font-family="sans-serif">No image</text></svg>'
      );

  let state = {
    themes: [],
    selectedTheme: null,
    selectedDate: null,
    selectedTimeId: null,
    selectedTimeIsReserved: false,
    availableDates: new Set(),
    calendarMonth: new Date(),
    editing: null,
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
      let message = res.statusText;
      try {
        const parsed = JSON.parse(t);
        if (parsed && parsed.message) {
          message = parsed.message;
        }
      } catch (_) {
        if (t) message = t;
      }
      throw new Error(message);
    }
    if (res.status === 204) return null;
    return res.json();
  }

  function setActiveTab(tab) {
    const isBooking = tab === "booking";
    tabBooking.classList.toggle("is-active", isBooking);
    tabBooking.setAttribute("aria-selected", String(isBooking));
    tabLookup.classList.toggle("is-active", !isBooking);
    tabLookup.setAttribute("aria-selected", String(!isBooking));
    panelBooking.classList.toggle("is-hidden", !isBooking);
    panelLookup.classList.toggle("is-hidden", isBooking);
  }

  tabBooking.addEventListener("click", () => setActiveTab("booking"));
  tabLookup.addEventListener("click", () => {
    exitEditMode();
    setActiveTab("lookup");
    document.getElementById("lookup-name-input").focus();
  });

  function enterEditMode(item, userName) {
    state.editing = { id: item.id, userName };
    state.selectedTheme = {
      id: item.themeResponse.id,
      name: item.themeResponse.name,
    };
    state.selectedDate = null;
    state.availableDates = new Set();
    state.calendarMonth = new Date();
    selectedThemeName.textContent = item.themeResponse.name;
    btnBackThemes.style.display = "none";
    nameInput.value = userName;
    nameInput.readOnly = true;

    setActiveTab("booking");
    setStep(2);
    calendarRoot.innerHTML = "";
    calendarLoading.classList.remove("is-hidden");

    collectAvailableDates(item.themeResponse.id)
        .then((dates) => {
          state.availableDates = dates;
          calendarLoading.classList.add("is-hidden");
          renderCalendar();
        })
        .catch(() => {
          calendarLoading.classList.add("is-hidden");
          calendarRoot.innerHTML =
              '<p class="message message--err">예약 가능 날짜를 계산하지 못했습니다.</p>';
        });
  }

  function exitEditMode() {
    if (!state.editing) return;
    state.editing = null;
    state.selectedTheme = null;
    state.selectedDate = null;
    btnBackThemes.style.display = "";
    nameInput.readOnly = false;
    nameInput.value = "";
    setStep(1);
  }

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
                `/themes/${themeId}/schedule?date=${dateStr}`
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

  function updateSubmitButton() {
    const hasTime = state.selectedTimeId != null;
    const hasName = nameInput.value.trim().length > 0;
    reserveSubmitBtn.disabled = !hasTime || !hasName;
    if (state.editing) {
      reserveSubmitBtn.textContent = "예약 변경";
    } else {
      reserveSubmitBtn.textContent = state.selectedTimeIsReserved ? "대기 신청" : "예약 완료";
    }
  }

  function clearTimeChips() {
    timeChips.innerHTML = "";
    state.selectedTimeId = null;
    state.selectedTimeIsReserved = false;
  }

  function selectTimeChip(chip) {
    timeChips.querySelectorAll(".time-chip").forEach((c) => {
      c.classList.remove("is-active");
      c.setAttribute("aria-checked", "false");
    });
    chip.classList.add("is-active");
    chip.setAttribute("aria-checked", "true");
    state.selectedTimeId = Number(chip.dataset.timeId);
    state.selectedTimeIsReserved = chip.dataset.reserved === "true";
    updateSubmitButton();
  }

  async function onSelectDate(dateStr) {
    state.selectedDate = dateStr;
    if (!state.selectedTheme) return;
    setStep(3);
    summaryTheme.textContent = state.selectedTheme.name;
    summaryDate.textContent = dateStr;
    reserveMessage.textContent = "";
    reserveMessage.className = "message";
    if (!state.editing) {
      nameInput.value = "";
    }
    clearTimeChips();
    timeChipsEmpty.classList.add("is-hidden");
    updateSubmitButton();
    try {
      const slots = await fetchJson(
          `/themes/${state.selectedTheme.id}/schedule?date=${dateStr}`
      );
      if (!slots.length) {
        timeChipsEmpty.textContent =
            "선택한 날짜에 예약 또는 대기 가능한 시간이 없습니다. 날짜를 다시 선택해 주세요.";
        timeChipsEmpty.classList.remove("is-hidden");
        return;
      }
      slots.forEach((s) => {
        const chip = document.createElement("button");
        chip.type = "button";
        chip.className = "time-chip";
        chip.dataset.timeId = String(s.timeResponse.id);
        chip.dataset.reserved = s.isReserved ? "true" : "false";
        chip.setAttribute("role", "radio");
        chip.setAttribute("aria-checked", "false");

        const timeEl = document.createElement("span");
        timeEl.className = "time-chip__time";
        timeEl.textContent = formatTimeLabel(s.timeResponse.startAt);

        const statusEl = document.createElement("span");
        statusEl.className = "time-chip__status";
        if (state.editing && s.isReserved) {
          statusEl.textContent = "예약됨";
          chip.disabled = true;
        } else {
          statusEl.textContent = s.isReserved ? "대기 가능" : "예약 가능";
        }

        chip.append(timeEl, statusEl);
        if (!chip.disabled) {
          chip.addEventListener("click", () => selectTimeChip(chip));
        }
        timeChips.appendChild(chip);
      });
    } catch (e) {
      timeChipsEmpty.textContent = "시간 목록을 불러오지 못했습니다.";
      timeChipsEmpty.classList.remove("is-hidden");
    }
  }

  nameInput.addEventListener("input", updateSubmitButton);

  btnBackThemes.addEventListener("click", () => {
    state.selectedTheme = null;
    state.selectedDate = null;
    clearTimeChips();
    setStep(1);
  });

  btnBackCalendar.addEventListener("click", () => {
    state.selectedDate = null;
    clearTimeChips();
    setStep(2);
    renderCalendar();
  });

  reserveForm.addEventListener("submit", async (ev) => {
    ev.preventDefault();
    reserveMessage.textContent = "";
    reserveMessage.className = "message";
    if (!state.selectedTheme || !state.selectedDate) return;
    const timeId = state.selectedTimeId;
    const name = nameInput.value.trim();
    if (!timeId || !name) return;
    const isReserved = state.selectedTimeIsReserved;
    const editing = state.editing;
    try {
      if (editing) {
        await fetchJson(`/reservations/${editing.id}`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            name,
            date: state.selectedDate,
            timeId,
            themeId: state.selectedTheme.id,
          }),
        });
        const userName = editing.userName;
        exitEditMode();
        setActiveTab("lookup");
        lookupNameInput.value = userName;
        lookupMessage.textContent = "예약이 변경되었습니다.";
        lookupMessage.className = "message message--ok";
        await runLookup(userName);
        loadPopular();
        return;
      }
      if (isReserved) {
        await fetchJson("/waitings", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            name,
            date: state.selectedDate,
            timeId,
            themeId: state.selectedTheme.id,
          }),
        });
        reserveMessage.textContent = "대기 신청이 완료되었습니다.";
      } else {
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
      }
      reserveMessage.classList.add("message--ok");
      loadPopular();
      state.availableDates = await collectAvailableDates(state.selectedTheme.id);
      renderCalendar();
      setStep(2);
    } catch (e) {
      reserveMessage.textContent = e.message || (editing
          ? "예약 변경에 실패했습니다. 입력값을 확인해 주세요."
          : isReserved
              ? "대기 신청에 실패했습니다. 입력값을 확인해 주세요."
              : "예약에 실패했습니다. 이미 예약된 시간이거나 입력값을 확인해 주세요.");
      reserveMessage.classList.add("message--err");
    }
  });

  const lookupForm = document.getElementById("lookup-form");
  const lookupNameInput = document.getElementById("lookup-name-input");
  const lookupMessage = document.getElementById("lookup-message");
  const lookupItems = document.getElementById("lookup-items");

  function formatTime(startAt) {
    if (!startAt) return "";
    const parts = String(startAt).split(":");
    return `${parts[0]}:${parts[1] || "00"}`;
  }

  function isReservationPast(item) {
    if (!item.date || !item.timeResponse || !item.timeResponse.startAt) return false;
    const dateTime = new Date(`${item.date}T${item.timeResponse.startAt}`);
    return dateTime.getTime() < Date.now();
  }

  function renderReservationDetailItems(items, userName) {
    lookupItems.innerHTML = "";
    if (!items.length) {
      const empty = document.createElement("div");
      empty.className = "lookup-empty";
      empty.textContent = "예약 내역이 없습니다.";
      lookupItems.appendChild(empty);
      return;
    }

    const reservations = items.filter((i) => i.status !== "WAITING");
    const waitings = items.filter((i) => i.status === "WAITING");

    if (reservations.length) {
      lookupItems.appendChild(buildLookupGroup("예약 확정", "reserved", reservations, userName));
    }
    if (waitings.length) {
      lookupItems.appendChild(buildLookupGroup("예약 대기", "waiting", waitings, userName));
    }
  }

  function buildLookupGroup(title, kind, items, userName) {
    const section = document.createElement("section");
    section.className = `lookup-group lookup-group--${kind}`;

    const header = document.createElement("div");
    header.className = "lookup-group__header";

    const dot = document.createElement("span");
    dot.className = `lookup-group__dot lookup-group__dot--${kind}`;

    const titleEl = document.createElement("h3");
    titleEl.className = "lookup-group__title";
    titleEl.textContent = title;

    const count = document.createElement("span");
    count.className = "lookup-group__count";
    count.textContent = `${items.length}건`;

    header.append(dot, titleEl, count);

    const list = document.createElement("ul");
    list.className = "lookup-list";
    items.forEach((item) => {
      const li = document.createElement("li");
      li.className = "lookup-list__item";

      const info = document.createElement("div");
      info.className = "lookup-list__info";
      const theme = document.createElement("div");
      theme.className = "lookup-list__theme";
      theme.textContent = item.themeResponse.name;
      const meta = document.createElement("div");
      meta.className = "lookup-list__meta";
      meta.textContent = `${item.date} · ${formatTime(item.timeResponse.startAt)}`;
      info.append(theme, meta);

      const badge = document.createElement("span");
      badge.className = `lookup-list__badge lookup-list__badge--${kind}`;
      badge.textContent = kind === "waiting" ? `대기 ${item.sequence}번` : "예약 확정";

      const isPast = kind === "reserved" && isReservationPast(item);

      if (isPast) {
        const pastTag = document.createElement("span");
        pastTag.className = "lookup-list__past";
        pastTag.textContent = "지난 예약";
        li.append(info, badge, pastTag);
      } else {
        const cancelBtn = document.createElement("button");
        cancelBtn.type = "button";
        cancelBtn.className = "lookup-list__cancel";
        cancelBtn.textContent = "취소";
        cancelBtn.addEventListener("click", () => cancelLookupItem(item, userName, cancelBtn));

        if (kind === "reserved") {
          const editBtn = document.createElement("button");
          editBtn.type = "button";
          editBtn.className = "lookup-list__edit";
          editBtn.textContent = "변경";
          editBtn.addEventListener("click", () => enterEditMode(item, userName));
          li.append(info, badge, editBtn, cancelBtn);
        } else {
          li.append(info, badge, cancelBtn);
        }
      }
      list.appendChild(li);
    });

    section.append(header, list);
    return section;
  }

  async function runLookup(userName) {
    try {
      const data = await fetchJson(
          `/reservations?userName=${encodeURIComponent(userName)}`
      );
      renderReservationDetailItems(data.reservationDetailResponses || [], userName);
    } catch (e) {
      lookupItems.innerHTML = "";
      lookupMessage.textContent = "조회에 실패했습니다. 잠시 후 다시 시도해 주세요.";
      lookupMessage.classList.add("message--err");
    }
  }

  async function cancelLookupItem(item, userName, btn) {
    const isWaiting = item.status === "WAITING";
    const label = isWaiting ? "대기" : "예약";
    const confirmMsg = `${item.themeResponse.name} ${item.date} ${formatTime(item.timeResponse.startAt)} ${label}을(를) 취소하시겠습니까?`;
    if (!window.confirm(confirmMsg)) return;
    btn.disabled = true;
    try {
      if (isWaiting) {
        await fetchJson(
            `/waitings/${item.id}?name=${encodeURIComponent(userName)}`,
            { method: "DELETE" }
        );
      } else {
        await fetchJson(
            `/reservations/${item.id}?userName=${encodeURIComponent(userName)}`,
            { method: "DELETE" }
        );
      }
      lookupMessage.textContent = `${label}이(가) 취소되었습니다.`;
      lookupMessage.className = "message message--ok";
      await runLookup(userName);
      loadPopular();
    } catch (e) {
      btn.disabled = false;
      lookupMessage.textContent = `${label} 취소에 실패했습니다.`;
      lookupMessage.className = "message message--err";
    }
  }

  lookupForm.addEventListener("submit", (ev) => {
    ev.preventDefault();
    lookupMessage.textContent = "";
    lookupMessage.className = "message";
    const userName = lookupNameInput.value.trim();
    if (!userName) return;
    runLookup(userName);
  });

  loadPopular();
  loadThemesForBooking();
})();
