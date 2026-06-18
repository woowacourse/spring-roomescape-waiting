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
  myReservationList: document.querySelector("#mine-reservation-list"),
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
  await loadMyReservations();
}

function bindEvents() {
  elements.dateInput.addEventListener("change", handleDateChange);
  elements.form.addEventListener("submit", handleReservationSubmit);
  elements.searchForm.addEventListener("submit", handleReservationSearch);
}

async function loadThemes() {
  renderLoading(elements.themeList, "테마를 불러오는 중입니다.");
  state.themes = (await requestJson("/themes")).items;
  state.selectedThemeId = firstThemeId();
  renderThemes();
}

async function loadRankingThemes() {
  const period = recentWeekPeriod();
  elements.rankingPeriod.textContent = `${period.startDate} - ${period.endDate}`;
  renderLoading(elements.rankingList, "인기 테마를 불러오는 중입니다.");
  state.rankingThemes = (await requestJson(rankingUrl(period))).items;
  renderRankingThemes();
}

async function loadAvailableTimes() {
  if (!state.selectedThemeId) {
    state.availableTimes = [];
    renderEmpty(elements.timeList, "테마를 먼저 선택하세요.");
    updateSubmitButton();
    return;
  }
  resetSelectedTime();
  renderLoading(elements.timeList, "시간을 불러오는 중입니다.");
  updateSubmitButton();
  state.availableTimes = (await requestJson(availableTimeUrl())).items;
  renderAvailableTimes();
  updateSubmitButton();
}

function firstThemeId() {
  const firstTheme = state.themes.at(0);
  if (firstTheme) {
    return firstTheme.id;
  }
  return null;
}

function renderThemes() {
  renderThemeCards(elements.themeList, state.themes, handleThemeSelect);
  renderSelectedTheme();
}

function renderRankingThemes() {
  renderThemeCards(elements.rankingList, state.rankingThemes, handleThemeSelect);
}

function renderSelectedTheme() {
  const theme = selectedTheme();
  if (theme) {
    elements.selectedTheme.textContent = theme.name;
    return;
  }
  elements.selectedTheme.textContent = "테마를 선택하세요";
}

function selectedTheme() {
  return state.themes.find((theme) => theme.id === state.selectedThemeId);
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
  card.className = themeCardClassName(theme);
  card.addEventListener("click", () => clickHandler(theme.id));
  card.append(createThemeImage(theme), createThemeContent(theme));
  return card;
}

function themeCardClassName(theme) {
  if (theme.id === state.selectedThemeId) {
    return "theme-card is-selected";
  }
  return "theme-card";
}

function createThemeImage(theme) {
  const wrapper = document.createElement("div");
  wrapper.className = "theme-image";
  const image = document.createElement("img");
  image.src = theme.thumbnailUrl;
  image.alt = theme.name;
  image.addEventListener("error", () => replaceBrokenImage(wrapper, theme));
  wrapper.appendChild(image);
  return wrapper;
}

function replaceBrokenImage(wrapper, theme) {
  wrapper.textContent = theme.name.slice(0, 1);
}

function createThemeContent(theme) {
  const content = document.createElement("div");
  content.className = "theme-content";
  content.append(createTextElement("h3", theme.name));
  content.append(createTextElement("p", theme.description));
  content.append(createTextElement("span", selectLabel(theme)));
  content.lastElementChild.className = "select-label";
  return content;
}

function selectLabel(theme) {
  if (theme.id === state.selectedThemeId) {
    return "선택됨";
  }
  return "선택하기";
}

async function handleThemeSelect(themeId) {
  state.selectedThemeId = themeId;
  renderThemes();
  renderRankingThemes();
  await loadAvailableTimes();
}

async function handleDateChange(event) {
  state.selectedDate = event.target.value;
  await loadAvailableTimes();
}

function renderAvailableTimes() {
  elements.timeList.innerHTML = "";
  if (state.availableTimes.length === 0) {
    renderEmpty(elements.timeList, "선택 가능한 시간이 없습니다.");
    return;
  }
  state.availableTimes.forEach((time) => elements.timeList.appendChild(createTimeButton(time)));
}

function createTimeButton(timeAvailability) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = timeButtonClassName(timeAvailability);
  button.disabled = !isSelectableTime(state.selectedDate, timeAvailability);
  button.textContent = formatStartAt(timeAvailability.time.startAt);
  button.addEventListener("click", () => handleTimeSelect(timeAvailability.time.id));
  return button;
}

