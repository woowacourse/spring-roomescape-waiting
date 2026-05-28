const state = {
  themes: [],
  rankingThemes: [],
  selectedThemeId: null,
  selectedDate: formatDate(new Date()),
  selectedTimeId: null,
  availableTimes: [],
};

const elements = {
  themeList: document.querySelector("#theme-list"),
  rankingList: document.querySelector("#ranking-list"),
  rankingPeriod: document.querySelector("#ranking-period"),
  selectedTheme: document.querySelector("#selected-theme"),
  dateInput: document.querySelector("#reservation-date"),
  timeList: document.querySelector("#time-list"),
  form: document.querySelector("#reservation-form"),
  nameInput: document.querySelector("#reservation-name"),
  submitButton: document.querySelector("#submit-button"),
  result: document.querySelector("#reservation-result"),
  searchForm: document.querySelector("#reservation-search-form"),
  searchNameInput: document.querySelector("#reservation-search-name"),
  searchResult: document.querySelector("#reservation-search-result"),
  toast: document.querySelector("#toast"),
};

document.addEventListener("DOMContentLoaded", initialize);

async function initialize() {
  elements.dateInput.value = state.selectedDate;
  elements.dateInput.min = formatDate(new Date());
  bindEvents();
  await loadThemes();
  await loadRankingThemes();
  await loadAvailableTimes();
}

function bindEvents() {
  elements.dateInput.addEventListener("change", handleDateChange);
  elements.form.addEventListener("submit", handleReservationSubmit);
  elements.searchForm.addEventListener("submit", handleReservationSearch);
}

// ── 테마 ───────────────────────────────────────────────────

async function loadThemes() {
  renderLoading(elements.themeList, "테마를 불러오는 중입니다.");
  state.themes = await requestJson("/themes");
  state.selectedThemeId = state.themes[0]?.id ?? null;
  renderThemes();
}

function renderThemes() {
  renderThemeCards(elements.themeList, state.themes, handleThemeSelect);
  renderSelectedTheme();
}

function renderSelectedTheme() {
  const theme = state.themes.find((t) => t.id === state.selectedThemeId);
  elements.selectedTheme.textContent = theme ? theme.name : "테마를 선택하세요";
}

function renderThemeCards(container, themes, clickHandler) {
  container.innerHTML = "";
  if (themes.length === 0) {
    renderEmpty(container, "표시할 테마가 없습니다.");
    return;
  }
  themes.forEach((theme) => container.appendChild(createThemeCard(theme, clickHandler)));
}

function createThemeCard(theme, clickHandler) {
  const card = document.createElement("button");
  card.type = "button";
  card.className = theme.id === state.selectedThemeId ? "theme-card is-selected" : "theme-card";
  card.addEventListener("click", () => clickHandler(theme.id));
  card.append(createThemeImage(theme), createThemeContent(theme));
  return card;
}

function createThemeImage(theme) {
  const wrapper = document.createElement("div");
  wrapper.className = "theme-image";
  const img = document.createElement("img");
  img.src = theme.thumbnailUrl;
  img.alt = theme.name;
  img.addEventListener("error", () => { wrapper.removeChild(img); wrapper.textContent = theme.name.slice(0, 1); });
  wrapper.appendChild(img);
  return wrapper;
}

function createThemeContent(theme) {
  const content = document.createElement("div");
  content.className = "theme-content";
  const title = document.createElement("h3");
  title.textContent = theme.name;
  const desc = document.createElement("p");
  desc.textContent = theme.description;
  const label = document.createElement("span");
  label.className = "select-label";
  label.textContent = theme.id === state.selectedThemeId ? "선택됨" : "선택하기";
  content.append(title, desc, label);
  return content;
}

async function handleThemeSelect(themeId) {
  state.selectedThemeId = themeId;
  renderThemes();
  renderRankingThemes();
  await loadAvailableTimes();
}

// ── 인기 테마 ───────────────────────────────────────────────

async function loadRankingThemes() {
  const period = recentWeekPeriod();
  elements.rankingPeriod.textContent = `${period.startDate} ~ ${period.endDate}`;
  renderLoading(elements.rankingList, "인기 테마를 불러오는 중입니다.");
  state.rankingThemes = await requestJson(rankingUrl(period));
  renderRankingThemes();
}

function renderRankingThemes() {
  renderThemeCards(elements.rankingList, state.rankingThemes, handleThemeSelect);
}

// ── 예약 가능 시간 ──────────────────────────────────────────

async function loadAvailableTimes() {
  if (!state.selectedThemeId) {
    state.availableTimes = [];
    renderEmpty(elements.timeList, "테마를 먼저 선택하세요.");
    updateSubmitButton();
    return;
  }
  state.selectedTimeId = null;
  updateSubmitButton();
  renderLoading(elements.timeList, "시간을 불러오는 중입니다.");
  state.availableTimes = await requestJson(availableTimeUrl());
  renderAvailableTimes();
  updateSubmitButton();
}

