const API_BASE = "";
    const DEMO_DATE = "2026-05-06";
    const PAGE = document.body.dataset.page || "user";

    const state = {
      currentView: "user",
      mode: "demo",
      themes: [],
      popularThemes: [],
      times: [],
      reservations: [],
      availableTimes: [],
      demoReservations: [
        { id: 1, name: "guest-8", date: "2026-05-05", themeId: 1, timeId: 1 },
        { id: 2, name: "guest-9", date: "2026-05-05", themeId: 1, timeId: 2 },
        { id: 3, name: "guest-10", date: "2026-05-05", themeId: 1, timeId: 3 },
        { id: 4, name: "guest-18", date: "2026-05-05", themeId: 2, timeId: 1 },
        { id: 5, name: "guest-19", date: "2026-05-05", themeId: 2, timeId: 2 },
        { id: 6, name: "guest-56", date: "2026-05-06", themeId: 11, timeId: 1 },
        { id: 7, name: "guest-57", date: "2026-05-06", themeId: 11, timeId: 2 }
      ],
      selectedThemeId: null,
      selectedTimeId: null,
      adminSelectedThemeId: null,
      adminSelectedTimeId: null,
      adminAvailableTimes: []
    };

    const demoThemes = Array.from({ length: 12 }, (_, index) => {
      const id = index + 1;
      return {
        id,
        name: `Theme ${id}`,
        description: id === 11 ? "Out of range reservations only" : id === 12 ? "No reservations" : `Popular theme rank ${id}`,
        thumbnail: ""
      };
    });

    const demoTimes = [
      { id: 1, startAt: "10:00", isAvailable: true },
      { id: 2, startAt: "12:00", isAvailable: true },
      { id: 3, startAt: "14:00", isAvailable: true },
      { id: 4, startAt: "16:00", isAvailable: true },
      { id: 5, startAt: "18:00", isAvailable: true },
      { id: 6, startAt: "20:00", isAvailable: true }
    ];

    const colors = [
      ["#0e6f70", "#192f35"],
      ["#b84f2f", "#33241f"],
      ["#2f5c9a", "#1b2738"],
      ["#746a35", "#252318"],
      ["#7b3f68", "#2e1e2a"],
      ["#2f7a50", "#182b22"]
    ];

    const $ = (selector) => document.querySelector(selector);

    function isAdminPage() {
      return PAGE === "admin";
    }

    function isUserPage() {
      return PAGE === "user";
    }

    const elements = {
      sourceStatus: $("#sourceStatus"),
      popularList: $("#popularList"),
      dateInput: $("#dateInput"),
      dateNote: $("#dateNote"),
      themeGrid: $("#themeGrid"),
      themeCount: $("#themeCount"),
      timeGrid: $("#timeGrid"),
      timeCount: $("#timeCount"),
      nameInput: $("#nameInput"),
      summaryDate: $("#summaryDate"),
      summaryTheme: $("#summaryTheme"),
      summaryTime: $("#summaryTime"),
      reserveButton: $("#reserveButton"),
      formMessage: $("#formMessage"),
      themeMetric: $("#themeMetric"),
      timeMetric: $("#timeMetric"),
      reservationMetric: $("#reservationMetric"),
      themeForm: $("#themeForm"),
      timeForm: $("#timeForm"),
      adminThemeName: $("#adminThemeName"),
      adminThemeDescription: $("#adminThemeDescription"),
      adminThemeThumbnail: $("#adminThemeThumbnail"),
      adminReservationForm: $("#adminReservationForm"),
      adminReserveName: $("#adminReserveName"),
      adminReserveDate: $("#adminReserveDate"),
      adminReserveTheme: $("#adminReserveTheme"),
      adminReserveTimeGrid: $("#adminReserveTimeGrid"),
      adminReserveSummary: $("#adminReserveSummary"),
      adminReserveButton: $("#adminReserveButton"),
      adminReserveMessage: $("#adminReserveMessage"),
      adminTimeStartAt: $("#adminTimeStartAt"),
      adminMessage: $("#adminMessage"),
      adminReservationList: $("#adminReservationList"),
      adminThemeList: $("#adminThemeList"),
      adminTimeList: $("#adminTimeList"),
      reservationCount: $("#reservationCount"),
      adminThemeCount: $("#adminThemeCount"),
      adminTimeCount: $("#adminTimeCount"),
      toast: $("#toast")
    };

    function posterFor(theme) {
      const [start, end] = colors[(theme.id - 1) % colors.length];
      const canvas = document.createElement("canvas");
      canvas.width = 520;
      canvas.height = 320;
      const context = canvas.getContext("2d");
      const gradient = context.createLinearGradient(0, 0, 520, 320);
      gradient.addColorStop(0, start);
      gradient.addColorStop(1, end);
      context.fillStyle = gradient;
      context.fillRect(0, 0, 520, 320);
      context.fillStyle = "rgba(255,255,255,0.14)";
      for (let i = 0; i < 8; i += 1) {
        context.fillRect(42 + i * 58, 52, 26, 216);
      }
      context.fillStyle = "rgba(0,0,0,0.22)";
      context.fillRect(0, 232, 520, 88);
      context.fillStyle = "#fffdf8";
      context.font = "800 42px system-ui, sans-serif";
      context.fillText(theme.name, 34, 286);
      return canvas.toDataURL("image/png");
    }

    function themeImageSource(theme) {
      const thumbnail = String(theme.thumbnail || "").trim();
      if (!thumbnail || thumbnail.includes("example.com/")) {
        return posterFor(theme);
      }
      return thumbnail;
    }

    async function getJson(path) {
      const controller = new AbortController();
      const timer = window.setTimeout(() => controller.abort(), 5000);
      const response = await fetch(`${API_BASE}${path}`, {
        headers: { Accept: "application/json" },
        signal: controller.signal
      }).finally(() => window.clearTimeout(timer));
      if (!response.ok) {
        throw new Error(await errorMessageFrom(response));
      }
      return response.json();
    }

    async function postJson(path, body) {
      const response = await fetch(`${API_BASE}${path}`, {
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
      });
      if (!response.ok) {
        throw new Error(await errorMessageFrom(response));
      }
      return response.json();
    }

    async function deleteJson(path) {
      const response = await fetch(`${API_BASE}${path}`, {
        method: "DELETE",
        headers: { Accept: "application/json" }
      });
      if (!response.ok) {
        throw new Error(await errorMessageFrom(response));
      }
    }

    async function errorMessageFrom(response) {
      const fallback = `HTTP ${response.status}`;
      const contentType = response.headers.get("content-type") || "";
      if (!contentType.includes("application/json")) {
        return fallback;
      }

      try {
        const body = await response.json();
        if (Array.isArray(body.messages) && body.messages.length > 0) {
          return body.messages.filter(Boolean).join("\n");
        }
        if (typeof body.message === "string" && body.message.trim()) {
          return body.message;
        }
        return fallback;
      } catch (error) {
        return fallback;
      }
    }

    function endpointMessageOr(error, fallback) {
      if (error instanceof Error && error.message && !/^HTTP \d+$/.test(error.message)) {
        return error.message;
      }
      return fallback;
    }

    async function getReservationListData() {
      return getJson("/admin/reservations");
    }

    function escapeHtml(value) {
      return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
    }

    function setSourceStatus() {
      elements.sourceStatus.classList.toggle("live", state.mode === "live");
      const label = state.mode === "live"
        ? "Spring 서버 연결됨"
        : state.mode === "loading"
          ? "데이터 불러오는 중"
          : "데모 데이터";
      elements.sourceStatus.querySelector("span:last-child").textContent = label;
    }

    function selectedTheme() {
      return state.themes.find((theme) => theme.id === state.selectedThemeId) || null;
    }

    function selectedTime() {
      return state.availableTimes.find((time) => time.id === state.selectedTimeId) || null;
    }

    function selectedAdminTheme() {
      return state.themes.find((theme) => theme.id === state.adminSelectedThemeId) || null;
    }

    function selectedAdminTime() {
      return state.adminAvailableTimes.find((time) => time.id === state.adminSelectedTimeId) || null;
    }

    function normalizeTime(startAt) {
      return String(startAt).slice(0, 5);
    }

    function formatDate(dateText) {
      if (!dateText) {
        return "-";
      }
      const [year, month, date] = dateText.split("-");
      return `${year}.${month}.${date}`;
    }

    function renderPopularThemes() {
      const themes = state.popularThemes.slice(0, 10);
      elements.popularList.innerHTML = "";

      if (themes.length === 0) {
        elements.popularList.innerHTML = `<div class="empty">인기 테마가 없습니다.</div>`;
        return;
      }

      themes.forEach((theme, index) => {
        const item = document.createElement("button");
        item.type = "button";
        item.className = "popular-item";
        item.innerHTML = `
          <span class="rank">${index + 1}</span>
          <span>
            <span class="popular-name">${escapeHtml(theme.name)}</span>
            <span class="popular-desc">${escapeHtml(theme.description || "")}</span>
          </span>
        `;
        item.addEventListener("click", () => {
          state.selectedThemeId = theme.id;
          state.selectedTimeId = null;
          renderThemes();
          loadAvailability();
        });
        elements.popularList.appendChild(item);
      });
    }

    function renderThemes() {
      elements.themeGrid.innerHTML = "";
      elements.themeCount.textContent = `${state.themes.length}개 테마`;

      if (state.themes.length === 0) {
        elements.themeGrid.innerHTML = `<div class="empty">등록된 테마가 없습니다.</div>`;
        return;
      }

      state.themes.forEach((theme) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = `theme-button${theme.id === state.selectedThemeId ? " selected" : ""}`;
        button.setAttribute("aria-pressed", theme.id === state.selectedThemeId ? "true" : "false");
        const imageSource = themeImageSource(theme);
        button.innerHTML = `
          <img class="theme-thumb" src="${escapeHtml(imageSource)}" alt="${escapeHtml(theme.name)} 썸네일">
          <span class="theme-body">
            <span class="theme-name">${escapeHtml(theme.name)}</span>
            <span class="theme-description">${escapeHtml(theme.description || "")}</span>
            <span class="theme-meta">60분 진행</span>
          </span>
        `;
        const img = button.querySelector("img");
        img.addEventListener("error", () => {
          img.src = posterFor(theme);
        }, { once: true });
        button.addEventListener("click", () => {
          state.selectedThemeId = theme.id;
          state.selectedTimeId = null;
          renderThemes();
          loadAvailability();
        });
        elements.themeGrid.appendChild(button);
      });
    }

    function renderTimes() {
      elements.timeGrid.innerHTML = "";
      const availableCount = state.availableTimes.filter((time) => time.isAvailable).length;
      elements.timeCount.textContent = state.selectedThemeId ? `${availableCount}개 가능` : "테마를 먼저 선택";

      if (!state.selectedThemeId) {
        elements.timeGrid.innerHTML = `<div class="empty">테마를 선택하면 시간 목록이 표시됩니다.</div>`;
        syncSummary();
        return;
      }

      if (state.availableTimes.length === 0) {
        elements.timeGrid.innerHTML = `<div class="empty">등록된 시간이 없습니다.</div>`;
        syncSummary();
        return;
      }

      state.availableTimes.forEach((time) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = `time-button${time.id === state.selectedTimeId ? " selected" : ""}`;
        button.disabled = !time.isAvailable;
        button.textContent = normalizeTime(time.startAt);
        button.addEventListener("click", () => {
          state.selectedTimeId = time.id;
          renderTimes();
        });
        elements.timeGrid.appendChild(button);
      });

      syncSummary();
    }

    function syncSummary() {
      const theme = selectedTheme();
      const time = selectedTime();
      elements.dateNote.textContent = `${formatDate(elements.dateInput.value)} 기준으로 선택한 테마의 비어 있는 시간만 보여줍니다.`;
      elements.summaryDate.textContent = formatDate(elements.dateInput.value);
      elements.summaryTheme.textContent = theme ? theme.name : "-";
      elements.summaryTime.textContent = time ? normalizeTime(time.startAt) : "-";

      const canReserve = Boolean(elements.nameInput.value.trim() && theme && time);
      elements.reserveButton.disabled = !canReserve;
      elements.formMessage.textContent = canReserve ? "" : "이름, 테마, 시간을 모두 선택하면 예약할 수 있습니다.";
      elements.formMessage.className = "message";
    }

    function getDemoAvailabilityFor(date, themeId) {
      return state.times.map((time) => {
        const reserved = state.demoReservations.some((reservation) =>
          reservation.date === date &&
          reservation.themeId === themeId &&
          reservation.timeId === time.id
        );
        return { ...time, isAvailable: !reserved };
      });
    }

    function getDemoAvailability() {
      return getDemoAvailabilityFor(elements.dateInput.value, state.selectedThemeId);
    }

    async function loadAvailability() {
      if (!state.selectedThemeId) {
        state.availableTimes = [];
        renderTimes();
        return;
      }

      if (state.mode === "live") {
        try {
          const data = await getJson(`/times/availability?date=${elements.dateInput.value}&themeId=${state.selectedThemeId}`);
          state.availableTimes = data.availableTimes || [];
        } catch (error) {
          state.mode = "demo";
          setSourceStatus();
          state.availableTimes = getDemoAvailability();
        }
      } else {
        state.availableTimes = getDemoAvailability();
      }

      const selected = selectedTime();
      if (selected && !selected.isAvailable) {
        state.selectedTimeId = null;
      }
      renderTimes();
    }

    async function loadAdminAvailability() {
      const date = elements.adminReserveDate.value;
      const themeId = state.adminSelectedThemeId;
      if (!date || !themeId) {
        state.adminAvailableTimes = [];
        state.adminSelectedTimeId = null;
        renderAdminReserveTimes();
        return;
      }

      if (state.mode === "live") {
        try {
          const data = await getJson(`/times/availability?date=${date}&themeId=${themeId}`);
          state.adminAvailableTimes = data.availableTimes || [];
        } catch (error) {
          state.mode = "demo";
          setSourceStatus();
          state.adminAvailableTimes = getDemoAvailabilityFor(date, themeId);
        }
      } else {
        state.adminAvailableTimes = getDemoAvailabilityFor(date, themeId);
      }

      const selected = selectedAdminTime();
      if (selected && !selected.isAvailable) {
        state.adminSelectedTimeId = null;
      }
      renderAdminReserveTimes();
    }

    async function reserve() {
      const theme = selectedTheme();
      const time = selectedTime();
      const name = elements.nameInput.value.trim();
      if (!theme || !time || !name) {
        syncSummary();
        return;
      }

      const payload = {
        name,
        date: elements.dateInput.value,
        timeId: time.id,
        themeId: theme.id
      };

      try {
        let createdReservation = null;
        if (state.mode === "live") {
          createdReservation = await postJson("/reservations", payload);
        } else {
          createdReservation = {
            id: getNextId(state.demoReservations),
            name: payload.name,
            date: payload.date,
            themeId: payload.themeId,
            timeId: payload.timeId
          };
          state.demoReservations = [...state.demoReservations, createdReservation];
        }
        state.reservations = [...state.reservations, createdReservation];

        showToast(`${name}님의 예약이 완료되었습니다.`, `${formatDate(payload.date)} · ${theme.name} · ${normalizeTime(time.startAt)}`);
        elements.nameInput.value = "";
        state.selectedTimeId = null;
        await loadAvailability();
        elements.formMessage.textContent = "예약이 완료되었습니다.";
        elements.formMessage.className = "message ok";
      } catch (error) {
        elements.formMessage.textContent = endpointMessageOr(error, "예약 요청에 실패했습니다.");
        elements.formMessage.className = "message error";
      }
    }

    function showToast(title, detail) {
      elements.toast.innerHTML = `<strong>${title}</strong><span>${detail}</span>`;
      elements.toast.classList.add("show");
      window.clearTimeout(showToast.timer);
      showToast.timer = window.setTimeout(() => {
        elements.toast.classList.remove("show");
      }, 3600);
    }

    function getNextId(items) {
      return Math.max(0, ...items.map((item) => Number(item.id) || 0)) + 1;
    }

    function setAdminMessage(text, type = "") {
      elements.adminMessage.textContent = text;
      elements.adminMessage.className = `admin-message${type ? ` ${type}` : ""}`;
    }

    function setAdminReserveMessage(text, type = "") {
      elements.adminReserveMessage.textContent = text;
      elements.adminReserveMessage.className = `admin-message${type ? ` ${type}` : ""}`;
    }

    function getReservationTheme(reservation) {
      return reservation.theme || state.themes.find((theme) => theme.id === reservation.themeId) || null;
    }

    function getReservationTime(reservation) {
      return reservation.time || state.times.find((time) => time.id === reservation.timeId) || null;
    }

    function renderAdmin() {
      elements.themeMetric.textContent = state.themes.length;
      elements.timeMetric.textContent = state.times.length;
      elements.reservationMetric.textContent = state.reservations.length;
      elements.reservationCount.textContent = `GET /admin/reservations · ${state.reservations.length}건`;
      elements.adminThemeCount.textContent = `${state.themes.length}개`;
      elements.adminTimeCount.textContent = `${state.times.length}개`;
      renderAdminReservationForm();
      renderAdminReservations();
      renderAdminThemes();
      renderAdminTimes();
    }

    function renderAdminReservationForm() {
      const previousThemeId = Number(elements.adminReserveTheme.value) || state.adminSelectedThemeId;
      elements.adminReserveTheme.innerHTML = "";

      if (state.themes.length === 0) {
        elements.adminReserveTheme.innerHTML = `<option value="">테마 없음</option>`;
        state.adminSelectedThemeId = null;
      } else {
        state.themes.forEach((theme) => {
          const option = document.createElement("option");
          option.value = theme.id;
          option.textContent = theme.name;
          elements.adminReserveTheme.appendChild(option);
        });
        const nextThemeId = state.themes.some((theme) => theme.id === previousThemeId)
          ? previousThemeId
          : state.themes[0].id;
        state.adminSelectedThemeId = nextThemeId;
        elements.adminReserveTheme.value = String(nextThemeId);
      }

      renderAdminReserveTimes();
    }

    function renderAdminReserveTimes() {
      elements.adminReserveTimeGrid.innerHTML = "";

      if (!state.adminSelectedThemeId) {
        elements.adminReserveTimeGrid.innerHTML = `<div class="empty">테마를 선택하면 시간이 표시됩니다.</div>`;
        syncAdminReserveSummary();
        return;
      }

      if (state.adminAvailableTimes.length === 0) {
        elements.adminReserveTimeGrid.innerHTML = `<div class="empty">등록된 시간이 없습니다.</div>`;
        syncAdminReserveSummary();
        return;
      }

      state.adminAvailableTimes.forEach((time) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = `time-button${time.id === state.adminSelectedTimeId ? " selected" : ""}`;
        button.disabled = !time.isAvailable;
        button.textContent = normalizeTime(time.startAt);
        button.addEventListener("click", () => {
          state.adminSelectedTimeId = time.id;
          renderAdminReserveTimes();
        });
        elements.adminReserveTimeGrid.appendChild(button);
      });

      syncAdminReserveSummary();
    }

    function syncAdminReserveSummary() {
      const theme = selectedAdminTheme();
      const time = selectedAdminTime();
      const name = elements.adminReserveName.value.trim();
      elements.adminReserveSummary.innerHTML = `
        <span>날짜 <strong>${escapeHtml(formatDate(elements.adminReserveDate.value))}</strong></span>
        <span>테마 <strong>${escapeHtml(theme?.name || "-")}</strong></span>
        <span>시간 <strong>${escapeHtml(time ? normalizeTime(time.startAt) : "-")}</strong></span>
      `;
      elements.adminReserveButton.disabled = !(name && theme && time);
    }

    function renderAdminReservations() {
      elements.adminReservationList.innerHTML = "";
      if (state.reservations.length === 0) {
        elements.adminReservationList.innerHTML = `<div class="empty">등록된 예약이 없습니다.</div>`;
        return;
      }

      [...state.reservations]
        .sort((a, b) => String(b.date).localeCompare(String(a.date)) || Number(b.id) - Number(a.id))
        .forEach((reservation) => {
          const theme = getReservationTheme(reservation);
          const time = getReservationTime(reservation);
          const row = document.createElement("div");
          row.className = "list-row";
          row.innerHTML = `
            <div class="list-main">
              <span class="list-title">${escapeHtml(reservation.name || "예약자")}</span>
              <span class="list-meta">${escapeHtml(formatDate(reservation.date))} · ${escapeHtml(theme?.name || "-")} · ${escapeHtml(normalizeTime(time?.startAt || "-"))}</span>
            </div>
            <button class="danger-button" type="button" data-delete-reservation-id="${reservation.id}">삭제</button>
          `;
          elements.adminReservationList.appendChild(row);
        });
    }

    function renderAdminThemes() {
      elements.adminThemeList.innerHTML = "";
      if (state.themes.length === 0) {
        elements.adminThemeList.innerHTML = `<div class="empty">등록된 테마가 없습니다.</div>`;
        return;
      }

      state.themes.forEach((theme) => {
        const row = document.createElement("div");
        row.className = "list-row theme-list-row";
        const imageSource = themeImageSource(theme);
        row.innerHTML = `
          <img class="admin-thumb" src="${escapeHtml(imageSource)}" alt="${escapeHtml(theme.name)} 썸네일">
          <div class="list-main">
            <span class="list-title">${escapeHtml(theme.name)}</span>
            <span class="list-meta">${escapeHtml(theme.description || "")}</span>
          </div>
          <button class="danger-button" type="button" data-delete-theme-id="${theme.id}">삭제</button>
        `;
        const img = row.querySelector("img");
        img.addEventListener("error", () => {
          img.src = posterFor(theme);
        }, { once: true });
        elements.adminThemeList.appendChild(row);
      });
    }

    function renderAdminTimes() {
      elements.adminTimeList.innerHTML = "";
      if (state.times.length === 0) {
        elements.adminTimeList.innerHTML = `<div class="empty">등록된 시간이 없습니다.</div>`;
        return;
      }

      [...state.times]
        .sort((a, b) => normalizeTime(a.startAt).localeCompare(normalizeTime(b.startAt)))
        .forEach((time) => {
          const row = document.createElement("div");
          row.className = "list-row";
          row.innerHTML = `
            <div class="list-main">
              <span class="list-title">${escapeHtml(normalizeTime(time.startAt))}</span>
              <span class="list-meta">모든 테마 공통 시작 시간</span>
            </div>
            <button class="danger-button" type="button" data-delete-time-id="${time.id}">삭제</button>
          `;
          elements.adminTimeList.appendChild(row);
        });
    }

    async function syncAfterAdminChange() {
      renderAdmin();
      await loadAdminAvailability();
      renderAdmin();
    }

    async function createTheme(event) {
      event.preventDefault();
      const payload = {
        name: elements.adminThemeName.value.trim(),
        description: elements.adminThemeDescription.value.trim(),
        thumbnail: elements.adminThemeThumbnail.value.trim()
      };
      if (!payload.name) {
        setAdminMessage("테마 이름을 입력해주세요.", "error");
        return;
      }

      try {
        const theme = state.mode === "live"
          ? await postJson("/admin/themes", payload)
          : { id: getNextId(state.themes), ...payload };
        state.themes = [...state.themes, theme];
        if (state.popularThemes.length < 10) {
          state.popularThemes = [...state.popularThemes, theme];
        }
        state.selectedThemeId = state.selectedThemeId || theme.id;
        elements.themeForm.reset();
        setAdminMessage("테마가 추가되었습니다.", "ok");
        showToast("테마가 추가되었습니다.", theme.name);
        await syncAfterAdminChange();
      } catch (error) {
        setAdminMessage(endpointMessageOr(error, "테마 추가에 실패했습니다."), "error");
      }
    }

    async function createTime(event) {
      event.preventDefault();
      const startAt = elements.adminTimeStartAt.value;
      if (!startAt) {
        setAdminMessage("시작 시간을 선택해주세요.", "error");
        return;
      }

      try {
        const time = state.mode === "live"
          ? await postJson("/admin/times", { startAt })
          : { id: getNextId(state.times), startAt };
        state.times = [...state.times, time];
        setAdminMessage("예약 시간이 추가되었습니다.", "ok");
        showToast("예약 시간이 추가되었습니다.", normalizeTime(startAt));
        await syncAfterAdminChange();
      } catch (error) {
        setAdminMessage(endpointMessageOr(error, "시간 추가에 실패했습니다."), "error");
      }
    }

    async function createAdminReservation(event) {
      event.preventDefault();
      const theme = selectedAdminTheme();
      const time = selectedAdminTime();
      const name = elements.adminReserveName.value.trim();
      if (!name || !theme || !time) {
        setAdminReserveMessage("이름, 테마, 시간을 모두 선택해주세요.", "error");
        syncAdminReserveSummary();
        return;
      }

      const payload = {
        name,
        date: elements.adminReserveDate.value,
        timeId: time.id,
        themeId: theme.id
      };

      try {
        let createdReservation = null;
        if (state.mode === "live") {
          createdReservation = await postJson("/reservations", payload);
        } else {
          createdReservation = {
            id: getNextId(state.demoReservations),
            name: payload.name,
            date: payload.date,
            themeId: payload.themeId,
            timeId: payload.timeId
          };
          state.demoReservations = [...state.demoReservations, createdReservation];
        }
        state.reservations = [...state.reservations, createdReservation];
        elements.adminReserveName.value = "";
        state.adminSelectedTimeId = null;
        setAdminReserveMessage("예약이 추가되었습니다.", "ok");
        showToast("관리자 예약이 추가되었습니다.", `${formatDate(payload.date)} · ${theme.name} · ${normalizeTime(time.startAt)}`);
        await syncAfterAdminChange();
      } catch (error) {
        setAdminReserveMessage(endpointMessageOr(error, "예약 추가에 실패했습니다."), "error");
      }
    }

    async function deleteTheme(id) {
      try {
        if (state.mode === "live") {
          await deleteJson(`/admin/themes/${id}`);
        }
        state.themes = state.themes.filter((theme) => theme.id !== id);
        state.popularThemes = state.popularThemes.filter((theme) => theme.id !== id);
        state.demoReservations = state.demoReservations.filter((reservation) => reservation.themeId !== id);
        state.reservations = state.reservations.filter((reservation) => {
          const themeId = reservation.themeId || reservation.theme?.id;
          return themeId !== id;
        });
        if (state.selectedThemeId === id) {
          state.selectedThemeId = state.themes[0]?.id || null;
          state.selectedTimeId = null;
        }
        setAdminMessage("테마가 삭제되었습니다.", "ok");
        await syncAfterAdminChange();
      } catch (error) {
        setAdminMessage(endpointMessageOr(error, "테마 삭제에 실패했습니다."), "error");
      }
    }

    async function deleteTime(id) {
      try {
        if (state.mode === "live") {
          await deleteJson(`/admin/times/${id}`);
        }
        state.times = state.times.filter((time) => time.id !== id);
        state.demoReservations = state.demoReservations.filter((reservation) => reservation.timeId !== id);
        state.reservations = state.reservations.filter((reservation) => {
          const timeId = reservation.timeId || reservation.time?.id;
          return timeId !== id;
        });
        if (state.selectedTimeId === id) {
          state.selectedTimeId = null;
        }
        setAdminMessage("예약 시간이 삭제되었습니다.", "ok");
        await syncAfterAdminChange();
      } catch (error) {
        setAdminMessage(endpointMessageOr(error, "시간 삭제에 실패했습니다."), "error");
      }
    }

    async function deleteReservation(id) {
      try {
        if (state.mode === "live") {
          await deleteJson(`/admin/reservations/${id}`);
        }
        state.demoReservations = state.demoReservations.filter((reservation) => reservation.id !== id);
        state.reservations = state.reservations.filter((reservation) => reservation.id !== id);
        setAdminMessage("예약이 삭제되었습니다.", "ok");
        await syncAfterAdminChange();
      } catch (error) {
        setAdminMessage(endpointMessageOr(error, "예약 삭제에 실패했습니다."), "error");
      }
    }

    function renderDemoFirst() {
      state.mode = "demo";
      state.themes = demoThemes;
      state.popularThemes = demoThemes.slice(0, 10);
      state.times = demoTimes.map(({ id, startAt }) => ({ id, startAt }));
      state.reservations = [...state.demoReservations];
      setSourceStatus();

      if (isAdminPage()) {
        state.adminSelectedThemeId = state.themes[0]?.id || null;
        state.adminSelectedTimeId = null;
        elements.adminReserveDate.value = DEMO_DATE;
        state.adminAvailableTimes = getDemoAvailabilityFor(elements.adminReserveDate.value, state.adminSelectedThemeId);
        renderAdmin();
        return;
      }

      state.selectedThemeId = state.themes[0]?.id || null;
      state.selectedTimeId = null;
      elements.dateInput.value = DEMO_DATE;
      renderPopularThemes();
      renderThemes();
      state.availableTimes = getDemoAvailability();
      renderTimes();
      syncSummary();
    }

    async function loadInitialData() {
      const isFilePreview = window.location.protocol === "file:";
      if (isFilePreview) {
        renderDemoFirst();
        return;
      } else {
        if (isAdminPage()) {
          elements.adminReserveDate.value = DEMO_DATE;
        } else {
          elements.dateInput.value = DEMO_DATE;
        }
        state.mode = "loading";
        setSourceStatus();
      }

      try {
        const [themeData, popularityData, timeData, reservationData] = isAdminPage()
          ? await Promise.all([
              getJson("/themes"),
              Promise.resolve({ themes: [] }),
              getJson("/times"),
              getReservationListData()
            ])
          : await Promise.all([
              getJson("/themes"),
              getJson("/themes/popularity?days=7&size=10"),
              getJson("/times"),
              Promise.resolve({ reservations: [] })
            ]);
        state.mode = "live";
        state.themes = themeData.themes || [];
        state.popularThemes = popularityData.themes || popularityData.popularThemes || [];
        state.times = timeData.times || [];
        state.reservations = reservationData.reservations || [];
        setSourceStatus();

        if (isAdminPage()) {
          state.adminSelectedThemeId = state.themes[0]?.id || null;
          state.adminSelectedTimeId = null;
          await loadAdminAvailability();
          renderAdmin();
          return;
        }

        state.selectedThemeId = state.themes[0]?.id || null;
        state.selectedTimeId = null;
        renderPopularThemes();
        renderThemes();
        await loadAvailability();
        syncSummary();
      } catch (error) {
        state.mode = "demo";
        renderDemoFirst();
        setSourceStatus();
      }
    }

    if (isUserPage()) {
      elements.dateInput.addEventListener("change", () => {
        state.selectedTimeId = null;
        loadAvailability();
      });
      elements.nameInput.addEventListener("input", syncSummary);
      elements.reserveButton.addEventListener("click", reserve);
    }

    if (isAdminPage()) {
      elements.adminReservationForm.addEventListener("submit", createAdminReservation);
      elements.adminReserveName.addEventListener("input", syncAdminReserveSummary);
      elements.adminReserveDate.addEventListener("change", () => {
        state.adminSelectedTimeId = null;
        loadAdminAvailability();
      });
      elements.adminReserveTheme.addEventListener("change", () => {
        state.adminSelectedThemeId = Number(elements.adminReserveTheme.value) || null;
        state.adminSelectedTimeId = null;
        loadAdminAvailability();
      });
      elements.themeForm.addEventListener("submit", createTheme);
      elements.timeForm.addEventListener("submit", createTime);
      elements.adminThemeList.addEventListener("click", (event) => {
        const button = event.target.closest("[data-delete-theme-id]");
        if (button) {
          deleteTheme(Number(button.dataset.deleteThemeId));
        }
      });
      elements.adminTimeList.addEventListener("click", (event) => {
        const button = event.target.closest("[data-delete-time-id]");
        if (button) {
          deleteTime(Number(button.dataset.deleteTimeId));
        }
      });
      elements.adminReservationList.addEventListener("click", (event) => {
        const button = event.target.closest("[data-delete-reservation-id]");
        if (button) {
          deleteReservation(Number(button.dataset.deleteReservationId));
        }
      });
    }

    loadInitialData();
