import {appEl, modalRootEl, toastRootEl} from "./dom.js";
import {canSubmitReservation, selectedTheme, selectedTime, state, todayString} from "./state.js";

let enterAnimationTimer = null;

export function render(options = {}) {
    if (options.animate) {
        window.clearTimeout(enterAnimationTimer);
        appEl.classList.add("is-entering");
        enterAnimationTimer = window.setTimeout(() => {
            appEl.classList.remove("is-entering");
        }, 900);
    } else {
        appEl.classList.remove("is-entering");
    }

    appEl.innerHTML = `
    ${renderHeader()}
    ${renderCurrentPage()}
  `;
    modalRootEl.innerHTML = renderConfirm();
    toastRootEl.innerHTML = renderToast();
    syncThemeFilter();
}

function renderCurrentPage() {
    if (state.route === "admin") {
        return renderAdmin();
    }

    if (state.route === "reservations") {
        return renderReservationLookupPage();
    }

    return renderReserve();
}

export function syncThemeFilter() {
    const query = normalize(state.themeQuery);
    const cards = appEl.querySelectorAll("[data-theme-card]");

    cards.forEach((card) => {
        const target = normalize(card.dataset.search || "");
        card.hidden = Boolean(query) && !target.includes(query);
    });
}

function renderHeader() {
    const reserveActive = state.route === "reserve";
    const reservationsActive = state.route === "reservations";
    const adminActive = state.route === "admin";

    return `
    <header class="site-header">
      <div class="site-header-inner">
        <button class="brand-button" type="button" data-route="reserve" aria-label="예약 화면으로 이동">
          <span class="brand-mark">R</span>
          <span>
            <strong>ROOMESCAPE</strong>
            <small>RESERVATION STORE</small>
          </span>
        </button>
        <nav class="header-nav" aria-label="주요 화면">
          <button class="${reserveActive ? "is-active" : ""}" type="button" data-route="reserve">예약</button>
          <button class="${reservationsActive ? "is-active" : ""}" type="button" data-route="reservations">예약보기</button>
          <button class="${adminActive ? "is-active" : ""}" type="button" data-route="admin">운영</button>
        </nav>
      </div>
    </header>
  `;
}

function renderReserve() {
    const theme = selectedTheme();
    const popular = state.popularThemes;
    const gridClass = `reservation-grid ${theme ? "has-booking" : ""}`;

    return `
    <main class="page-shell">
      <section class="headline-row">
        <div>
          <p class="section-kicker">Now Booking</p>
          <h1>방탈출 테마 랭킹</h1>
          <p>테마를 고르고 날짜와 시간을 바로 확정합니다.</p>
        </div>
        <div class="metric-row" aria-label="예약 현황">
          ${renderMetric(state.themes.length, "테마")}
          ${renderMetric(popular.length, "인기")}
          ${renderMetric(state.availableTimes.filter((time) => time.available).length, "가능 시간")}
        </div>
      </section>

      <section class="${gridClass}">
        <aside class="popular-panel" aria-labelledby="popular-title">
          ${renderPopularThemes(popular)}
        </aside>

        ${theme ? `
          <aside class="booking-panel" aria-labelledby="booking-title">
            <form id="reservation-form">
                <div class="booking-header">
                  <div>
                    <p class="section-kicker">Order</p>
                    <h2 id="booking-title">${state.editingReservationId ? "예약 변경" : "예약 정보"}</h2>
                    ${state.editingReservationId ? `<span class="booking-mode-note">현재 예약을 수정하는 중입니다.</span>` : ""}
                  </div>
                  <div class="booking-header-actions">
                    ${state.editingReservationId ? `<button class="secondary-button compact" type="button" data-action="cancel-editing">변경 취소</button>` : ""}
                    <button class="booking-close-button" type="button" data-action="close-booking" aria-label="예약 정보 닫기">×</button>
                  </div>
                </div>
              ${renderSelectedTheme(theme)}
              ${renderReservationFields()}
              ${renderTimeSlots()}
              ${renderBookingSummary(theme)}
              <button class="primary-button submit-button" type="submit" ${canSubmitReservation() ? "" : "disabled"}>
                ${state.submitting
        ? (state.editingReservationId ? "변경 중" : selectedTime()?.available === false ? "대기 신청 중" : "예약 중")
        : (state.editingReservationId ? "변경하기" : selectedTime()?.available === false ? "대기 신청" : "예약하기")}
              </button>
            </form>
          </aside>
        ` : ""}

        <section class="theme-section" aria-labelledby="theme-section-title">
          <div class="section-toolbar theme-toolbar">
            <div>
              <p class="section-kicker">Theme List</p>
              <h2 id="theme-section-title">전체 테마</h2>
            </div>
            <div class="toolbar-actions">
              <label class="search-field" for="theme-search">
                <span>검색</span>
                <input id="theme-search" type="search" value="${escapeAttr(state.themeQuery)}" placeholder="테마명">
              </label>
            </div>
          </div>

          ${renderThemeGrid()}
        </section>
      </section>
    </main>
  `;
}

