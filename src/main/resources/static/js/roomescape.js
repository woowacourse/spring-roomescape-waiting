const API_BASE = "";
    const GUEST_NAME_HEADER = "X-Guest-Name";
    const DEMO_DATE = "2026-05-06";
    const DEFAULT_DATE = todayDate();
    const PAGE = document.body.dataset.page || "user";

    const state = {
      currentView: "user",
      mode: "demo",
      themes: [],
      popularThemes: [],
      times: [],
      reservations: [],
      lookupReservations: [],
      demoReservations: [
        { id: 1, guestName: "guest-8", date: "2026-05-05", themeId: 1, timeId: 1, status: "CONFIRMED" },
        { id: 2, guestName: "guest-9", date: "2026-05-05", themeId: 1, timeId: 2, status: "CONFIRMED" },
        { id: 3, guestName: "guest-10", date: "2026-05-05", themeId: 1, timeId: 3, status: "CANCELED" },
        { id: 4, guestName: "guest-18", date: "2027-05-05", themeId: 2, timeId: 1, status: "CONFIRMED" },
        { id: 5, guestName: "guest-19", date: "2027-05-05", themeId: 2, timeId: 2, status: "CONFIRMED" },
        { id: 6, guestName: "guest-56", date: "2027-05-06", themeId: 11, timeId: 1, status: "CONFIRMED" },
        { id: 7, guestName: "guest-57", date: "2027-05-06", themeId: 11, timeId: 2, status: "WAITING", waitNumber: 1 }
      ],
      selectedThemeId: null,
      selectedTimeId: null,
      editingReservationId: null,
      editingReservationThemeId: null,
      cancelingReservationId: null,
      adminSelectedThemeId: null,
      adminSelectedTimeId: null,
      adminReservationPage: 1,
      adminReservationSize: 20,
      adminReservationNumberOfElements: 0,
      adminReservationTotalElements: 0,
      adminReservationTotalPages: 0,
      adminReservationHasPrevious: false,
      adminReservationHasNext: false
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
      { id: 1, startAt: "10:00" },
      { id: 2, startAt: "12:00" },
      { id: 3, startAt: "14:00" },
      { id: 4, startAt: "16:00" },
      { id: 5, startAt: "18:00" },
      { id: 6, startAt: "20:00" }
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

    function todayDate() {
      const today = new Date();
      const year = today.getFullYear();
      const month = String(today.getMonth() + 1).padStart(2, "0");
      const date = String(today.getDate()).padStart(2, "0");
      return `${year}-${month}-${date}`;
    }

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
      lookupForm: $("#lookupForm"),
      lookupGuestName: $("#lookupGuestName"),
      lookupButton: $("#lookupButton"),
      lookupMessage: $("#lookupMessage"),
      lookupList: $("#lookupList"),
      lookupCount: $("#lookupCount"),
      summaryStatus: $("#summaryStatus"),
      editReservationForm: $("#editReservationForm"),
      editReservationTitle: $("#editReservationTitle"),
      editReservationMeta: $("#editReservationMeta"),
      editAuthorizationName: $("#editAuthorizationName"),
      editReservationDate: $("#editReservationDate"),
      editReservationTime: $("#editReservationTime"),
      editReservationButton: $("#editReservationButton"),
      editReservationMessage: $("#editReservationMessage"),
      editCancelButton: $("#editCancelButton"),
      cancelReservationForm: $("#cancelReservationForm"),
      cancelReservationTitle: $("#cancelReservationTitle"),
      cancelReservationMeta: $("#cancelReservationMeta"),
      cancelAuthorizationName: $("#cancelAuthorizationName"),
      cancelReservationButton: $("#cancelReservationButton"),
      cancelReservationMessage: $("#cancelReservationMessage"),
      cancelReservationCloseButton: $("#cancelReservationCloseButton"),
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
      adminReservationPageSize: $("#adminReservationPageSize"),
      adminReservationPrevPage: $("#adminReservationPrevPage"),
      adminReservationNextPage: $("#adminReservationNextPage"),
      adminReservationPageLabel: $("#adminReservationPageLabel"),
      adminThemeCount: $("#adminThemeCount"),
      adminTimeCount: $("#adminTimeCount"),
      toast: $("#toast"),
      errorPopup: $("#errorPopup"),
      errorPopupTitle: $("#errorPopupTitle"),
      errorPopupMessage: $("#errorPopupMessage"),
      errorPopupClose: $("#errorPopupClose")
    };

    let popupPreviousFocus = null;

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

    async function getJson(path, headers = {}) {
      const controller = new AbortController();
      const timer = window.setTimeout(() => controller.abort(), 5000);
      const response = await fetch(`${API_BASE}${path}`, {
        headers: {
          Accept: "application/json",
          ...headers
        },
        signal: controller.signal
      }).finally(() => window.clearTimeout(timer));
      if (!response.ok) {
        throw new Error(await errorMessageFrom(response));
      }
      if (response.status === 204) {
        return null;
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
      if (response.status === 204) {
        return null;
      }
      return response.json();
    }

    async function patchJson(path, body, headers = {}) {
      const response = await fetch(`${API_BASE}${path}`, {
        method: "PATCH",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
          ...headers
        },
        body: JSON.stringify(body)
      });
      if (!response.ok) {
        throw new Error(await errorMessageFrom(response));
      }
      if (response.status === 204) {
        return null;
      }
      return response.json();
    }

    async function deleteJson(path, headers = {}) {
      const response = await fetch(`${API_BASE}${path}`, {
        method: "DELETE",
        headers: {
          Accept: "application/json",
          ...headers
        }
      });
      if (!response.ok) {
        throw new Error(await errorMessageFrom(response));
      }
    }

    function guestNameHeaders(guestName) {
      return {
        [GUEST_NAME_HEADER]: encodeURIComponent(guestName)
      };
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

    function showErrorPopup(message, title = "확인해주세요") {
      const text = String(message || "요청을 처리하지 못했습니다.");
      if (!elements.errorPopup) {
        window.alert(text);
        return;
      }

      popupPreviousFocus = document.activeElement;
      elements.errorPopupTitle.textContent = title;
      elements.errorPopupMessage.textContent = text;
      elements.errorPopup.hidden = false;
      elements.errorPopup.classList.add("show");
      document.body.classList.add("popup-open");
      elements.errorPopupClose.focus();
    }

    function hideErrorPopup() {
      if (!elements.errorPopup || elements.errorPopup.hidden) {
        return;
      }

      elements.errorPopup.classList.remove("show");
      elements.errorPopup.hidden = true;
      document.body.classList.remove("popup-open");

      if (popupPreviousFocus && typeof popupPreviousFocus.focus === "function") {
        popupPreviousFocus.focus();
      }
      popupPreviousFocus = null;
    }

    function setInlineMessage(element, baseClass, text, type = "") {
      if (!element) {
        return;
      }

      if (type === "error") {
        element.textContent = "";
        element.className = baseClass;
        showErrorPopup(text);
        return;
      }

      element.textContent = text;
      element.className = `${baseClass}${type ? ` ${type}` : ""}`;
    }

    function setFormMessage(text, type = "") {
      setInlineMessage(elements.formMessage, "message", text, type);
    }

    function setLookupMessage(text, type = "") {
      setInlineMessage(elements.lookupMessage, "message", text, type);
    }

    async function getReservationListData(page = state.adminReservationPage, size = state.adminReservationSize) {
      return getJson(`/admin/reservations?page=${page}&size=${size}`);
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
      return state.times.find((time) => time.id === state.selectedTimeId) || null;
    }

    function selectedAdminTheme() {
      return state.themes.find((theme) => theme.id === state.adminSelectedThemeId) || null;
    }

    function selectedAdminTime() {
      return state.times.find((time) => time.id === state.adminSelectedTimeId) || null;
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

    function reservationStatus(reservation) {
      return reservation.status || "CONFIRMED";
    }

    function statusLabel(status) {
      if (status === "WAITING") {
        return "대기";
      }
      if (status === "CANCELED") {
        return "취소";
      }
      return "확정";
    }

    function statusBadgeHtml(status, waitNumber) {
      const badgeClass = status === "WAITING" ? "waiting" : status === "CANCELED" ? "canceled" : "confirmed";
      const label = status === "WAITING" && waitNumber
        ? `${statusLabel(status)} ${waitNumber}번`
        : statusLabel(status);
      return `<span class="status-badge ${badgeClass}">${escapeHtml(label)}</span>`;
    }

    function isActiveReservation(reservation) {
      return reservationStatus(reservation) !== "CANCELED";
    }

    function isConfirmedReservation(reservation) {
      return reservationStatus(reservation) === "CONFIRMED";
    }

    function isSameSlot(reservation, date, timeId, themeId) {
      return reservation.date === date &&
        getReservationTimeId(reservation) === Number(timeId) &&
        getReservationThemeId(reservation) === Number(themeId);
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
          renderTimes();
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
          renderTimes();
        });
        elements.themeGrid.appendChild(button);
      });
    }

    function renderTimes() {
      elements.timeGrid.innerHTML = "";
      elements.timeCount.textContent = state.selectedThemeId ? `${state.times.length}개 시간` : "테마를 먼저 선택";

      if (!state.selectedThemeId) {
        elements.timeGrid.innerHTML = `<div class="empty">테마를 선택하면 시간 목록이 표시됩니다.</div>`;
        syncSummary();
        return;
      }

      if (state.times.length === 0) {
        elements.timeGrid.innerHTML = `<div class="empty">등록된 시간이 없습니다.</div>`;
        syncSummary();
        return;
      }

      [...state.times]
        .sort((a, b) => normalizeTime(a.startAt).localeCompare(normalizeTime(b.startAt)))
        .forEach((time) => {
          const button = document.createElement("button");
          button.type = "button";
          button.className = `time-button${time.id === state.selectedTimeId ? " selected" : ""}`;
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
      elements.dateNote.textContent = `${formatDate(elements.dateInput.value)} 기준으로 등록된 모든 예약 시간을 선택할 수 있습니다.`;
      elements.summaryDate.textContent = formatDate(elements.dateInput.value);
      elements.summaryTheme.textContent = theme ? theme.name : "-";
      elements.summaryTime.textContent = time ? normalizeTime(time.startAt) : "-";
      elements.summaryStatus.textContent = time ? "예약/대기 신청" : "-";

      const canReserve = Boolean(elements.nameInput.value.trim() && theme && time);
      elements.reserveButton.disabled = !canReserve;
      elements.reserveButton.textContent = "예약/대기 신청하기";
      setFormMessage(canReserve ? "" : "이름, 테마, 시간을 모두 선택하면 예약하거나 대기할 수 있습니다.");
    }

    function nextDemoWaitNumber(date, timeId, themeId, excludedId = null) {
      return state.demoReservations.filter((reservation) =>
        reservation.id !== excludedId &&
        isActiveReservation(reservation) &&
        reservationStatus(reservation) === "WAITING" &&
        isSameSlot(reservation, date, timeId, themeId)
      ).length + 1;
    }

    function hasDemoConfirmedReservation(date, timeId, themeId, excludedId = null) {
      return state.demoReservations.some((reservation) =>
        reservation.id !== excludedId &&
        isActiveReservation(reservation) &&
        isConfirmedReservation(reservation) &&
        isSameSlot(reservation, date, timeId, themeId)
      );
    }

    function hasDuplicateDemoReservation(guestName, date, timeId, themeId, excludedId = null) {
      return state.demoReservations.some((reservation) =>
        reservation.id !== excludedId &&
        isActiveReservation(reservation) &&
        reservation.guestName === guestName &&
        isSameSlot(reservation, date, timeId, themeId)
      );
    }

    function createDemoReservation(payload) {
      const status = hasDemoConfirmedReservation(payload.date, payload.timeId, payload.themeId)
        ? "WAITING"
        : "CONFIRMED";
      if (hasDuplicateDemoReservation(payload.guestName, payload.date, payload.timeId, payload.themeId)) {
        throw new Error("이미 같은 시간대에 대기 중입니다.");
      }

      const waitNumber = status === "WAITING"
        ? nextDemoWaitNumber(payload.date, payload.timeId, payload.themeId)
        : null;

      return {
        id: getNextId(state.demoReservations),
        guestName: payload.guestName,
        date: payload.date,
        themeId: payload.themeId,
        timeId: payload.timeId,
        status,
        ...(waitNumber ? { waitNumber } : {})
      };
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
        guestName: name,
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
          state.demoReservations = [...state.demoReservations, createdReservation];
        }
        state.reservations = [...state.reservations, createdReservation];

        const createdStatus = reservationStatus(createdReservation);
        const isWaiting = createdStatus === "WAITING";
        const waitNumber = createdReservation.waitNumber;
        showToast(
          isWaiting ? `${name}님의 대기 신청이 완료되었습니다.` : `${name}님의 예약이 완료되었습니다.`,
          `${formatDate(payload.date)} · ${theme.name} · ${normalizeTime(time.startAt)}${isWaiting && waitNumber ? ` · 대기 ${waitNumber}번` : ""}`
        );
        elements.nameInput.value = "";
        state.selectedTimeId = null;
        renderTimes();
        setFormMessage(isWaiting
          ? `대기 신청이 완료되었습니다.${waitNumber ? ` 현재 대기 ${waitNumber}번입니다.` : ""}`
          : "예약이 완료되었습니다.", "ok");
      } catch (error) {
        setFormMessage(endpointMessageOr(error, "예약 요청에 실패했습니다."), "error");
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
      setInlineMessage(elements.adminMessage, "admin-message", text, type);
    }

    function setAdminReserveMessage(text, type = "") {
      setInlineMessage(elements.adminReserveMessage, "admin-message", text, type);
    }

    function getReservationTheme(reservation) {
      return reservation.theme || state.themes.find((theme) => theme.id === reservation.themeId) || null;
    }

    function getReservationTime(reservation) {
      return reservation.time || state.times.find((time) => time.id === reservation.timeId) || null;
    }

    function getReservationThemeId(reservation) {
      return Number(reservation.themeId || reservation.theme?.id);
    }

    function getReservationTimeId(reservation) {
      return Number(reservation.timeId || reservation.time?.id);
    }

    function setEditReservationMessage(text, type = "") {
      setInlineMessage(elements.editReservationMessage, "message", text, type);
    }

    function setCancelReservationMessage(text, type = "") {
      setInlineMessage(elements.cancelReservationMessage, "message", text, type);
    }

    function syncEditReservationForm() {
      if (!elements.editReservationForm || elements.editReservationForm.hidden) {
        return;
      }

      const canEdit = Boolean(
        state.editingReservationId &&
        elements.editAuthorizationName.value.trim() &&
        elements.editReservationDate.value &&
        elements.editReservationTime.value &&
        !elements.editReservationTime.disabled
      );
      elements.editReservationButton.disabled = !canEdit;
    }

    function syncCancelReservationForm() {
      if (!elements.cancelReservationForm || elements.cancelReservationForm.hidden) {
        return;
      }

      const canCancel = Boolean(
        state.cancelingReservationId &&
        elements.cancelAuthorizationName.value.trim()
      );
      elements.cancelReservationButton.disabled = !canCancel;
    }

    function renderEditTimeOptions(times, selectedTimeId = null) {
      elements.editReservationTime.innerHTML = "";
      if (times.length === 0) {
        elements.editReservationTime.innerHTML = `<option value="">등록된 시간 없음</option>`;
        elements.editReservationTime.disabled = true;
        syncEditReservationForm();
        return;
      }

      elements.editReservationTime.disabled = false;
      elements.editReservationTime.innerHTML = `<option value="">시간 선택</option>`;

      [...times]
        .sort((a, b) => normalizeTime(a.startAt).localeCompare(normalizeTime(b.startAt)))
        .forEach((time) => {
          const option = document.createElement("option");
          option.value = time.id;
          option.textContent = normalizeTime(time.startAt);
          elements.editReservationTime.appendChild(option);
        });

      if (times.some((time) => time.id === selectedTimeId)) {
        elements.editReservationTime.value = String(selectedTimeId);
      }
      syncEditReservationForm();
    }

    function findReservation(id) {
      return [...state.lookupReservations, ...state.reservations, ...state.demoReservations]
        .find((reservation) => reservation.id === id) || null;
    }

    function clearEditReservation() {
      state.editingReservationId = null;
      state.editingReservationThemeId = null;
      elements.editReservationForm.hidden = true;
      elements.editReservationForm.reset();
      elements.editReservationMeta.textContent = "";
      elements.editReservationTime.disabled = false;
      setEditReservationMessage("");
    }

    function clearCancelReservation() {
      state.cancelingReservationId = null;
      elements.cancelReservationForm.hidden = true;
      elements.cancelReservationForm.reset();
      elements.cancelReservationMeta.textContent = "";
      setCancelReservationMessage("");
      syncCancelReservationForm();
    }

    async function startEditReservation(id) {
      const reservation = findReservation(id);
      if (!reservation) {
        return;
      }

      clearCancelReservation();
      const theme = getReservationTheme(reservation);
      const time = getReservationTime(reservation);
      state.editingReservationId = id;
      state.editingReservationThemeId = getReservationThemeId(reservation);
      elements.editReservationForm.hidden = false;
      elements.editReservationTitle.textContent = `예약 수정 #${id}`;
      elements.editReservationMeta.textContent = `${theme?.name || "-"} · ${normalizeTime(time?.startAt || "-")}`;
      elements.editAuthorizationName.value = "";
      elements.editReservationDate.value = reservation.date;
      renderEditTimeOptions(state.times, getReservationTimeId(reservation));
      setEditReservationMessage("");
      syncEditReservationForm();
      elements.editAuthorizationName.focus();
    }

    function startCancelReservation(id) {
      const reservation = findReservation(id);
      if (!reservation) {
        return;
      }

      clearEditReservation();
      const theme = getReservationTheme(reservation);
      const time = getReservationTime(reservation);
      state.cancelingReservationId = id;
      elements.cancelReservationForm.hidden = false;
      elements.cancelReservationTitle.textContent = `예약 취소 #${id}`;
      elements.cancelReservationMeta.textContent = `${theme?.name || "-"} · ${formatDate(reservation.date)} · ${normalizeTime(time?.startAt || "-")}`;
      elements.cancelAuthorizationName.value = "";
      setCancelReservationMessage("");
      syncCancelReservationForm();
      elements.cancelAuthorizationName.focus();
    }

    function renderLookupReservations(reservations) {
      state.lookupReservations = reservations;
      elements.lookupList.innerHTML = "";
      elements.lookupCount.textContent = `${reservations.length}건`;

      if (reservations.length === 0) {
        elements.lookupList.innerHTML = `<div class="empty">조회된 예약이 없습니다.</div>`;
        return;
      }

      [...reservations]
        .sort((a, b) => String(b.date).localeCompare(String(a.date)) || Number(b.id) - Number(a.id))
        .forEach((reservation) => {
          const theme = getReservationTheme(reservation);
          const time = getReservationTime(reservation);
          const status = reservationStatus(reservation);
          const waitNumber = reservation.waitNumber;
          const disabledActions = status === "CANCELED";
          const row = document.createElement("div");
          row.className = `list-row reservation-row ${status.toLowerCase()}`;
          row.innerHTML = `
            <div class="list-main">
              <span class="list-title">
                ${escapeHtml(reservation.guestName || "예약자")}
                ${statusBadgeHtml(status, waitNumber)}
              </span>
              <span class="list-meta">${escapeHtml(formatDate(reservation.date))} · ${escapeHtml(theme?.name || "-")} · ${escapeHtml(normalizeTime(time?.startAt || "-"))}</span>
            </div>
            <div class="row-actions">
              ${disabledActions ? "" : `<button class="secondary-button compact-button" type="button" data-edit-reservation-id="${reservation.id}">수정</button>`}
              ${disabledActions ? "" : `<button class="danger-button compact-button" type="button" data-cancel-reservation-id="${reservation.id}">취소</button>`}
            </div>
          `;
          elements.lookupList.appendChild(row);
        });
    }

    async function lookupReservations(event) {
      event.preventDefault();
      const guestName = elements.lookupGuestName.value.trim();
      if (!guestName) {
        setLookupMessage("예약자 이름을 입력해주세요.", "error");
        renderLookupReservations([]);
        clearEditReservation();
        clearCancelReservation();
        return;
      }

      elements.lookupButton.disabled = true;
      setLookupMessage("예약을 조회하는 중입니다.");
      clearEditReservation();
      clearCancelReservation();

      try {
        const reservations = state.mode === "live"
          ? (await getJson("/reservations/me", guestNameHeaders(guestName))).reservations || []
          : state.demoReservations.filter((reservation) => reservation.guestName === guestName);

        renderLookupReservations(reservations);
        setLookupMessage(reservations.length === 0 ? "조회된 예약이 없습니다." : "예약 조회가 완료되었습니다.",
          reservations.length === 0 ? "" : "ok");
      } catch (error) {
        renderLookupReservations([]);
        setLookupMessage(endpointMessageOr(error, "예약 조회에 실패했습니다."), "error");
      } finally {
        elements.lookupButton.disabled = false;
      }
    }

    function replaceReservation(reservations, editedReservation) {
      return reservations.map((reservation) =>
        reservation.id === editedReservation.id ? editedReservation : reservation
      );
    }

    function removeReservation(reservations, reservationId) {
      return reservations.filter((reservation) => reservation.id !== reservationId);
    }

    function editDemoReservation(id, payload, authorizationName) {
      const reservation = state.demoReservations.find((item) => item.id === id);
      if (!reservation) {
        throw new Error("존재하지 않는 예약입니다.");
      }
      if (reservation.guestName !== authorizationName) {
        throw new Error("본인의 예약만 수정할 수 있습니다.");
      }
      if (reservationStatus(reservation) === "CANCELED") {
        throw new Error("이미 취소된 예약입니다.");
      }

      const beforeDate = reservation.date;
      const beforeTimeId = getReservationTimeId(reservation);
      const themeId = getReservationThemeId(reservation);
      const wasConfirmed = isConfirmedReservation(reservation);
      const slotChanged = beforeDate !== payload.date || beforeTimeId !== payload.timeId;

      if (hasDuplicateDemoReservation(reservation.guestName, payload.date, payload.timeId, themeId, id)) {
        throw new Error("이미 존재하는 예약입니다.");
      }

      const status = hasDemoConfirmedReservation(payload.date, payload.timeId, themeId, id)
        ? "WAITING"
        : "CONFIRMED";
      const waitNumber = status === "WAITING"
        ? nextDemoWaitNumber(payload.date, payload.timeId, themeId, id)
        : null;

      reservation.date = payload.date;
      reservation.timeId = payload.timeId;
      reservation.status = status;
      if (waitNumber) {
        reservation.waitNumber = waitNumber;
      } else {
        delete reservation.waitNumber;
      }

      if (wasConfirmed && (slotChanged || status !== "CONFIRMED")) {
        promoteTopDemoWaiting(beforeDate, beforeTimeId, themeId);
      } else if (slotChanged) {
        refreshDemoWaitNumbers(beforeDate, beforeTimeId, themeId);
      }
      if (status === "WAITING") {
        refreshDemoWaitNumbers(payload.date, payload.timeId, themeId);
      }

      return reservation;
    }

    function demoWaitingReservationsForSlot(date, timeId, themeId) {
      return state.demoReservations
        .filter((reservation) =>
          isActiveReservation(reservation) &&
          reservationStatus(reservation) === "WAITING" &&
          isSameSlot(reservation, date, timeId, themeId)
        )
        .sort((a, b) => Number(a.id) - Number(b.id));
    }

    function refreshDemoWaitNumbers(date, timeId, themeId) {
      demoWaitingReservationsForSlot(date, timeId, themeId)
        .forEach((reservation, index) => {
          reservation.waitNumber = index + 1;
        });
    }

    function promoteTopDemoWaiting(date, timeId, themeId) {
      const [topWaiting] = demoWaitingReservationsForSlot(date, timeId, themeId);
      if (!topWaiting) {
        return;
      }

      topWaiting.status = "CONFIRMED";
      delete topWaiting.waitNumber;
      refreshDemoWaitNumbers(date, timeId, themeId);
    }

    function cancelDemoReservation(id, authorizationName) {
      const reservation = state.demoReservations.find((item) => item.id === id);
      if (!reservation) {
        throw new Error("존재하지 않는 예약입니다.");
      }
      if (reservation.guestName !== authorizationName) {
        throw new Error("본인의 예약만 취소할 수 있습니다.");
      }
      if (reservationStatus(reservation) === "CANCELED") {
        throw new Error("이미 취소된 예약입니다.");
      }

      const wasConfirmed = reservationStatus(reservation) === "CONFIRMED";
      const date = reservation.date;
      const timeId = getReservationTimeId(reservation);
      const themeId = getReservationThemeId(reservation);

      reservation.status = "CANCELED";
      delete reservation.waitNumber;

      if (wasConfirmed) {
        promoteTopDemoWaiting(date, timeId, themeId);
        return;
      }
      refreshDemoWaitNumbers(date, timeId, themeId);
    }

    async function submitCancelReservation(event) {
      event.preventDefault();
      const reservationId = state.cancelingReservationId;
      const authorizationName = elements.cancelAuthorizationName.value.trim();
      if (!reservationId || !authorizationName) {
        setCancelReservationMessage("예약자 이름을 입력해주세요.", "error");
        syncCancelReservationForm();
        return;
      }

      elements.cancelReservationButton.disabled = true;
      setCancelReservationMessage("예약을 취소하는 중입니다.");

      try {
        let reservations = [];
        if (state.mode === "live") {
          await deleteJson(`/reservations/${reservationId}`, guestNameHeaders(authorizationName));
          reservations = (await getJson("/reservations/me", guestNameHeaders(authorizationName))).reservations || [];
        } else {
          cancelDemoReservation(reservationId, authorizationName);
          reservations = state.demoReservations.filter((reservation) => reservation.guestName === authorizationName);
        }

        state.lookupReservations = reservations;
        renderLookupReservations(state.lookupReservations);
        clearCancelReservation();
        showToast("예약이 취소되었습니다.", authorizationName);
        renderTimes();
        setLookupMessage("예약 취소가 완료되었습니다.", "ok");
      } catch (error) {
        setCancelReservationMessage(endpointMessageOr(error, "예약 취소에 실패했습니다."), "error");
        syncCancelReservationForm();
      }
    }

    async function editReservation(event) {
      event.preventDefault();
      const reservationId = state.editingReservationId;
      const authorizationName = elements.editAuthorizationName.value.trim();
      const payload = {
        date: elements.editReservationDate.value,
        timeId: Number(elements.editReservationTime.value)
      };

      if (!reservationId || !authorizationName || !payload.date || !payload.timeId) {
        setEditReservationMessage("이름, 날짜, 시간을 모두 입력해주세요.", "error");
        syncEditReservationForm();
        return;
      }

      elements.editReservationButton.disabled = true;
      setEditReservationMessage("예약을 수정하는 중입니다.");

      try {
        let reservations = [];
        let editedReservation = null;
        if (state.mode === "live") {
          await patchJson(`/reservations/${reservationId}`, payload, guestNameHeaders(authorizationName));
          reservations = (await getJson("/reservations/me", guestNameHeaders(authorizationName))).reservations || [];
          editedReservation = reservations.find((reservation) => reservation.id === reservationId) || null;
        } else {
          editedReservation = editDemoReservation(reservationId, payload, authorizationName);
          state.demoReservations = replaceReservation(state.demoReservations, editedReservation);
          reservations = state.demoReservations.filter((reservation) => reservation.guestName === authorizationName);
        }
        if (editedReservation) {
          state.reservations = replaceReservation(state.reservations, editedReservation);
        }
        state.lookupReservations = reservations;

        renderLookupReservations(state.lookupReservations);
        clearEditReservation();
        showToast("예약이 수정되었습니다.", `${formatDate(payload.date)} · ${normalizeTime(getReservationTime(editedReservation || { timeId: payload.timeId })?.startAt || "")}`);
        renderTimes();
        setLookupMessage("예약 수정이 완료되었습니다.", "ok");
      } catch (error) {
        setEditReservationMessage(endpointMessageOr(error, "예약 수정에 실패했습니다."), "error");
        syncEditReservationForm();
      }
    }

    function renderAdmin() {
      const rangeLabel = adminReservationRangeLabel();
      elements.reservationCount.textContent =
        `GET /admin/reservations?page=${state.adminReservationPage}&size=${state.adminReservationSize} · ${rangeLabel} / ${state.adminReservationTotalElements}건`;
      elements.adminThemeCount.textContent = `${state.themes.length}개`;
      elements.adminTimeCount.textContent = `${state.times.length}개`;
      renderAdminReservationForm();
      renderAdminReservations();
      renderAdminReservationPagination();
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

      if (state.times.length === 0) {
        elements.adminReserveTimeGrid.innerHTML = `<div class="empty">등록된 시간이 없습니다.</div>`;
        syncAdminReserveSummary();
        return;
      }

      [...state.times]
        .sort((a, b) => normalizeTime(a.startAt).localeCompare(normalizeTime(b.startAt)))
        .forEach((time) => {
          const button = document.createElement("button");
          button.type = "button";
          button.className = `time-button${time.id === state.adminSelectedTimeId ? " selected" : ""}`;
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

      state.reservations.forEach((reservation) => {
          const theme = getReservationTheme(reservation);
          const time = getReservationTime(reservation);
          const row = document.createElement("div");
          row.className = "list-row";
          row.innerHTML = `
            <div class="list-main">
              <span class="list-title">${escapeHtml(reservation.guestName || "예약자")}</span>
              <span class="list-meta">${escapeHtml(formatDate(reservation.date))} · ${escapeHtml(theme?.name || "-")} · ${escapeHtml(normalizeTime(time?.startAt || "-"))}</span>
            </div>
            <button class="danger-button" type="button" data-delete-reservation-id="${reservation.id}">삭제</button>
          `;
          elements.adminReservationList.appendChild(row);
        });
    }

    function renderAdminReservationPagination() {
      elements.adminReservationPageSize.value = String(state.adminReservationSize);
      elements.adminReservationPageLabel.textContent =
        `${state.adminReservationTotalPages === 0 ? 0 : state.adminReservationPage} / ${state.adminReservationTotalPages} 페이지`;
      elements.adminReservationPrevPage.disabled = !state.adminReservationHasPrevious;
      elements.adminReservationNextPage.disabled = !state.adminReservationHasNext;
    }

    function adminReservationRangeLabel() {
      if (state.adminReservationNumberOfElements === 0) {
        return "0-0";
      }

      const start = (state.adminReservationPage - 1) * state.adminReservationSize + 1;
      const end = start + state.adminReservationNumberOfElements - 1;
      return `${start}-${end}`;
    }

    function applyAdminReservationPage(pageData) {
      const contents = Array.isArray(pageData?.contents) ? pageData.contents : [];
      state.reservations = contents;
      state.adminReservationPage = Number(pageData?.page ?? state.adminReservationPage);
      state.adminReservationSize = Number(pageData?.size ?? state.adminReservationSize);
      state.adminReservationNumberOfElements = Number(pageData?.numberOfElements ?? contents.length);
      state.adminReservationTotalElements = Number(pageData?.totalElements ?? contents.length);
      state.adminReservationTotalPages = Number(pageData?.totalPages ?? 0);
      state.adminReservationHasPrevious = Boolean(pageData?.hasPrevious);
      state.adminReservationHasNext = Boolean(pageData?.hasNext);

      if (state.adminReservationTotalPages === 0) {
        state.adminReservationPage = 1;
      }
    }

    function demoReservationPage() {
      const reservations = [...state.demoReservations]
        .sort((a, b) => Number(a.id) - Number(b.id));
      const totalElements = reservations.length;
      const totalPages = Math.ceil(totalElements / state.adminReservationSize);
      if (totalPages > 0 && state.adminReservationPage > totalPages) {
        state.adminReservationPage = totalPages;
      }

      const start = (state.adminReservationPage - 1) * state.adminReservationSize;
      const end = start + state.adminReservationSize;
      const contents = reservations.slice(start, end);

      return {
        contents,
        page: state.adminReservationPage,
        size: state.adminReservationSize,
        numberOfElements: contents.length,
        totalElements,
        totalPages,
        hasPrevious: state.adminReservationPage > 1,
        hasNext: state.adminReservationPage < totalPages
      };
    }

    async function loadAdminReservations() {
      if (!isAdminPage()) {
        return;
      }

      if (state.mode === "live") {
        const data = await getReservationListData();
        applyAdminReservationPage(data);
        if (state.adminReservationTotalPages > 0 && state.adminReservationPage > state.adminReservationTotalPages) {
          state.adminReservationPage = state.adminReservationTotalPages;
          applyAdminReservationPage(await getReservationListData());
        }
        return;
      }

      applyAdminReservationPage(demoReservationPage());
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
      await loadAdminReservations();
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
        guestName: name,
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
          state.demoReservations = [...state.demoReservations, createdReservation];
        }
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
        if (state.adminSelectedTimeId === id) {
          state.adminSelectedTimeId = null;
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
      state.adminReservationPage = 1;
      applyAdminReservationPage(demoReservationPage());
      setSourceStatus();

      if (isAdminPage()) {
        state.adminSelectedThemeId = state.themes[0]?.id || null;
        state.adminSelectedTimeId = null;
        elements.adminReserveDate.value = DEFAULT_DATE;
        renderAdmin();
        return;
      }

      state.selectedThemeId = state.themes[0]?.id || null;
      state.selectedTimeId = null;
      elements.dateInput.value = DEFAULT_DATE;
      renderPopularThemes();
      renderThemes();
      renderTimes();
      syncSummary();
      renderLookupReservations([]);
    }

    async function loadInitialData() {
      const isFilePreview = window.location.protocol === "file:";
      if (isFilePreview) {
        renderDemoFirst();
        return;
      } else {
        if (isAdminPage()) {
          state.adminReservationPage = 1;
          elements.adminReserveDate.value = DEFAULT_DATE;
        } else {
          elements.dateInput.value = DEFAULT_DATE;
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
              getReservationListData(1, state.adminReservationSize)
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
        if (isAdminPage()) {
          applyAdminReservationPage(reservationData);
        } else {
          state.reservations = [];
        }
        setSourceStatus();

        if (isAdminPage()) {
          state.adminSelectedThemeId = state.themes[0]?.id || null;
          state.adminSelectedTimeId = null;
          renderAdmin();
          return;
        }

        state.selectedThemeId = state.themes[0]?.id || null;
        state.selectedTimeId = null;
        renderPopularThemes();
        renderThemes();
        renderTimes();
        syncSummary();
        renderLookupReservations([]);
      } catch (error) {
        state.mode = "demo";
        renderDemoFirst();
        setSourceStatus();
      }
    }

    if (isUserPage()) {
      elements.dateInput.addEventListener("change", () => {
        state.selectedTimeId = null;
        renderTimes();
      });
      elements.nameInput.addEventListener("input", syncSummary);
      elements.reserveButton.addEventListener("click", reserve);
      elements.lookupForm.addEventListener("submit", lookupReservations);
      elements.lookupList.addEventListener("click", (event) => {
        const editButton = event.target.closest("[data-edit-reservation-id]");
        if (editButton) {
          startEditReservation(Number(editButton.dataset.editReservationId));
          return;
        }

        const cancelButton = event.target.closest("[data-cancel-reservation-id]");
        if (cancelButton) {
          startCancelReservation(Number(cancelButton.dataset.cancelReservationId));
        }
      });
      elements.editReservationForm.addEventListener("submit", editReservation);
      elements.editCancelButton.addEventListener("click", clearEditReservation);
      elements.editAuthorizationName.addEventListener("input", syncEditReservationForm);
      elements.editReservationDate.addEventListener("change", syncEditReservationForm);
      elements.editReservationTime.addEventListener("change", syncEditReservationForm);
      elements.cancelReservationForm.addEventListener("submit", submitCancelReservation);
      elements.cancelReservationCloseButton.addEventListener("click", clearCancelReservation);
      elements.cancelAuthorizationName.addEventListener("input", syncCancelReservationForm);
    }

    if (isAdminPage()) {
      elements.adminReservationForm.addEventListener("submit", createAdminReservation);
      elements.adminReserveName.addEventListener("input", syncAdminReserveSummary);
      elements.adminReserveDate.addEventListener("change", () => {
        state.adminSelectedTimeId = null;
        renderAdminReserveTimes();
      });
      elements.adminReserveTheme.addEventListener("change", () => {
        state.adminSelectedThemeId = Number(elements.adminReserveTheme.value) || null;
        state.adminSelectedTimeId = null;
        renderAdminReserveTimes();
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
      elements.adminReservationPageSize.addEventListener("change", async () => {
        state.adminReservationSize = Number(elements.adminReservationPageSize.value);
        state.adminReservationPage = 1;
        await loadAdminReservations();
        renderAdmin();
      });
      elements.adminReservationPrevPage.addEventListener("click", async () => {
        if (!state.adminReservationHasPrevious) {
          return;
        }
        state.adminReservationPage -= 1;
        await loadAdminReservations();
        renderAdmin();
      });
      elements.adminReservationNextPage.addEventListener("click", async () => {
        if (!state.adminReservationHasNext) {
          return;
        }
        state.adminReservationPage += 1;
        await loadAdminReservations();
        renderAdmin();
      });
    }

    if (elements.errorPopup && elements.errorPopupClose) {
      elements.errorPopupClose.addEventListener("click", hideErrorPopup);
      elements.errorPopup.addEventListener("click", (event) => {
        if (event.target === elements.errorPopup) {
          hideErrorPopup();
        }
      });
      document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
          hideErrorPopup();
        }
      });
    }

    loadInitialData();
