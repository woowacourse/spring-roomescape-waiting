const adminState = {
  reservations: [],
  themes: [],
  times: [],
  reservationPage: 1,
  reservationPageSize: 10,
};

const adminElements = {
  reservationCount: document.querySelector("#reservation-count"),
  themeCount: document.querySelector("#theme-count"),
  timeCount: document.querySelector("#time-count"),
  reservationFilterName: document.querySelector("#reservation-filter-name"),
  reservationFilterDate: document.querySelector("#reservation-filter-date"),
  reservationFilterTheme: document.querySelector("#reservation-filter-theme"),
  reservationFilterReset: document.querySelector("#reservation-filter-reset"),
  reservationTableBody: document.querySelector("#reservation-table-body"),
  reservationPagination: document.querySelector("#reservation-pagination"),
  themeForm: document.querySelector("#theme-form"),
  themeName: document.querySelector("#theme-name"),
  themeDescription: document.querySelector("#theme-description"),
  themeThumbnail: document.querySelector("#theme-thumbnail"),
  themeAdminList: document.querySelector("#theme-admin-list"),
  timeForm: document.querySelector("#time-form"),
  timeStartAt: document.querySelector("#time-start-at"),
  timeAdminList: document.querySelector("#time-admin-list"),
  toast: document.querySelector("#toast"),
};

document.addEventListener("DOMContentLoaded", initializeAdmin);

async function initializeAdmin() {
  bindAdminEvents();
  await loadAdminData();
}

function bindAdminEvents() {
  adminElements.reservationFilterName.addEventListener("input", handleReservationFilterChange);
  adminElements.reservationFilterDate.addEventListener("change", handleReservationFilterChange);
  adminElements.reservationFilterTheme.addEventListener("change", handleReservationFilterChange);
  adminElements.reservationFilterReset.addEventListener("click", resetReservationFilters);
  adminElements.themeForm.addEventListener("submit", handleThemeSubmit);
  adminElements.timeForm.addEventListener("submit", handleTimeSubmit);
}

async function loadAdminData() {
  const [reservationResponse, themesResponse, timesResponse] = await Promise.all([
    requestJson("/reservations"),
    requestJson("/themes"),
    requestJson("/times"),
  ]);
  adminState.reservations = reservationResponse.reservations.items;
  adminState.themes = themesResponse.items;
  adminState.times = timesResponse.items;
  renderAdminPage();
}

function renderAdminPage() {
  renderSummary();
  renderThemeFilter();
  renderReservations();
  renderThemes();
  renderTimes();
}

function renderSummary() {
  adminElements.reservationCount.textContent = adminState.reservations.length;
  adminElements.themeCount.textContent = adminState.themes.length;
  adminElements.timeCount.textContent = adminState.times.length;
}

function renderThemeFilter() {
  const selectedValue = adminElements.reservationFilterTheme.value;
  adminElements.reservationFilterTheme.innerHTML = '<option value="">전체</option>';
  adminState.themes.forEach((theme) => {
    const option = document.createElement("option");
    option.value = String(theme.id);
    option.textContent = theme.name;
    adminElements.reservationFilterTheme.appendChild(option);
  });
  adminElements.reservationFilterTheme.value = selectedValue;
}

function renderReservations() {
  adminElements.reservationTableBody.innerHTML = "";
  const reservations = filteredReservations();
  if (reservations.length === 0) {
    renderTableEmpty(adminElements.reservationTableBody, 5, "조회된 예약이 없습니다.");
    renderReservationPagination(reservations);
    return;
  }
  normalizeReservationPage(reservations.length);
  pagedReservations(reservations).forEach((reservation) => {
    adminElements.reservationTableBody.appendChild(createReservationRow(reservation));
  });
  renderReservationPagination(reservations);
}

function handleReservationFilterChange() {
  adminState.reservationPage = 1;
  renderReservations();
}

function pagedReservations(reservations) {
  const startIndex = (adminState.reservationPage - 1) * adminState.reservationPageSize;
  const endIndex = startIndex + adminState.reservationPageSize;
  return reservations.slice(startIndex, endIndex);
}

function normalizeReservationPage(reservationCount) {
  const totalPages = reservationTotalPages(reservationCount);
  if (adminState.reservationPage <= totalPages) {
    return;
  }
  adminState.reservationPage = totalPages;
}