function renderReservationLookupPage() {
    const editingReservation = state.editingReservationId ? state.myReservations.find((reservation) => Number(reservation.id) === Number(state.editingReservationId)) : null;

    return `
    <main class="page-shell">
      <section class="headline-row">
        <div>
          <p class="section-kicker">My Reservation</p>
          <h1>예약 조회 및 변경</h1>
          <p>이름으로 본인 예약을 조회하고 날짜와 시간을 변경하거나 취소할 수 있습니다.</p>
        </div>
        <div class="metric-row" aria-label="내 예약 현황">
          ${renderMetric(state.myReservations.length, "내 예약")}
          ${renderMetric(state.myPaymentOrders.filter((order) => order.status === "CONFIRMATION_UNKNOWN").length, "확인 필요")}
          ${renderMetric(state.myWaitings.length, "대기 중")}
        </div>
      </section>

      <section class="lookup-layout">
        <section class="theme-section" aria-labelledby="reservation-lookup-title">
          ${renderReservationLookup()}
          ${renderPaymentOrders()}
          ${renderMyReservations()}
          ${renderMyWaitings()}
        </section>

        ${editingReservation ? `
          <section class="theme-section reservation-edit-section" aria-labelledby="lookup-booking-title">
            <form id="reservation-form">
              <div class="booking-header">
                <div>
                  <p class="section-kicker">Update</p>
                  <h2 id="lookup-booking-title">예약 변경</h2>
                  <span class="booking-mode-note">조회한 예약의 날짜와 시간을 수정합니다.</span>
                </div>
                <div class="booking-header-actions">
                  <button class="secondary-button compact" type="button" data-action="cancel-editing">변경 취소</button>
                </div>
              </div>
              ${renderSelectedTheme(selectedTheme())}
              ${renderReservationFields()}
              ${renderTimeSlots()}
              ${renderBookingSummary(selectedTheme())}
              <button class="primary-button submit-button" type="submit" ${canSubmitReservation() ? "" : "disabled"}>
                ${state.submitting ? "변경 중" : "변경하기"}
              </button>
            </form>
          </section>
        ` : ""}
      </section>
    </main>
  `;
}

function renderPopularThemes(popular) {
    if (state.loading.boot && popular.length === 0) {
        return `
      <div class="popular-header">
        <p class="section-kicker">Live Rank</p>
        <h2 id="popular-title">실시간 인기</h2>
        <span>최근 1주 예약 기준</span>
      </div>
      ${renderSkeletonStrip()}
    `;
    }

    if (popular.length === 0) {
        return `
      <div class="popular-header">
        <p class="section-kicker">Live Rank</p>
        <h2 id="popular-title">실시간 인기</h2>
      </div>
      ${renderEmpty("아직 인기 테마가 없습니다.")}
    `;
    }

    return `
    <div class="popular-section">
      <div class="popular-header">
        <p class="section-kicker">Live Rank</p>
        <h2 id="popular-title">실시간 인기</h2>
        <span>최근 1주 예약 기준</span>
      </div>
      <div class="popular-list">
        ${popular.map((theme, index) => `
          <button class="popular-item ${isSelected(theme) ? "is-active" : ""}" type="button" data-theme-id="${theme.id}">
            <span class="popular-rank">${index + 1}</span>
            <span class="popular-name">${escapeHtml(theme.name)}</span>
            <span class="popular-count">${theme.reservedCount}건</span>
          </button>
        `).join("")}
      </div>
    </div>
  `;
}

