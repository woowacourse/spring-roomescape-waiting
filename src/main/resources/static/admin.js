const adminState = {
  reservations: [],
  themes: [],
  times: [],
  reservationPage: 1,
  reservationPageSize: 10,
};

const adminElements = {
  reservationCount: document.querySelector("#reservation-count"),
  waitCount: document.querySelector("#wait-count"),
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
  const [reservations, themes, times] = await Promise.all([
    requestJson("/reservations"),
    requestJson("/themes"),
    requestJson("/times"),
  ]);
  adminState.reservations = reservations;
  adminState.themes = themes;
  adminState.times = times;
  renderAdminPage();
}

function renderAdminPage() {
  renderSummary();
  renderThemeFilter();
  renderReservations();
  renderThemes();
  renderTimes();
}

// ── 요약 ─────────────────────────────────────────────────────

function renderSummary() {
  const confirmed = adminState.reservations.filter((r) => r.status === "CONFIRMED").length;
  const waiting = adminState.reservations.filter((r) => r.status === "WAITING").length;
  adminElements.reservationCount.textContent = confirmed;
  adminElements.waitCount.textContent = waiting;
  adminElements.themeCount.textContent = adminState.themes.length;
  adminElements.timeCount.textContent = adminState.times.length;
}

// ── 예약 테이블 ──────────────────────────────────────────────

function renderThemeFilter() {
  const selected = adminElements.reservationFilterTheme.value;
  adminElements.reservationFilterTheme.innerHTML = '<option value="">전체</option>';
  adminState.themes.forEach((theme) => {
    const opt = document.createElement("option");
    opt.value = String(theme.id);
    opt.textContent = theme.name;
    adminElements.reservationFilterTheme.appendChild(opt);
  });
  adminElements.reservationFilterTheme.value = selected;
}

function renderReservations() {
  adminElements.reservationTableBody.innerHTML = "";
  const reservations = filteredReservations();
  if (reservations.length === 0) {
    renderTableEmpty(adminElements.reservationTableBody, 6, "조회된 예약이 없습니다.");
    renderReservationPagination(reservations);
    return;
  }
  normalizeReservationPage(reservations.length);
  pagedReservations(reservations).forEach((r) => {
    adminElements.reservationTableBody.appendChild(createReservationRow(r));
  });
  renderReservationPagination(reservations);
}

function handleReservationFilterChange() {
  adminState.reservationPage = 1;
  renderReservations();
}

function filteredReservations() {
  const name = adminElements.reservationFilterName.value.trim();
  const date = adminElements.reservationFilterDate.value;
  const themeId = adminElements.reservationFilterTheme.value;
  return adminState.reservations.filter((r) =>
    r.name.includes(name) &&
    (date === "" || r.date === date) &&
    (themeId === "" || String(r.theme.id) === themeId)
  );
}

function pagedReservations(reservations) {
  const start = (adminState.reservationPage - 1) * adminState.reservationPageSize;
  return reservations.slice(start, start + adminState.reservationPageSize);
}

function normalizeReservationPage(count) {
  const total = reservationTotalPages(count);
  if (adminState.reservationPage > total) adminState.reservationPage = total;
}

function reservationTotalPages(count) {
  return Math.ceil(count / adminState.reservationPageSize);
}

function renderReservationPagination(reservations) {
  adminElements.reservationPagination.innerHTML = "";
  if (reservations.length === 0) return;
  adminElements.reservationPagination.append(
    createPaginationButton("이전", adminState.reservationPage - 1, adminState.reservationPage === 1),
    createPaginationSummary(reservations.length),
    createPaginationButton("다음", adminState.reservationPage + 1, adminState.reservationPage === reservationTotalPages(reservations.length))
  );
}

function createPaginationButton(text, page, disabled) {
  const btn = document.createElement("button");
  btn.type = "button";
  btn.className = "secondary-button";
  btn.disabled = disabled;
  btn.textContent = text;
  btn.addEventListener("click", () => { adminState.reservationPage = page; renderReservations(); });
  return btn;
}

function createPaginationSummary(count) {
  const span = document.createElement("span");
  span.className = "pagination-summary";
  span.textContent = `${adminState.reservationPage} / ${reservationTotalPages(count)} 페이지`;
  return span;
}

function createReservationRow(reservation) {
  const row = document.createElement("tr");
  row.append(
    createCell(reservation.name),
    createCell(reservation.date),
    createCell(formatStartAt(reservation.time.startAt)),
    createCell(reservation.theme.name),
    createStatusCell(reservation.status),
    createActionCell(createDangerButton("취소", () => confirmReservationDelete(reservation)))
  );
  return row;
}

function createStatusCell(status) {
  const cell = document.createElement("td");
  const pill = document.createElement("span");
  pill.className = status === "CONFIRMED" ? "status-pill is-confirmed" : "status-pill is-waiting";
  pill.textContent = status === "CONFIRMED" ? "예약 확정" : "대기";
  cell.appendChild(pill);
  return cell;
}

function resetReservationFilters() {
  adminElements.reservationFilterName.value = "";
  adminElements.reservationFilterDate.value = "";
  adminElements.reservationFilterTheme.value = "";
  adminState.reservationPage = 1;
  renderReservations();
}