function renderReservationPagination(reservations) {
  adminElements.reservationPagination.innerHTML = "";
  if (reservations.length === 0) {
    return;
  }
  adminElements.reservationPagination.append(
    createPaginationButton("이전", adminState.reservationPage - 1, isFirstReservationPage()),
    createPaginationSummary(reservations.length),
    createPaginationButton("다음", adminState.reservationPage + 1, isLastReservationPage(reservations.length))
  );
}

function createPaginationButton(text, page, disabled) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = "secondary-button";
  button.disabled = disabled;
  button.textContent = text;
  button.addEventListener("click", () => moveReservationPage(page));
  return button;
}

function createPaginationSummary(reservationCount) {
  const summary = document.createElement("span");
  summary.className = "pagination-summary";
  summary.textContent = `${adminState.reservationPage} / ${reservationTotalPages(reservationCount)} 페이지`;
  return summary;
}

function moveReservationPage(page) {
  adminState.reservationPage = page;
  renderReservations();
}

function isFirstReservationPage() {
  return adminState.reservationPage === 1;
}

function isLastReservationPage(reservationCount) {
  return adminState.reservationPage === reservationTotalPages(reservationCount);
}

function reservationTotalPages(reservationCount) {
  return Math.ceil(reservationCount / adminState.reservationPageSize);
}

function filteredReservations() {
  return adminState.reservations.filter((reservation) => {
    return includesName(reservation) && matchesDate(reservation) && matchesTheme(reservation);
  });
}

function includesName(reservation) {
  const name = adminElements.reservationFilterName.value.trim();
  return reservation.name.includes(name);
}

function matchesDate(reservation) {
  const date = adminElements.reservationFilterDate.value;
  if (date === "") {
    return true;
  }
  return reservation.date === date;
}

function matchesTheme(reservation) {
  const themeId = adminElements.reservationFilterTheme.value;
  if (themeId === "") {
    return true;
  }
  return String(reservation.theme.id) === themeId;
}

function createReservationRow(reservation) {
  const row = document.createElement("tr");
  row.append(createCell(reservation.name));
  row.append(createCell(reservation.date));
  row.append(createCell(formatStartAt(reservation.time.startAt)));
  row.append(createCell(reservation.theme.name));
  row.append(createActionCell(createDangerButton("취소", () => confirmReservationDelete(reservation))));
  return row;
}

function resetReservationFilters() {
  adminElements.reservationFilterName.value = "";
  adminElements.reservationFilterDate.value = "";
  adminElements.reservationFilterTheme.value = "";
  adminState.reservationPage = 1;
  renderReservations();
}

async function handleThemeSubmit(event) {
  event.preventDefault();
  await requestJson("/admin/themes", createPostOption(themePayload()));
  adminElements.themeForm.reset();
  showToast("테마가 추가되었습니다.");
  await loadAdminData();
}

function themePayload() {
  return {
    name: adminElements.themeName.value.trim(),
    description: adminElements.themeDescription.value.trim(),
    thumbnailUrl: adminElements.themeThumbnail.value.trim(),
  };
}

function renderThemes() {
  adminElements.themeAdminList.innerHTML = "";
  if (adminState.themes.length === 0) {
    renderEmpty(adminElements.themeAdminList, "등록된 테마가 없습니다.");
    return;
  }
  adminState.themes.forEach((theme) => {
    adminElements.themeAdminList.appendChild(createThemeAdminItem(theme));
  });
}

function createThemeAdminItem(theme) {
  const item = createAdminItem();
  item.append(createThemeAdminContent(theme));
  item.append(createStatusBadge(themeReservationCount(theme.id)));
  item.append(createDangerButton("삭제", () => confirmThemeDelete(theme)));
  return item;
}

function createThemeAdminContent(theme) {
  const content = document.createElement("div");
  content.className = "admin-item-content";
  content.append(createTextElement("strong", theme.name));
  content.append(createTextElement("span", theme.description));
  return content;
}

async function handleTimeSubmit(event) {
  event.preventDefault();
  await requestJson("/times", createPostOption(timePayload()));
  adminElements.timeForm.reset();
  showToast("예약 시간이 추가되었습니다.");
  await loadAdminData();
}

function timePayload() {
  return {
    startAt: adminElements.timeStartAt.value,
  };
}