function renderAvailableTimes() {
  elements.timeList.innerHTML = "";
  if (state.availableTimes.length === 0) {
    renderEmpty(elements.timeList, "선택 가능한 시간이 없습니다.");
    return;
  }
  state.availableTimes.forEach((t) => elements.timeList.appendChild(createTimeButton(t)));
}

function createTimeButton(timeAvailability) {
  const btn = document.createElement("button");
  btn.type = "button";
  const past = isPastDateTime(state.selectedDate, timeAvailability.time.startAt);
  const waitMode = !timeAvailability.available && !past;
  const selected = timeAvailability.time.id === state.selectedTimeId;

  let cls = "time-button";
  if (waitMode) cls += " is-waitlisted";
  if (selected) cls += " is-selected";
  btn.className = cls;
  btn.disabled = past;
  btn.textContent = waitMode
    ? `${formatStartAt(timeAvailability.time.startAt)} 대기`
    : formatStartAt(timeAvailability.time.startAt);

  btn.addEventListener("click", () => {
    state.selectedTimeId = timeAvailability.time.id;
    renderAvailableTimes();
    updateSubmitButton();
  });
  return btn;
}

async function handleDateChange(event) {
  state.selectedDate = event.target.value;
  await loadAvailableTimes();
}

// ── 예약 생성 ───────────────────────────────────────────────

async function handleReservationSubmit(event) {
  event.preventDefault();
  const reservation = await createReservation();
  renderReservationResult(reservation);
  const isWaiting = reservation.status === "WAITING";
  showToast(isWaiting ? `대기 ${reservation.order}번으로 등록되었습니다.` : "예약이 확정되었습니다!");
  await loadAvailableTimes();
}

async function createReservation() {
  return requestJson("/reservations", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      name: elements.nameInput.value.trim(),
      date: state.selectedDate,
      timeId: state.selectedTimeId,
      themeId: state.selectedThemeId,
    }),
  });
}

function renderReservationResult(reservation) {
  const isWaiting = reservation.status === "WAITING";
  elements.result.hidden = false;
  elements.result.innerHTML = `
    <span class="result-status ${isWaiting ? "is-waiting" : "is-confirmed"}">
      ${isWaiting ? `대기 ${reservation.order}번` : "예약 확정"}
    </span>
    <p><strong>${escapeHtml(reservation.theme.name)}</strong></p>
    <p>예약자 &nbsp;${escapeHtml(reservation.name)}</p>
    <p>날짜 &nbsp;${reservation.date} &nbsp;${formatStartAt(reservation.time.startAt)}</p>
    ${isWaiting ? `<p style="color:var(--warn);font-size:12px;margin-top:8px">앞 예약이 취소되면 자동으로 확정됩니다.</p>` : ""}
  `;
}

// ── 내 예약 조회 ────────────────────────────────────────────

async function handleReservationSearch(event) {
  event.preventDefault();
  const name = elements.searchNameInput.value.trim();
  renderLoading(elements.searchResult, "예약을 조회하는 중입니다.");
  const reservations = await requestJson(`/reservations?name=${encodeURIComponent(name)}`);
  renderSearchResult(reservations);
}

function renderSearchResult(reservations) {
  elements.searchResult.innerHTML = "";
  if (reservations.length === 0) {
    renderEmpty(elements.searchResult, "조회된 예약이 없습니다.");
    return;
  }
  reservations.forEach((r) => elements.searchResult.appendChild(createReservationItem(r)));
}

function createReservationItem(reservation) {
  const item = document.createElement("article");
  item.className = "reservation-item";
  item.append(createReservationSummary(reservation), createReservationActions(reservation));
  return item;
}

function createReservationSummary(reservation) {
  const summary = document.createElement("div");
  summary.className = "reservation-summary";

  const titleRow = document.createElement("div");
  titleRow.className = "reservation-title-row";
  const strong = document.createElement("strong");
  strong.textContent = reservation.theme.name;
  titleRow.append(strong, createStatusPill(reservation));
  summary.appendChild(titleRow);

  const meta = document.createElement("div");
  meta.className = "reservation-meta";
  meta.append(
    createSpan(`예약자  ${reservation.name}`),
    createSpan(`${reservation.date}  ${formatStartAt(reservation.time.startAt)}`),
  );
  summary.appendChild(meta);

  return summary;
}