function timeButtonClassName(timeAvailability) {
  if (timeAvailability.time.id === state.selectedTimeId) {
    return "time-button is-selected";
  }
  return "time-button";
}

function handleTimeSelect(timeId) {
  state.selectedTimeId = timeId;
  renderAvailableTimes();
  updateSubmitButton();
}

async function handleReservationSubmit(event) {
  event.preventDefault();
  const result = await createReservation();
  renderReservationResult(result);
  const message = result.status === "WAITING" ? "대기가 신청되었습니다." : "예약이 완료되었습니다.";
  showToast(message);
  await loadAvailableTimes();
}

async function loadMyReservations() {
  renderLoading(elements.myReservationList, "내 예약을 불러오는 중입니다.");
  const response = await requestJson("/reservations/mine", {headers: {"Member-Id": "1"}});
  renderMyReservations(response);
}

function renderMyReservations(response) {
  elements.myReservationList.innerHTML = "";
  const reservations = response.reservations.items;
  const waits = response.waits.items;
  const all = [...reservations, ...waits];

  if (all.length === 0) {
    renderEmpty(elements.myReservationList, "예약 내역이 없습니다.");
    return;
  }
  all.forEach((item) => {
    elements.myReservationList.appendChild(createReservationItem(item));
  });
}

async function handleReservationSearch(event) {
  event.preventDefault();
  renderLoading(elements.searchResult, "예약을 조회하는 중입니다.");
  const name = elements.searchNameInput.value.trim();
  const response = await requestJson(`/reservations?name=${encodeURIComponent(name)}`);
  renderSearchResult(response);
}

function renderSearchResult(response) {
  elements.searchResult.innerHTML = "";
  const reservations = response.reservations.items;
  const waits = response.waits.items;
  const all = [...reservations, ...waits];

  if (all.length === 0) {
    renderEmpty(elements.searchResult, "조회된 예약이 없습니다.");
    return;
  }
  all.forEach((item) => {
    elements.searchResult.appendChild(createReservationItem(item));
  });
}

function createReservationItem(item) {
  const article = document.createElement("article");
  article.className = "reservation-item";
  article.append(createReservationSummary(item), createReservationActions(item));
  return article;
}

function createReservationSummary(item) {
  const summary = document.createElement("div");
  summary.className = "reservation-summary";
  summary.append(createTextElement("strong", item.theme.name));
  summary.append(createMetaText("예약자", item.member.name));
  summary.append(createMetaText("날짜", item.date));
  summary.append(createMetaText("시간", formatStartAt(item.time.startAt)));
  if (item.status === "WAITING") {
    summary.append(createMetaText("상태", `대기 ${item.order}번`));
  } else {
    summary.append(createMetaText("상태", "예약 확정"));
  }
  if (isPastReservation(item)) {
    summary.append(createPastBadge());
  }
  return summary;
}

function createMetaText(label, value) {
  const element = document.createElement("span");
  element.textContent = `${label}: ${value}`;
  return element;
}

function createReservationActions(item) {
  const actions = document.createElement("div");
  actions.className = "reservation-actions";
  actions.append(createCancelButton(item));
  return actions;
}

function createCancelButton(item) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = "danger-button";
  button.textContent = item.status === "WAITING" ? "대기 취소" : "취소";
  button.disabled = isPastReservation(item);
  button.title = pastReservationTitle(item);
  button.addEventListener("click", () => confirmCancelReservation(item));
  return button;
}

async function confirmCancelReservation(item) {
  if (isPastReservation(item)) {
    const msg = item.status === "WAITING" ? "지난 대기는 취소할 수 없습니다." : "지난 예약은 취소할 수 없습니다.";
    showToast(msg, true);
    return;
  }
  if (window.confirm(cancelConfirmMessage(item))) {
    await cancelReservation(item);
  }
}

function cancelConfirmMessage(item) {
  const themeName = item.theme.name;
  const startAt = formatStartAt(item.time.startAt);
  const label = item.status === "WAITING" ? "대기" : "예약";
  return `[${themeName} / ${item.date} / ${startAt}] ${label}을 취소하시겠습니까?`;
}

