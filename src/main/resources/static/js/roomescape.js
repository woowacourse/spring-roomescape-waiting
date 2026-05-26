const API_BASE = "";

    function toDateInputValue(date) {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      return `${year}-${month}-${day}`;
    }

    function dateOffset(days) {
      const date = new Date();
      date.setDate(date.getDate() + days);
      return toDateInputValue(date);
    }

    const TODAY = dateOffset(0);
    const DEFAULT_RESERVATION_DATE = dateOffset(1);
    const DEMO_POPULARITY_DATE = dateOffset(-1);
    const PAGE = document.body.dataset.page || "user";

    const state = {
      currentView: "user",
      mode: "demo",
      themes: [],
      popularThemes: [],
      times: [],
      reservations: [],
      myReservations: [],
      availableTimes: [],
      demoReservations: [
        { id: 1, name: "브라운", date: DEFAULT_RESERVATION_DATE, themeId: 1, timeId: 1 },
        { id: 2, name: "브라운", date: dateOffset(2), themeId: 2, timeId: 2 },
        { id: 3, name: "guest-10", date: DEFAULT_RESERVATION_DATE, themeId: 1, timeId: 3 },
        { id: 4, name: "guest-18", date: DEMO_POPULARITY_DATE, themeId: 2, timeId: 1 },
        { id: 5, name: "guest-19", date: DEMO_POPULARITY_DATE, themeId: 2, timeId: 2 },
        { id: 6, name: "guest-56", date: DEMO_POPULARITY_DATE, themeId: 11, timeId: 1 },
        { id: 7, name: "guest-57", date: DEMO_POPULARITY_DATE, themeId: 11, timeId: 2 }
      ],
      selectedThemeId: null,
      selectedTimeId: null,
      adminSelectedThemeId: null,
      adminSelectedTimeId: null,
      adminAvailableTimes: [],
      editingReservationId: null,
      editDate: "",
      editAvailableTimes: [],
      editSelectedTimeId: null
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
      myReservationName: $("#myReservationName"),
      myReservationButton: $("#myReservationButton"),
      myReservationMessage: $("#myReservationMessage"),
      myReservationList: $("#myReservationList"),
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

    async function requestJson(path, options = {}) {
      const controller = new AbortController();
      const timer = window.setTimeout(() => controller.abort(), 5000);
      const response = await fetch(`${API_BASE}${path}`, {
        method: options.method || "GET",
        headers: {
          Accept: "application/json",
          ...(options.body ? { "Content-Type": "application/json" } : {})
        },
        body: options.body ? JSON.stringify(options.body) : undefined,
        signal: controller.signal
      }).finally(() => window.clearTimeout(timer));

      if (!response.ok) {
        throw new Error(await errorMessageFrom(response));
      }

      if (response.status === 204 || options.expectJson === false) {
        return null;
      }

      return response.json();
    }

    async function getJson(path) {
      return requestJson(path);
    }

    async function postJson(path, body) {
      return requestJson(path, { method: "POST", body });
    }

    async function patchJson(path, body) {
      return requestJson(path, { method: "PATCH", body });
    }

    async function deleteJson(path) {
      return requestJson(path, { method: "DELETE", expectJson: false });
    }

    async function errorMessageFrom(response) {
      const fallback = `HTTP ${response.status}`;
      const contentType = response.headers.get("content-type") || "";
      if (!contentType.includes("application/json") && !contentType.includes("+json")) {
        return fallback;
      }

      try {
        const body = await response.json();
        const detail = typeof body.detail === "string" && body.detail.trim()
          ? body.detail.trim()
          : "";
        const message = typeof body.message === "string" && body.message.trim()
          ? body.message.trim()
          : "";
        const title = typeof body.title === "string" && body.title.trim()
          ? body.title.trim()
          : "";
        const validationErrors = Array.isArray(body.errors)
          ? body.errors
              .map((error) => {
                const field = error.field ? `${fieldLabel(error.field)}: ` : "";
                const reason = error.reason || error.message || "";
                return `${field}${reason}`.trim();
              })
              .filter(Boolean)
          : [];
        const mainMessage = detail || message || title || fallback;
        return [mainMessage, ...validationErrors].join("\n");
      } catch (error) {
        return fallback;
      }
    }

    function fieldLabel(field) {
      const labels = {
        name: "이름",
        date: "날짜",
        timeId: "시간",
        themeId: "테마",
        startAt: "시작 시간",
        description: "설명",
        thumbnail: "썸네일"
      };
      return labels[field] || field;
    }

    function endpointMessageOr(error, fallback) {
      if (error instanceof DOMException && error.name === "AbortError") {
        return "요청 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.";
      }
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
          state.availableTimes = [];
          state.selectedTimeId = null;
          renderTimes();
          elements.formMessage.textContent = endpointMessageOr(error, "예약 가능 시간을 불러오지 못했습니다.");
          elements.formMessage.className = "message error";
          return;
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
          state.adminAvailableTimes = [];
          state.adminSelectedTimeId = null;
          renderAdminReserveTimes();
          setAdminReserveMessage(endpointMessageOr(error, "예약 가능 시간을 불러오지 못했습니다."), "error");
          return;
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
          createdReservation = createDemoReservation(payload);
        }
        state.reservations = [...state.reservations, createdReservation];
        if (elements.myReservationName.value.trim() === name) {
          state.myReservations = [...state.myReservations, createdReservation];
          renderMyReservations();
        }

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

    async function loadMyReservations() {
      const name = elements.myReservationName.value.trim();
      if (!name) {
        state.myReservations = [];
        state.editingReservationId = null;
        renderMyReservations();
        setMyReservationMessage("예약자 이름을 입력해주세요.", "error");
        return;
      }

      try {
        const data = state.mode === "live"
          ? await getJson(`/reservations?name=${encodeURIComponent(name)}`)
          : { reservations: state.demoReservations.filter((reservation) => reservation.name === name) };
        state.myReservations = data.reservations || [];
        state.editingReservationId = null;
        state.editAvailableTimes = [];
        state.editSelectedTimeId = null;
        renderMyReservations();
        setMyReservationMessage(
          state.myReservations.length === 0 ? "조회된 예약이 없습니다." : `${state.myReservations.length}건의 예약을 조회했습니다.`,
          state.myReservations.length === 0 ? "" : "ok"
        );
      } catch (error) {
        setMyReservationMessage(endpointMessageOr(error, "예약 조회에 실패했습니다."), "error");
      }
    }

    function renderMyReservations() {
      if (!elements.myReservationList) {
        return;
      }

      elements.myReservationList.innerHTML = "";
      if (state.myReservations.length === 0) {
        elements.myReservationList.innerHTML = `<div class="empty">조회된 예약이 없습니다.</div>`;
        return;
      }

      [...state.myReservations]
        .sort((a, b) => String(a.date).localeCompare(String(b.date)) || normalizeTime(getReservationTime(a)?.startAt || "").localeCompare(normalizeTime(getReservationTime(b)?.startAt || "")))
        .forEach((reservation) => {
          const theme = getReservationTheme(reservation);
          const time = getReservationTime(reservation);
          const isEditing = state.editingReservationId === reservation.id;
          const card = document.createElement("div");
          card.className = "my-reservation-card";
          card.innerHTML = `
            <div class="my-reservation-main">
              <div class="list-main">
                <span class="list-title">${escapeHtml(theme?.name || "-")}</span>
                <span class="list-meta">${escapeHtml(formatDate(reservation.date))} · ${escapeHtml(normalizeTime(time?.startAt || "-"))} · ${escapeHtml(reservation.name || "")}</span>
              </div>
              <div class="reservation-actions">
                <button class="secondary-button" type="button" data-edit-reservation-id="${reservation.id}">${isEditing ? "닫기" : "변경"}</button>
                <button class="danger-button" type="button" data-cancel-reservation-id="${reservation.id}">취소</button>
              </div>
            </div>
            ${isEditing ? `
              <div class="reservation-edit">
                <div class="edit-grid">
                  <label class="field">
                    <span class="label">변경 날짜</span>
                    <input class="date-input" type="date" min="${TODAY}" value="${escapeHtml(state.editDate)}" data-edit-date>
                  </label>
                  <div class="field">
                    <span class="label">변경 시간</span>
                    <div class="edit-time-grid" data-edit-time-grid></div>
                  </div>
                </div>
                <div class="edit-actions">
                  <button class="primary-button" type="button" data-save-reservation-id="${reservation.id}" ${state.editSelectedTimeId ? "" : "disabled"}>변경 저장</button>
                </div>
              </div>
            ` : ""}
          `;
          elements.myReservationList.appendChild(card);
          if (isEditing) {
            renderEditTimes(card);
          }
        });
    }

    function renderEditTimes(container) {
      const grid = container.querySelector("[data-edit-time-grid]");
      if (!grid) {
        return;
      }
      grid.innerHTML = "";

      if (!state.editDate) {
        grid.innerHTML = `<div class="empty">날짜를 선택해주세요.</div>`;
        return;
      }

      if (state.editAvailableTimes.length === 0) {
        grid.innerHTML = `<div class="empty">예약 가능한 시간이 없습니다.</div>`;
        return;
      }

      state.editAvailableTimes.forEach((time) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = `time-button${time.id === state.editSelectedTimeId ? " selected" : ""}`;
        button.disabled = !time.isAvailable;
        button.textContent = normalizeTime(time.startAt);
        button.dataset.editTimeId = String(time.id);
        grid.appendChild(button);
      });
    }

    async function toggleEditReservation(id) {
      if (state.editingReservationId === id) {
        state.editingReservationId = null;
        state.editAvailableTimes = [];
        state.editSelectedTimeId = null;
        renderMyReservations();
        return;
      }

      const reservation = state.myReservations.find((item) => item.id === id);
      if (!reservation) {
        setMyReservationMessage("변경할 예약을 찾을 수 없습니다.", "error");
        return;
      }

      state.editingReservationId = id;
      state.editDate = reservation.date;
      state.editSelectedTimeId = null;
      state.editAvailableTimes = [];
      renderMyReservations();
      await loadEditAvailability();
    }

    async function loadEditAvailability() {
      const reservation = state.myReservations.find((item) => item.id === state.editingReservationId);
      if (!reservation) {
        return;
      }

      const themeId = getReservationThemeId(reservation);
      if (!state.editDate || !themeId) {
        state.editAvailableTimes = [];
        state.editSelectedTimeId = null;
        renderMyReservations();
        return;
      }

      try {
        if (state.mode === "live") {
          const data = await getJson(`/times/availability?date=${state.editDate}&themeId=${themeId}`);
          state.editAvailableTimes = data.availableTimes || [];
        } else {
          state.editAvailableTimes = getDemoAvailabilityFor(state.editDate, themeId);
        }
        const selected = state.editAvailableTimes.find((time) => time.id === state.editSelectedTimeId);
        if (selected && !selected.isAvailable) {
          state.editSelectedTimeId = null;
        }
        renderMyReservations();
      } catch (error) {
        state.editAvailableTimes = [];
        state.editSelectedTimeId = null;
        renderMyReservations();
        setMyReservationMessage(endpointMessageOr(error, "변경 가능한 시간을 불러오지 못했습니다."), "error");
      }
    }

    async function updateMyReservation(id) {
      const reservation = state.myReservations.find((item) => item.id === id);
      const name = elements.myReservationName.value.trim();
      if (!reservation || !name) {
        setMyReservationMessage("예약 정보를 다시 조회해주세요.", "error");
        return;
      }
      if (!state.editDate || !state.editSelectedTimeId) {
        setMyReservationMessage("변경할 날짜와 시간을 선택해주세요.", "error");
        return;
      }

      const payload = {
        name,
        date: state.editDate,
        timeId: state.editSelectedTimeId
      };

      try {
        const updatedReservation = state.mode === "live"
          ? await patchJson(`/reservations/${id}`, payload)
          : updateDemoReservation(id, payload);
        replaceReservation(updatedReservation);
        state.editingReservationId = null;
        state.editAvailableTimes = [];
        state.editSelectedTimeId = null;
        renderMyReservations();
        setMyReservationMessage("예약이 변경되었습니다.", "ok");
        showToast("예약이 변경되었습니다.", `${formatDate(updatedReservation.date)} · ${normalizeTime(getReservationTime(updatedReservation)?.startAt || "")}`);
        await loadAvailability();
      } catch (error) {
        setMyReservationMessage(endpointMessageOr(error, "예약 변경에 실패했습니다."), "error");
      }
    }

    async function cancelMyReservation(id) {
      const name = elements.myReservationName.value.trim();
      if (!name) {
        setMyReservationMessage("예약자 이름을 입력해주세요.", "error");
        return;
      }

      try {
        if (state.mode === "live") {
          await deleteJson(`/reservations/${id}?name=${encodeURIComponent(name)}`);
        } else {
          cancelDemoReservation(id, name);
        }
        state.myReservations = state.myReservations.filter((reservation) => reservation.id !== id);
        state.reservations = state.reservations.filter((reservation) => reservation.id !== id);
        if (state.editingReservationId === id) {
          state.editingReservationId = null;
          state.editAvailableTimes = [];
          state.editSelectedTimeId = null;
        }
        renderMyReservations();
        setMyReservationMessage("예약이 취소되었습니다.", "ok");
        await loadAvailability();
      } catch (error) {
        setMyReservationMessage(endpointMessageOr(error, "예약 취소에 실패했습니다."), "error");
      }
    }

    function showToast(title, detail) {
      elements.toast.innerHTML = `<strong>${escapeHtml(title)}</strong><span>${escapeHtml(detail)}</span>`;
      elements.toast.classList.add("show");
      window.clearTimeout(showToast.timer);
      showToast.timer = window.setTimeout(() => {
        elements.toast.classList.remove("show");
      }, 3600);
    }

    function getNextId(items) {
      return Math.max(0, ...items.map((item) => Number(item.id) || 0)) + 1;
    }

    function getReservationThemeId(reservation) {
      return Number(reservation.themeId || reservation.theme?.id) || null;
    }

    function getReservationTimeId(reservation) {
      return Number(reservation.timeId || reservation.time?.id) || null;
    }

    function isPastDateTime(date, timeId) {
      const time = state.times.find((item) => item.id === Number(timeId));
      if (!date || !time) {
        return false;
      }
      const target = new Date(`${date}T${normalizeTime(time.startAt)}:00`);
      return target.getTime() <= Date.now();
    }

    function assertFutureSlot(date, timeId) {
      if (isPastDateTime(date, timeId)) {
        throw new Error("현재 시각 이후의 날짜와 시간을 선택해주세요.");
      }
    }

    function assertNoDuplicateReservation(date, timeId, themeId, ignoreId = null) {
      const duplicated = state.demoReservations.some((reservation) =>
        reservation.id !== ignoreId &&
        reservation.date === date &&
        getReservationTimeId(reservation) === Number(timeId) &&
        getReservationThemeId(reservation) === Number(themeId)
      );
      if (duplicated) {
        throw new Error("선택한 날짜와 시간에는 이미 해당 테마의 예약이 있습니다. 다른 시간을 선택해주세요.");
      }
    }

    function createDemoReservation(payload) {
      assertFutureSlot(payload.date, payload.timeId);
      assertNoDuplicateReservation(payload.date, payload.timeId, payload.themeId);
      const reservation = {
        id: getNextId(state.demoReservations),
        name: payload.name,
        date: payload.date,
        themeId: payload.themeId,
        timeId: payload.timeId
      };
      state.demoReservations = [...state.demoReservations, reservation];
      return reservation;
    }

    function updateDemoReservation(id, payload) {
      const reservation = state.demoReservations.find((item) => item.id === id && item.name === payload.name);
      if (!reservation) {
        throw new Error("해당 이름으로 예약을 찾을 수 없습니다. 예약 정보를 확인해주세요.");
      }
      if (isPastDateTime(reservation.date, getReservationTimeId(reservation))) {
        throw new Error("이미 지난 예약은 변경할 수 없습니다.");
      }
      if (reservation.date === payload.date && getReservationTimeId(reservation) === Number(payload.timeId)) {
        throw new Error("현재 예약과 같은 날짜와 시간으로 변경할 수 없습니다.");
      }

      const themeId = getReservationThemeId(reservation);
      assertFutureSlot(payload.date, payload.timeId);
      assertNoDuplicateReservation(payload.date, payload.timeId, themeId, id);

      const updatedReservation = {
        ...reservation,
        date: payload.date,
        timeId: payload.timeId
      };
      state.demoReservations = state.demoReservations.map((item) =>
        item.id === id ? updatedReservation : item
      );
      return updatedReservation;
    }

    function cancelDemoReservation(id, name) {
      const reservation = state.demoReservations.find((item) => item.id === id && item.name === name);
      if (!reservation) {
        throw new Error("해당 이름으로 예약을 찾을 수 없습니다. 예약 정보를 확인해주세요.");
      }
      if (isPastDateTime(reservation.date, getReservationTimeId(reservation))) {
        throw new Error("이미 지난 예약은 취소할 수 없습니다.");
      }
      state.demoReservations = state.demoReservations.filter((item) => item.id !== id);
    }

    function replaceReservation(reservation) {
      const exists = state.reservations.some((item) => item.id === reservation.id);
      state.reservations = exists
        ? state.reservations.map((item) => item.id === reservation.id ? reservation : item)
        : [...state.reservations, reservation];
      state.myReservations = state.myReservations.map((item) =>
        item.id === reservation.id ? reservation : item
      );
    }

    function setAdminMessage(text, type = "") {
      elements.adminMessage.textContent = text;
      elements.adminMessage.className = `admin-message${type ? ` ${type}` : ""}`;
    }

    function setAdminReserveMessage(text, type = "") {
      elements.adminReserveMessage.textContent = text;
      elements.adminReserveMessage.className = `admin-message${type ? ` ${type}` : ""}`;
    }

    function setMyReservationMessage(text, type = "") {
      elements.myReservationMessage.textContent = text;
      elements.myReservationMessage.className = `message${type ? ` ${type}` : ""}`;
    }

    function getReservationTheme(reservation) {
      return reservation.theme || state.themes.find((theme) => theme.id === getReservationThemeId(reservation)) || null;
    }

    function getReservationTime(reservation) {
      return reservation.time || state.times.find((time) => time.id === getReservationTimeId(reservation)) || null;
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
          createdReservation = createDemoReservation(payload);
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
        } else if (state.demoReservations.some((reservation) => getReservationTimeId(reservation) === id)) {
          throw new Error("예약이 존재하는 시간은 삭제할 수 없습니다. 먼저 해당 예약들을 삭제해주세요.");
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

    function initializeDateInputs() {
      if (elements.dateInput) {
        elements.dateInput.min = TODAY;
      }
      if (elements.adminReserveDate) {
        elements.adminReserveDate.min = TODAY;
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
        elements.adminReserveDate.value = DEFAULT_RESERVATION_DATE;
        state.adminAvailableTimes = getDemoAvailabilityFor(elements.adminReserveDate.value, state.adminSelectedThemeId);
        renderAdmin();
        return;
      }

      state.selectedThemeId = state.themes[0]?.id || null;
      state.selectedTimeId = null;
      elements.dateInput.value = DEFAULT_RESERVATION_DATE;
      renderPopularThemes();
      renderThemes();
      state.availableTimes = getDemoAvailability();
      renderTimes();
      syncSummary();
    }

    async function loadInitialData() {
      initializeDateInputs();
      const isFilePreview = window.location.protocol === "file:";
      if (isFilePreview) {
        renderDemoFirst();
        return;
      } else {
        if (isAdminPage()) {
          elements.adminReserveDate.value = DEFAULT_RESERVATION_DATE;
        } else {
          elements.dateInput.value = DEFAULT_RESERVATION_DATE;
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
      elements.myReservationButton.addEventListener("click", loadMyReservations);
      elements.myReservationName.addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
          loadMyReservations();
        }
      });
      elements.myReservationList.addEventListener("click", (event) => {
        const editButton = event.target.closest("[data-edit-reservation-id]");
        if (editButton) {
          toggleEditReservation(Number(editButton.dataset.editReservationId));
          return;
        }

        const cancelButton = event.target.closest("[data-cancel-reservation-id]");
        if (cancelButton) {
          cancelMyReservation(Number(cancelButton.dataset.cancelReservationId));
          return;
        }

        const timeButton = event.target.closest("[data-edit-time-id]");
        if (timeButton) {
          state.editSelectedTimeId = Number(timeButton.dataset.editTimeId);
          renderMyReservations();
          return;
        }

        const saveButton = event.target.closest("[data-save-reservation-id]");
        if (saveButton) {
          updateMyReservation(Number(saveButton.dataset.saveReservationId));
        }
      });
      elements.myReservationList.addEventListener("change", (event) => {
        if (event.target.matches("[data-edit-date]")) {
          state.editDate = event.target.value;
          state.editSelectedTimeId = null;
          loadEditAvailability();
        }
      });
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