function renderThemeGrid() {
    if (state.loading.boot && state.themes.length === 0) {
        return `<div class="theme-grid">${Array.from({length: 6}, () => `<div class="theme-card-skeleton"></div>`).join("")}</div>`;
    }

    if (state.themes.length === 0) {
        return renderEmpty("등록된 테마가 없습니다.");
    }

    return `
    <div class="theme-grid">
      ${state.themes.map(renderThemeCard).join("")}
    </div>
  `;
}

function renderThemeCard(theme) {
    const selected = isSelected(theme);

    return `
    <button class="theme-card ${selected ? "is-active" : ""}" type="button" data-theme-id="${theme.id}" data-theme-card data-search="${escapeAttr(`${theme.name} ${theme.description}`)}" aria-pressed="${selected}">
      <span class="image-frame">
        <img src="${escapeAttr(theme.thumbnailImgUrl)}" alt="${escapeAttr(theme.name)}" loading="lazy" data-cover>
      </span>
      <span class="theme-card-body">
        <strong>${escapeHtml(theme.name)}</strong>
        <span class="theme-card-description">${escapeHtml(theme.description)}</span>
        <span class="theme-card-action">${selected ? "선택됨" : "선택하기"}</span>
      </span>
    </button>
  `;
}

function renderSelectedTheme(theme) {
    if (!theme) {
        return `<div class="selected-theme is-empty">테마를 선택해 주세요.</div>`;
    }

    return `
    <div class="selected-theme">
      <div class="selected-copy">
        <span>${state.editingReservationId ? "변경할 테마" : "선택한 테마"}</span>
        <strong>${escapeHtml(theme.name)}</strong>
        <p>${escapeHtml(theme.description)}</p>
      </div>
    </div>
  `;
}

function renderReservationFields() {
    return `
    <div class="form-row">
      <label for="guest-name">예약자</label>
      <input id="guest-name" name="name" type="text" value="${escapeAttr(state.guestName)}" placeholder="이름" ${state.editingReservationId ? "readonly" : ""}>
    </div>
    <div class="form-row">
      <label for="reservation-date">날짜</label>
      <input id="reservation-date" name="date" type="date" min="${todayString()}" value="${escapeAttr(state.selectedDate)}">
    </div>
  `;
}

function renderTimeSlots() {
    if (!state.selectedThemeId) {
        return renderPanelNotice("테마를 선택하면 시간이 표시됩니다.");
    }

    if (state.loading.times) {
        return `
      <div class="time-section">
        <div class="subsection-heading"><h3>시간 선택</h3></div>
        <div class="time-grid">${Array.from({length: 8}, () => `<span class="time-skeleton"></span>`).join("")}</div>
      </div>
    `;
    }

    if (state.availableTimes.length === 0) {
        return renderPanelNotice("등록된 예약 시간이 없습니다.");
    }

    return `
    <div class="time-section">
      <div class="subsection-heading">
        <h3>시간 선택</h3>
        <span>회색은 마감</span>
      </div>
      <div class="time-grid">
        ${state.availableTimes.map((time) => `
          <button class="time-slot ${Number(state.selectedTimeId) === Number(time.id) ? "is-active" : ""} ${time.available ? "" : "is-full"}" type="button" data-time-slot-id="${time.id}">
            ${escapeHtml(time.startAt)}
          </button>
        `).join("")}
      </div>
    </div>
  `;
}

function renderBookingSummary(theme) {
    const time = selectedTime();

    return `
    <dl class="booking-summary">
      <div>
        <dt>예약자</dt>
        <dd>${escapeHtml(state.guestName || "-")}</dd>
      </div>
      <div>
        <dt>테마</dt>
        <dd>${theme ? escapeHtml(theme.name) : "-"}</dd>
      </div>
      <div>
        <dt>일정</dt>
        <dd>${escapeHtml(state.selectedDate || "-")} ${time ? escapeHtml(time.startAt) : ""}</dd>
      </div>
      <div>
        <dt>모드</dt>
        <dd>${state.editingReservationId ? "예약 변경" : "새 예약"}</dd>
      </div>
    </dl>
  `;
}