function renderTimes() {
  adminElements.timeAdminList.innerHTML = "";
  if (adminState.times.length === 0) {
    renderEmpty(adminElements.timeAdminList, "등록된 예약 시간이 없습니다.");
    return;
  }
  adminState.times.forEach((time) => {
    adminElements.timeAdminList.appendChild(createTimeAdminItem(time));
  });
}

function createTimeAdminItem(time) {
  const item = createAdminItem();
  item.append(createTimeAdminContent(time));
  item.append(createStatusBadge(timeReservationCount(time.id)));
  item.append(createDangerButton("삭제", () => confirmTimeDelete(time)));
  return item;
}

function createTimeAdminContent(time) {
  const content = document.createElement("div");
  content.className = "admin-item-content";
  content.append(createTextElement("strong", formatStartAt(time.startAt)));
  content.append(createTextElement("span", "공통 예약 시간"));
  return content;
}

async function confirmReservationDelete(reservation) {
  const message = `[${reservation.name} / ${reservation.date} / ${formatStartAt(reservation.time.startAt)} / ${reservation.theme.name}] 예약을 취소하시겠습니까?`;
  if (window.confirm(message)) {
    await request(`/reservations/${reservation.id}`, {method: "DELETE"});
    showToast("예약이 취소되었습니다.");
    await loadAdminData();
  }
}

async function confirmThemeDelete(theme) {
  const count = themeReservationCount(theme.id);
  if (window.confirm(deleteConfirmMessage(theme.name, count))) {
    await request(`/admin/themes/${theme.id}`, {method: "DELETE"});
    showToast("테마가 삭제되었습니다.");
    await loadAdminData();
  }
}

async function confirmTimeDelete(time) {
  const count = timeReservationCount(time.id);
  if (window.confirm(deleteConfirmMessage(formatStartAt(time.startAt), count))) {
    await request(`/times/${time.id}`, {method: "DELETE"});
    showToast("예약 시간이 삭제되었습니다.");
    await loadAdminData();
  }
}

function deleteConfirmMessage(name, count) {
  if (count > 0) {
    return `[${name}] 항목은 예약 ${count}건에서 사용 중입니다. 삭제하시겠습니까?`;
  }
  return `[${name}] 항목을 삭제하시겠습니까?`;
}

function themeReservationCount(themeId) {
  return adminState.reservations.filter((reservation) => reservation.theme.id === themeId).length;
}

function timeReservationCount(timeId) {
  return adminState.reservations.filter((reservation) => reservation.time.id === timeId).length;
}

function createStatusBadge(count) {
  const badge = document.createElement("span");
  badge.className = count > 0 ? "status-badge is-used" : "status-badge";
  badge.textContent = count > 0 ? `예약 ${count}건` : "사용 가능";
  return badge;
}

function createAdminItem() {
  const item = document.createElement("article");
  item.className = "admin-item";
  return item;
}

function createDangerButton(text, clickHandler) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = "danger-button";
  button.textContent = text;
  button.addEventListener("click", clickHandler);
  return button;
}

function createCell(text) {
  const cell = document.createElement("td");
  cell.textContent = text;
  return cell;
}

function createActionCell(button) {
  const cell = document.createElement("td");
  cell.appendChild(button);
  return cell;
}

function renderTableEmpty(tableBody, columnCount, message) {
  const row = document.createElement("tr");
  const cell = document.createElement("td");
  cell.colSpan = columnCount;
  cell.appendChild(createTextElement("div", message));
  cell.firstElementChild.className = "empty";
  row.appendChild(cell);
  tableBody.appendChild(row);
}

function createPostOption(payload) {
  return {
    method: "POST",
    headers: {"Content-Type": "application/json"},
    body: JSON.stringify(payload),
  };
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

function renderEmpty(container, message) {
  container.innerHTML = `<div class="empty">${message}</div>`;
}

function showToast(message, error = false) {
  adminElements.toast.textContent = message;
  adminElements.toast.className = error ? "toast is-error" : "toast";
  adminElements.toast.hidden = false;
  window.setTimeout(() => {
    adminElements.toast.hidden = true;
  }, 2400);
}

function formatStartAt(startAt) {
  return startAt.slice(0, 5);
}

function createTextElement(tagName, text) {
  const element = document.createElement(tagName);
  element.textContent = text;
  return element;
}