async function cancelReservation(item) {
  const url = item.status === "WAITING"
    ? `/reservations/waits/${item.id}`
    : `/reservations/${item.id}`;
  await request(url, {method: "DELETE"});
  const message = item.status === "WAITING" ? "대기가 취소되었습니다." : "예약이 취소되었습니다.";
  showToast(message);
  await loadMyReservations();
  await refreshReservationSearch();
  await loadAvailableTimes();
}

async function refreshReservationSearch() {
  const name = elements.searchNameInput.value.trim();
  if (name === "") {
    elements.searchResult.innerHTML = "";
    return;
  }
  const response = await requestJson(`/reservations?name=${encodeURIComponent(name)}`);
  renderSearchResult(response);
}

async function createReservation() {
  const payload = {
    date: state.selectedDate,
    timeId: state.selectedTimeId,
    themeId: state.selectedThemeId,
  };
  return requestJson("/reservations", createPostOption(payload));
}

function createPostOption(payload) {
  return {
    method: "POST",
    headers: {"Content-Type": "application/json", "Member-Id": "1"},
    body: JSON.stringify(payload),
  };
}

function renderReservationResult(result) {
  elements.result.hidden = false;
  const statusText = result.status === "WAITING" ? `대기 (순번: ${result.order}번)` : "예약 확정";
  elements.result.innerHTML = `
    <h3>${result.status === "WAITING" ? "대기 신청 완료" : "예약 완료"}</h3>
    <p>예약자: ${escapeHtml(result.member.name)}</p>
    <p>날짜: ${result.date}</p>
    <p>시간: ${formatStartAt(result.time.startAt)}</p>
    <p>테마: ${escapeHtml(result.theme.name)}</p>
    <p>상태: ${statusText}</p>
  `;
}

function resetSelectedTime() {
  state.selectedTimeId = null;
}

function updateSubmitButton() {
  const disabled = !state.selectedThemeId || !state.selectedDate || !state.selectedTimeId;
  elements.submitButton.disabled = disabled;
}

async function requestJson(url, options = {}) {
  const response = await request(url, options);
  return response.json();
}

async function request(url, options = {}) {
  const response = await fetch(url, options);
  if (response.ok) {
    return response;
  }
  await handleErrorResponse(response);
}

async function handleErrorResponse(response) {
  const error = await readError(response);
  showToast(error.message, true);
  throw new Error(error.message);
}

async function readError(response) {
  const fallback = {message: "요청을 처리하지 못했습니다."};
  try {
    return response.json();
  } catch (error) {
    return fallback;
  }
}

function rankingUrl(period) {
  return `/themes/ranking?startDate=${period.startDate}&endDate=${period.endDate}`;
}

function availableTimeUrl() {
  return `/times/available?date=${state.selectedDate}&themeId=${state.selectedThemeId}`;
}

function recentWeekPeriod() {
  const endDate = addDays(new Date(), -1);
  const startDate = addDays(endDate, -6);
  return {
    startDate: formatDate(startDate),
    endDate: formatDate(endDate),
  };
}

function addDays(date, days) {
  const nextDate = new Date(date);
  nextDate.setDate(nextDate.getDate() + days);
  return nextDate;
}

function formatDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function isSelectableTime(date, timeAvailability) {
  return timeAvailability.availability !== "NOTHING_AVAILABLE" && !isPastDateTime(date, timeAvailability.time.startAt);
}

function isPastReservation(item) {
  return isPastDateTime(item.date, item.time.startAt);
}

function isPastDateTime(date, startAt) {
  const reservationDateTime = new Date(`${date}T${formatStartAt(startAt)}:00`);
  return reservationDateTime < new Date();
}

function createPastBadge() {
  const badge = document.createElement("span");
  badge.className = "past-badge";
  badge.textContent = "지난 예약";
  return badge;
}

function pastReservationTitle(item) {
  if (isPastReservation(item)) {
    return "지난 예약/대기는 취소할 수 없습니다.";
  }
  return "";
}

function formatStartAt(startAt) {
  return startAt.slice(0, 5);
}

function createTextElement(tagName, text) {
  const element = document.createElement(tagName);
  element.textContent = text;
  return element;
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
  window.setTimeout(() => {
    elements.toast.hidden = true;
  }, 2400);
}

function escapeHtml(value) {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}