function renderReservationLookup() {
    return `
    <section class="reservation-lookup-section" aria-labelledby="reservation-lookup-title">
      <div class="section-toolbar compact">
        <div>
          <p class="section-kicker">My Reservation</p>
          <h2 id="reservation-lookup-title">내 예약 조회</h2>
          <p class="section-copy">예약자 이름으로 예약을 확인하고 변경하거나 취소할 수 있습니다.</p>
        </div>
      </div>
      <form id="reservation-lookup-form" class="lookup-row">
        <label class="search-field wide" for="reservation-name">
          <span>예약자 이름</span>
          <input id="reservation-name" name="name" type="text" value="${escapeAttr(state.guestName)}" placeholder="이름을 입력해 내 예약을 조회하세요">
        </label>
        <button class="secondary-button lookup-submit" type="submit">조회</button>
      </form>
    </section>
  `;
}

function renderPaymentOrders() {
    const hasName = Boolean(state.guestName.trim());

    return `
    <section class="my-reservations-section" aria-labelledby="payment-orders-title">
      <div class="admin-panel-header">
        <div>
          <p class="section-kicker">Payment History</p>
          <h2 id="payment-orders-title">주문 / 결제 내역</h2>
        </div>
      </div>
      ${renderPaymentOrdersContent(hasName)}
    </section>
  `;
}

function renderPaymentOrdersContent(hasName) {
    if (!hasName) {
        return renderEmpty("예약자 이름을 입력하면 주문과 결제 상태를 확인할 수 있습니다.");
    }
    if (state.myPaymentOrders.length === 0) {
        return renderEmpty("조회된 결제 주문이 없습니다.");
    }

    return `
    <div class="payment-order-list">
      ${state.myPaymentOrders.map((order) => `
        <article class="payment-order-card">
          <div class="payment-order-heading">
            <div>
              <strong>${escapeHtml(order.themeName)}</strong>
              <span>${escapeHtml(order.date)} ${escapeHtml(order.startAt)}</span>
            </div>
            <span class="payment-status ${paymentStatusClass(order.status)}">${paymentStatusLabel(order.status)}</span>
          </div>
          <dl class="payment-order-detail">
            <div><dt>orderId</dt><dd>${escapeHtml(order.orderId)}</dd></div>
            <div><dt>paymentKey</dt><dd>${order.status === "CONFIRMED" ? escapeHtml(order.paymentKey || "-") : "-"}</dd></div>
            <div><dt>금액</dt><dd>${Number(order.amount).toLocaleString("ko-KR")}원</dd></div>
          </dl>
          ${order.retryable ? `
            <button class="secondary-button" type="button" data-action="retry-payment" data-order-id="${escapeAttr(order.orderId)}" ${state.submitting ? "disabled" : ""}>
              같은 주문으로 승인 다시 확인
            </button>
          ` : ""}
        </article>
      `).join("")}
    </div>
  `;
}

function paymentStatusLabel(status) {
    return {
        PAYMENT_PENDING: "결제 대기",
        CONFIRMATION_UNKNOWN: "확인 필요",
        PAYMENT_FAILED: "결제 실패",
        CONFIRMED: "확정"
    }[status] || status;
}

function paymentStatusClass(status) {
    return {
        PAYMENT_PENDING: "pending",
        CONFIRMATION_UNKNOWN: "unknown",
        PAYMENT_FAILED: "failed",
        CONFIRMED: "confirmed"
    }[status] || "";
}

function renderMyReservations() {
    const hasName = Boolean(state.guestName.trim());

    return `
    <section class="my-reservations-section" aria-labelledby="my-reservations-title">
      <div class="admin-panel-header">
        <div>
          <p class="section-kicker">My Reservation</p>
          <h2 id="my-reservations-title">내 예약</h2>
        </div>
      </div>
      ${renderMyReservationsContent(hasName)}
    </section>
  `;
}