async function confirmReservationDelete(reservation) {
  const type = reservation.status === "WAITING" ? "대기" : "예약";
  const msg = `[${reservation.name} / ${reservation.date} / ${formatStartAt(reservation.time.startAt)} / ${reservation.theme.name}] ${type}를 취소하시겠습니까?`;
  if (!window.confirm(msg)) return;
  const url = reservation.status === "WAITING"
    ? `/reservations/waits/${reservation.id}`
    : `/reservations/${reservation.id}`;
  await request(url, { method: "DELETE" });
  showToast(`${type}가 취소되었습니다.`);
  await loadAdminData();
}

// ── 테마 관리 ────────────────────────────────────────────────

async function handleThemeSubmit(event) {
  event.preventDefault();
  await requestJson("/admin/themes", createPostOption({
    name: adminElements.themeName.value.trim(),
    description: adminElements.themeDescription.value.trim(),
    thumbnailUrl: adminElements.themeThumbnail.value.trim(),
  }));
  adminElements.themeForm.reset();
  showToast("테마가 추가되었습니다.");
  await loadAdminData();
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
  const content = document.createElement("div");
  content.className = "admin-item-content";
  const strong = document.createElement("strong");
  strong.textContent = theme.name;
  const span = document.createElement("span");
  span.textContent = theme.description;
  content.append(strong, span);
  const count = themeReservationCount(theme.id);
  item.append(content, createStatusBadge(count), createDangerButton("삭제", () => confirmThemeDelete(theme)));
  return item;
}

async function confirmThemeDelete(theme) {
  const count = themeReservationCount(theme.id);
  if (window.confirm(deleteConfirmMessage(theme.name, count))) {
    await request(`/admin/themes/${theme.id}`, { method: "DELETE" });
    showToast("테마가 삭제되었습니다.");
    await loadAdminData();
  }
}

function themeReservationCount(themeId) {
  return adminState.reservations.filter((r) => r.theme.id === themeId).length;
}

// ── 시간 관리 ────────────────────────────────────────────────

async function handleTimeSubmit(event) {
  event.preventDefault();
  await requestJson("/times", createPostOption({ startAt: adminElements.timeStartAt.value }));
  adminElements.timeForm.reset();
  showToast("예약 시간이 추가되었습니다.");
  await loadAdminData();
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
  const content = document.createElement("div");
  content.className = "admin-item-content";
  const strong = document.createElement("strong");
  strong.textContent = formatStartAt(time.startAt);
  const span = document.createElement("span");
  span.textContent = "공통 예약 시간";
  content.append(strong, span);
  const count = timeReservationCount(time.id);
  item.append(content, createStatusBadge(count), createDangerButton("삭제", () => confirmTimeDelete(time)));
  return item;
}

async function confirmTimeDelete(time) {
  const count = timeReservationCount(time.id);
  if (window.confirm(deleteConfirmMessage(formatStartAt(time.startAt), count))) {
    await request(`/times/${time.id}`, { method: "DELETE" });
    showToast("예약 시간이 삭제되었습니다.");
    await loadAdminData();
  }
}

function timeReservationCount(timeId) {
  return adminState.reservations.filter((r) => r.time.id === timeId).length;
}

function deleteConfirmMessage(name, count) {
  if (count > 0) return `[${name}] 항목은 예약 ${count}건에서 사용 중입니다. 삭제하시겠습니까?`;
  return `[${name}] 항목을 삭제하시겠습니까?`;
}

// ── UI 유틸 ──────────────────────────────────────────────────

function createAdminItem() {
  const item = document.createElement("article");
  item.className = "admin-item";
  return item;
}

function createStatusBadge(count) {
  const badge = document.createElement("span");
  badge.className = count > 0 ? "status-badge is-used" : "status-badge";
  badge.textContent = count > 0 ? `예약 ${count}건` : "사용 가능";
  return badge;
}

function createDangerButton(text, clickHandler) {
  const btn = document.createElement("button");
  btn.type = "button";
  btn.className = "danger-button";
  btn.textContent = text;
  btn.addEventListener("click", clickHandler);
  return btn;
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

function renderTableEmpty(tableBody, colSpan, message) {
  const row = document.createElement("tr");
  const cell = document.createElement("td");
  cell.colSpan = colSpan;
  const div = document.createElement("div");
  div.className = "empty";
  div.textContent = message;
  cell.appendChild(div);
  row.appendChild(cell);
  tableBody.appendChild(row);
}

function renderEmpty(container, message) {
  container.innerHTML = `<div class="empty">${message}</div>`;
}

function showToast(message, error = false) {
  adminElements.toast.textContent = message;
  adminElements.toast.className = error ? "toast is-error" : "toast";
  adminElements.toast.hidden = false;
  clearTimeout(adminElements.toast._timer);
  adminElements.toast._timer = setTimeout(() => { adminElements.toast.hidden = true; }, 2800);
}

function formatStartAt(startAt) {
  return startAt.slice(0, 5);
}

function createPostOption(payload) {
  return { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify(payload) };
}

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