function createStatusPill(reservation) {
  const pill = document.createElement("span");
  if (isPastReservation(reservation)) {
    pill.className = "status-pill is-past";
    pill.textContent = "지난 예약";
    return pill;
  }
  if (reservation.status === "WAITING") {
    pill.className = "status-pill is-waiting";
    pill.textContent = `대기 ${reservation.order}번`;
  } else {
    pill.className = "status-pill is-confirmed";
    pill.textContent = "예약 확정";
  }
  return pill;
}

function createReservationActions(reservation) {
  const actions = document.createElement("div");
  actions.className = "reservation-actions";
  const past = isPastReservation(reservation);
  const btn = document.createElement("button");
  btn.type = "button";
  btn.className = "danger-button";
  btn.textContent = reservation.status === "WAITING" ? "대기 취소" : "예약 취소";
  btn.disabled = past;
  btn.title = past ? "지난 예약은 취소할 수 없습니다." : "";
  btn.addEventListener("click", () => confirmCancelReservation(reservation));
  actions.appendChild(btn);
  return actions;
}

async function confirmCancelReservation(reservation) {
  if (isPastReservation(reservation)) {
    showToast("지난 예약은 취소할 수 없습니다.", true);
    return;
  }
  const type = reservation.status === "WAITING" ? "대기" : "예약";
  const msg = `[${reservation.theme.name} / ${reservation.date} / ${formatStartAt(reservation.time.startAt)}] ${type}를 취소하시겠습니까?`;
  if (!window.confirm(msg)) return;
  await cancelReservation(reservation);
}

async function cancelReservation(reservation) {
  const url = reservation.status === "WAITING"
    ? `/reservations/waits/${reservation.id}`
    : `/reservations/${reservation.id}`;
  await request(url, { method: "DELETE" });
  showToast(`${reservation.status === "WAITING" ? "대기가" : "예약이"} 취소되었습니다.`);
  await refreshReservationSearch();
  await loadAvailableTimes();
}

async function refreshReservationSearch() {
  const name = elements.searchNameInput.value.trim();
  if (!name) { elements.searchResult.innerHTML = ""; return; }
  const reservations = await requestJson(`/reservations?name=${encodeURIComponent(name)}`);
  renderSearchResult(reservations);
}

// ── UI 유틸 ─────────────────────────────────────────────────

function updateSubmitButton() {
  const canSubmit = !!(state.selectedThemeId && state.selectedDate && state.selectedTimeId);
  elements.submitButton.disabled = !canSubmit;
  if (canSubmit) {
    const time = state.availableTimes.find((t) => t.time.id === state.selectedTimeId);
    elements.submitButton.textContent = (time && !time.available) ? "대기 신청" : "예약하기";
  } else {
    elements.submitButton.textContent = "예약하기";
  }
}

function renderLoading(container, message) {
  container.innerHTML = `<div class="loading">${message}</div>`;
}

function renderEmpty(container, message) {
  container.innerHTML = `<div class="empty">${message}</div>`;
}

function showToast(message, error = false) {
  elements.toast.textContent = message;
  elements.toast.className = error ? "toast is-error" : "toast";
  elements.toast.hidden = false;
  clearTimeout(elements.toast._timer);
  elements.toast._timer = setTimeout(() => { elements.toast.hidden = true; }, 2800);
}

function createSpan(text) {
  const el = document.createElement("span");
  el.textContent = text;
  return el;
}

// ── 날짜/시간 유틸 ───────────────────────────────────────────

function isSelectableTime(date, timeAvailability) {
  return !isPastDateTime(date, timeAvailability.time.startAt);
}

function isPastReservation(reservation) {
  return isPastDateTime(reservation.date, reservation.time.startAt);
}

function isPastDateTime(date, startAt) {
  return new Date(`${date}T${formatStartAt(startAt)}:00`) < new Date();
}

function formatDate(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

function formatStartAt(startAt) {
  return startAt.slice(0, 5);
}

function addDays(date, days) {
  const d = new Date(date);
  d.setDate(d.getDate() + days);
  return d;
}

function recentWeekPeriod() {
  const end = addDays(new Date(), -1);
  const start = addDays(end, -6);
  return { startDate: formatDate(start), endDate: formatDate(end) };
}

function rankingUrl(period) {
  return `/themes/ranking?start-date=${period.startDate}&end-date=${period.endDate}`;
}

function availableTimeUrl() {
  return `/times/available?date=${state.selectedDate}&themeId=${state.selectedThemeId}`;
}

// ── HTTP 유틸 ────────────────────────────────────────────────

async function requestJson(url, options = {}) {
  return (await request(url, options)).json();
}

async function request(url, options = {}) {
  const response = await fetch(url, options);
  if (response.ok) return response;
  const error = await response.json().catch(() => ({ message: "요청을 처리하지 못했습니다." }));
  showToast(error.message, true);
  throw new Error(error.message);
}

function escapeHtml(value) {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}