function renderMyReservationsContent(hasName) {
    if (!hasName) {
        return renderEmpty("예약자 이름을 입력하면 내 예약을 확인할 수 있습니다.");
    }

    if (state.myReservations.length === 0) {
        return renderEmpty("조회된 예약이 없습니다.");
    }

    return `
    <div class="reservation-table" role="table" aria-label="내 예약 목록">
      <div class="table-head" role="row">
        <span>예약자</span>
        <span>테마</span>
        <span>일정</span>
        <span>관리</span>
      </div>
      ${state.myReservations.map((reservation) => `
        <div class="table-row" role="row">
          <span>${escapeHtml(reservation.name)}</span>
          <span>${escapeHtml(reservation.theme.name)}</span>
          <span>${escapeHtml(reservation.date)} ${escapeHtml(reservation.time.startAt)}</span>
          <span class="table-actions">
            <button class="secondary-button" type="button" data-action="edit-reservation" data-reservation-id="${reservation.id}">변경</button>
            <button class="danger-button" type="button" data-action="delete-reservation" data-reservation-id="${reservation.id}">취소</button>
          </span>
        </div>
      `).join("")}
    </div>
  `;
}

function renderMyWaitings() {
    const hasName = Boolean(state.guestName.trim());

    return `
    <section class="my-reservations-section" aria-labelledby="my-waitings-title">
      <div class="admin-panel-header">
        <div>
          <p class="section-kicker">My Waiting</p>
          <h2 id="my-waitings-title">내 대기</h2>
        </div>
      </div>
      ${renderMyWaitingsContent(hasName)}
    </section>
  `;
}

function renderMyWaitingsContent(hasName) {
    if (!hasName) {
        return renderEmpty("예약자 이름을 입력하면 내 대기를 확인할 수 있습니다.");
    }

    if (state.myWaitings.length === 0) {
        return renderEmpty("대기 중인 항목이 없습니다.");
    }

    return `
    <div class="reservation-table" role="table" aria-label="내 대기 목록">
      <div class="table-head" role="row">
        <span>예약자</span>
        <span>테마</span>
        <span>일정</span>
        <span>대기 순번</span>
        <span>관리</span>
      </div>
      ${state.myWaitings.map((waiting) => `
        <div class="table-row" role="row">
          <span>${escapeHtml(waiting.name)}</span>
          <span>${escapeHtml(waiting.theme.name)}</span>
          <span>${escapeHtml(waiting.date)} ${escapeHtml(waiting.time.startAt)}</span>
          <span><span class="waiting-badge">${waiting.order}번째</span></span>
          <span class="table-actions">
            <button class="danger-button" type="button" data-action="delete-waiting" data-waiting-id="${waiting.id}">취소</button>
          </span>
        </div>
      `).join("")}
    </div>
  `;
}

function renderAdmin() {
    return `
    <main class="page-shell">
      <section class="headline-row">
        <div>
          <p class="section-kicker">Operations</p>
          <h1>운영 관리</h1>
          <p>테마, 시간, 예약 데이터를 빠르게 확인합니다.</p>
        </div>
        <div class="metric-row" aria-label="운영 현황">
          ${renderMetric(state.themes.length, "테마")}
          ${renderMetric(state.adminTimes.length, "시간")}
        </div>
      </section>

        <section class="admin-layout">
        <aside class="admin-tabs" aria-label="운영 메뉴">
          ${renderAdminTab("themes", "테마")}
          ${renderAdminTab("times", "시간")}
        </aside>
        <section class="admin-panel">
          ${renderAdminPanel()}
        </section>
      </section>
    </main>
  `;
}

function renderAdminTab(tab, label) {
    return `
    <button class="${state.adminTab === tab ? "is-active" : ""}" type="button" data-admin-tab="${tab}">
      ${escapeHtml(label)}
    </button>
  `;
}

function renderAdminPanel() {
    if (state.adminTab === "times") {
        return renderTimesAdmin();
    }

    return renderThemesAdmin();
}

function renderThemesAdmin() {
    return `
    <div class="admin-panel-header">
      <div>
        <p class="section-kicker">Theme</p>
        <h2>테마 관리</h2>
      </div>
      <button class="secondary-button" type="button" data-action="refresh-all">새로고침</button>
    </div>

    <form id="theme-form" class="admin-form">
      <div class="form-row">
        <label for="theme-name">테마명</label>
        <input id="theme-name" name="name" type="text" placeholder="새 테마명">
      </div>
      <div class="form-row">
        <label for="theme-description">설명</label>
        <input id="theme-description" name="description" type="text" placeholder="한 줄 설명">
      </div>
      <div class="form-row wide">
        <label for="theme-thumbnail">이미지 URL</label>
        <input id="theme-thumbnail" name="thumbnailImgUrl" type="url" placeholder="https://images.unsplash.com/...">
      </div>
      <button class="primary-button" type="submit" ${state.submitting ? "disabled" : ""}>테마 추가</button>
    </form>

    <div class="data-list">
      ${state.themes.map((theme) => `
        <article class="data-row">
          <span class="row-thumb"><img src="${escapeAttr(theme.thumbnailImgUrl)}" alt="" loading="lazy" data-cover></span>
          <span class="row-main">
            <strong>${escapeHtml(theme.name)}</strong>
            <small>${escapeHtml(theme.description)}</small>
          </span>
          <button class="danger-button" type="button" data-action="delete-theme" data-theme-id="${theme.id}">삭제</button>
        </article>
      `).join("") || renderEmpty("등록된 테마가 없습니다.")}
    </div>
  `;
}

function renderTimesAdmin() {
    return `
    <div class="admin-panel-header">
      <div>
        <p class="section-kicker">Time</p>
        <h2>예약 시간 관리</h2>
      </div>
      <button class="secondary-button" type="button" data-action="refresh-all">새로고침</button>
    </div>

    <form id="time-form" class="admin-form compact">
      <div class="form-row">
        <label for="time-start-at">시작 시간</label>
        <input id="time-start-at" name="startAt" type="time">
      </div>
      <button class="primary-button" type="submit" ${state.submitting ? "disabled" : ""}>시간 추가</button>
    </form>

    <div class="time-admin-grid">
      ${state.adminTimes.map((time) => `
        <div class="time-admin-item">
          <strong>${escapeHtml(time.startAt)}</strong>
          <button class="danger-button" type="button" data-action="delete-time" data-time-id="${time.id}">삭제</button>
        </div>
      `).join("") || renderEmpty("등록된 시간이 없습니다.")}
    </div>
  `;
}

function renderMetric(value, label) {
    return `
    <div class="metric">
      <strong>${escapeHtml(value)}</strong>
      <span>${escapeHtml(label)}</span>
    </div>
  `;
}

function renderSkeletonStrip() {
    return `
    <div class="popular-list">
      ${Array.from({length: 5}, () => `<span class="popular-skeleton"></span>`).join("")}
    </div>
  `;
}

function renderPanelNotice(message) {
    return `<p class="panel-notice">${escapeHtml(message)}</p>`;
}

function renderEmpty(message) {
    return `<p class="empty-state">${escapeHtml(message)}</p>`;
}

function renderConfirm() {
    if (!state.confirm) {
        return "";
    }

    return `
    <div class="modal-backdrop" data-action="cancel-confirm">
      <section class="confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="confirm-title">
        <h2 id="confirm-title">${escapeHtml(state.confirm.title)}</h2>
        <p>${escapeHtml(state.confirm.body)}</p>
        <div class="confirm-actions">
          <button class="secondary-button" type="button" data-action="cancel-confirm">취소</button>
          <button class="danger-button strong" type="button" data-action="confirm-ok">삭제</button>
        </div>
      </section>
    </div>
  `;
}

function renderToast() {
    if (!state.toast) {
        return "";
    }

    return `
    <button class="toast ${state.toast.type}" type="button" data-action="dismiss-toast">
      ${escapeHtml(state.toast.message)}
    </button>
  `;
}

function isSelected(theme) {
    return Number(state.selectedThemeId) === Number(theme.id);
}

function normalize(value) {
    return String(value || "").trim().toLowerCase();
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}

function escapeAttr(value) {
    return escapeHtml(value);
}
