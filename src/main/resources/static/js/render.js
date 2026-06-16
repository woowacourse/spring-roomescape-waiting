import {appEl, modalRootEl, toastRootEl} from "./dom.js";
import {canSubmitReservation, isPastReservation, selectedTheme, selectedTime, state, todayString} from "./state.js";
import {formatAmount, isPaymentPendingReservation} from "./payment.js";

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
    ${renderRoute()}
  `;
    modalRootEl.innerHTML = renderConfirm();
    toastRootEl.innerHTML = renderToast();
    syncThemeFilter();
}

function renderRoute() {
    if (state.route === "admin") {
        return renderAdmin();
    }

    if (state.route === "payment-processing") {
        return renderPaymentProcessing();
    }

    if (state.route === "payment-fail") {
        return renderPaymentFail();
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
          <button class="${adminActive ? "is-active" : ""}" type="button" data-route="admin">운영</button>
        </nav>
      </div>
    </header>
  `;
}

function renderReserve() {
    const theme = selectedTheme();
    const time = selectedTime();
    const waitingMode = time && !time.available;
    const popular = state.popularThemes;
    const gridClass = `reservation-grid ${theme ? "has-booking" : ""}`;

    return `
    <main class="page-shell">
      ${renderMyReservations()}
      ${renderPendingPaymentBanner()}

      <section class="headline-row">
        <div>
          <p class="section-kicker">Now Booking</p>
          <h1>방탈출 테마 랭킹</h1>
          <p>테마를 고르고 예약하거나, 이미 찬 시간은 대기로 신청합니다.</p>
        </div>
        <div class="metric-row" aria-label="예약 현황">
          ${renderMetric(state.themes.length, "테마")}
          ${renderMetric(popular.length, "인기")}
          ${renderMetric(state.reservations.length, "예약")}
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
                  <h2 id="booking-title">예약 정보</h2>
                </div>
                <button class="booking-close-button" type="button" data-action="close-booking" aria-label="예약 정보 닫기">×</button>
              </div>
              ${renderSelectedTheme(theme)}
              ${renderReservationFields()}
              ${renderTimeSlots()}
              ${renderBookingSummary(theme)}
              <button class="primary-button submit-button" type="submit" ${canSubmitReservation() ? "" : "disabled"}>
                ${state.submitting ? (waitingMode ? "대기 신청 중" : "예약 중") : (waitingMode ? "대기 신청하기" : "예약하기")}
              </button>
            </form>
          </aside>
        ` : ""}

        <section class="theme-section" aria-labelledby="theme-section-title">
          <div class="section-toolbar">
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

function renderPaymentProcessing() {
    const processing = state.payment.processing;
    const result = state.payment.result;
    const success = result?.status === "success";
    const failed = result?.status === "failed" || processing.status === "failed";
    const title = success ? "결제 완료" : (failed ? "결제 승인 실패" : "결제 승인 중");
    const description = success
        ? "결제가 승인되어 예약이 확정되었습니다."
        : (failed ? "결제 승인 정보를 확인하지 못했습니다. 예약 조회 후 다시 결제해 주세요." : "결제 승인 결과를 확인하고 있습니다.");
    const statusMessage = result?.message || processing.message || "잠시만 기다려 주세요.";

    return `
    <main class="page-shell">
      <section class="payment-processing-panel" aria-live="polite" aria-busy="${success || failed ? "false" : "true"}">
        <p class="section-kicker">Payment</p>
        <h1>${escapeHtml(title)}</h1>
        <p>${escapeHtml(description)}</p>
        <dl class="payment-processing-summary">
          <div>
            <dt>주문</dt>
            <dd>${escapeHtml(processing.orderId || "-")}</dd>
          </div>
          <div>
            <dt>금액</dt>
            <dd>${escapeHtml(processing.amount ? formatAmount(processing.amount) : "-")}</dd>
          </div>
        </dl>
        <p class="payment-processing-message ${failed ? "is-error" : ""}">${escapeHtml(statusMessage)}</p>
        <button class="secondary-button" type="button" data-route="reserve">예약 조회로 돌아가기</button>
      </section>
    </main>
  `;
}

function renderPaymentFail() {
    const failure = state.payment.failure;
    const retryReservation = findRetryablePaymentReservation(failure.orderId);
    const canceled = failure.code === "PAY_PROCESS_CANCELED";
    const title = canceled ? "결제가 취소되었습니다" : "결제를 완료하지 못했습니다";
    const description = retryReservation
        ? "예약은 결제 대기 상태입니다. 같은 주문으로 다시 결제할 수 있습니다."
        : "예약 조회에서 이름으로 예약을 찾은 뒤 결제 대기 예약을 다시 결제해 주세요.";
    const detailMessage = failure.message || "결제가 완료되지 않았습니다.";

    return `
    <main class="page-shell">
      <section class="payment-processing-panel" aria-live="polite">
        <p class="section-kicker">Payment</p>
        <h1>${escapeHtml(title)}</h1>
        <p>${escapeHtml(description)}</p>
        <dl class="payment-processing-summary">
          <div>
            <dt>사유</dt>
            <dd>${escapeHtml(failure.code || "-")}</dd>
          </div>
          <div>
            <dt>주문</dt>
            <dd>${escapeHtml(failure.orderId || "-")}</dd>
          </div>
        </dl>
        <p class="payment-processing-message is-error">${escapeHtml(detailMessage)}</p>
        ${retryReservation ? `
          ${renderPaymentPanel(retryReservation, {
              compact: true,
              title: "결제 재시도",
              description: "이 주문번호와 연결된 결제 대기 예약입니다."
          })}
        ` : ""}
        <div class="payment-fail-actions">
          <button class="secondary-button" type="button" data-route="reserve">예약 조회로 돌아가기</button>
        </div>
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
        <span>선택한 테마</span>
        <strong>${escapeHtml(theme.name)}</strong>
        <p>${escapeHtml(theme.description)}</p>
      </div>
    </div>
  `;
}

function renderReservationFields() {
    return `
    <div class="form-row">
      <label for="reservation-date">날짜</label>
      <input id="reservation-date" name="date" type="date" min="${todayString()}" value="${escapeAttr(state.selectedDate)}">
    </div>
    <div class="form-row">
      <label for="guest-name">예약자</label>
      <input id="guest-name" name="name" type="text" value="${escapeAttr(state.guestName)}" placeholder="이름">
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
        <span>예약 완료 시간은 대기로 신청할 수 있습니다</span>
      </div>
      <div class="time-grid">
        ${state.availableTimes.map((time) => {
        const selected = Number(state.selectedTimeId) === Number(time.id);
        const waitingTarget = !time.available;

        return `
            <button class="time-slot ${selected ? "is-active" : ""} ${waitingTarget ? "is-waiting-target" : ""}" type="button" data-time-slot-id="${time.id}" aria-label="${escapeAttr(`${time.startAt} ${waitingTarget ? "대기 신청 가능" : "예약 가능"}`)}">
              <span>${escapeHtml(time.startAt)}</span>
              <small>${waitingTarget ? "대기 가능" : "예약 가능"}</small>
            </button>
          `;
    }).join("")}
      </div>
    </div>
  `;
}

function renderBookingSummary(theme) {
    const time = selectedTime();
    const waitingMode = time && !time.available;

    return `
    <dl class="booking-summary">
      <div>
        <dt>테마</dt>
        <dd>${theme ? escapeHtml(theme.name) : "-"}</dd>
      </div>
      <div>
        <dt>일정</dt>
        <dd>${escapeHtml(state.selectedDate || "-")} ${time ? escapeHtml(time.startAt) : ""}</dd>
      </div>
      <div>
        <dt>유형</dt>
        <dd>${time ? (waitingMode ? "예약 대기" : "예약 확정") : "-"}</dd>
      </div>
    </dl>
    ${waitingMode ? `<p class="booking-mode-notice">이미 예약된 시간입니다. 신청하면 대기 목록에 등록됩니다.</p>` : ""}
  `;
}

function renderPendingPaymentBanner() {
    const context = state.payment.pendingContext;

    if (!context?.reservation || !isPaymentPendingReservation(context.reservation)) {
        return "";
    }

    return renderPaymentPanel(context.reservation, {
        compact: false,
        title: "결제 대기",
        description: "예약 확정 전 결제가 필요합니다."
    });
}

function renderMyReservations() {
    const canSearch = Boolean(state.reservationSearchName.trim()) && !state.loading.searchedReservations;
    const showReset = state.reservationSearchSubmitted || state.reservationSearchName;

    return `
    <section class="my-reservation-panel" aria-labelledby="my-reservation-title">
      <div class="section-toolbar">
        <div>
          <p class="section-kicker">My Reservation</p>
          <h2 id="my-reservation-title">내 예약 조회</h2>
        </div>
        <form id="reservation-search-form" class="reservation-search-form ${showReset ? "has-reset" : ""}">
          <label class="search-field" for="reservation-search-name">
            <span>예약자</span>
            <input id="reservation-search-name" name="username" type="search" value="${escapeAttr(state.reservationSearchName)}" placeholder="이름">
          </label>
          <button class="primary-button submit-button" type="submit" ${canSearch ? "" : "disabled"}>
            ${state.loading.searchedReservations ? "조회 중" : "조회"}
          </button>
          ${showReset ? `
            <button class="secondary-button" type="button" data-action="clear-reservation-search">초기화</button>
          ` : ""}
        </form>
      </div>

      ${renderMyReservationResults()}
    </section>
  `;
}

function renderMyReservationResults() {
    if (state.loading.searchedReservations) {
        return `<div class="my-reservation-list">${Array.from({length: 2}, () => `<span class="my-reservation-skeleton"></span>`).join("")}</div>`;
    }

    if (!state.reservationSearchSubmitted) {
        return renderEmpty("조회한 예약이 여기에 표시됩니다.");
    }

    if (state.searchedReservations.length === 0 && state.searchedWaitings.length === 0) {
        return renderEmpty("예약 내역이 없습니다.");
    }

    return `
    <div class="my-reservation-results">
      ${renderApplicationGroup("예약 목록", "결제 대기와 확정 예약입니다.", state.searchedReservations, renderMyReservationItem)}
      ${renderApplicationGroup("대기 목록", "예약 취소가 발생하면 순서대로 확정됩니다.", state.searchedWaitings, renderMyWaitingItem)}
    </div>
  `;
}

function renderApplicationGroup(title, description, items, renderer) {
    return `
    <section class="my-reservation-group">
      <div class="application-group-header">
        <div>
          <h3>${escapeHtml(title)}</h3>
          <p>${escapeHtml(description)}</p>
        </div>
        <strong>${items.length}건</strong>
      </div>
      ${items.length > 0 ? `
        <div class="my-reservation-list">
          ${items.map(renderer).join("")}
        </div>
      ` : renderEmpty(`${title}이 없습니다.`)}
    </section>
  `;
}

function renderMyReservationItem(reservation) {
    const editing = Number(state.reservationEdit.id) === Number(reservation.id);
    const past = isPastReservation(reservation);
    const pendingPayment = isPaymentPendingReservation(reservation);

    return `
    <article class="my-reservation-item ${editing ? "is-editing" : ""} ${pendingPayment ? "is-payment-pending" : ""}">
      <span class="row-thumb"><img src="${escapeAttr(reservation.theme.thumbnailImgUrl)}" alt="" loading="lazy" data-cover></span>
      <span class="row-main">
        <span class="reservation-card-heading">
          <strong>${escapeHtml(reservation.theme.name)}</strong>
          ${renderReservationStatusBadge(reservation, past)}
        </span>
        <small>${escapeHtml(reservation.date)} ${escapeHtml(reservation.time.startAt)} · ${escapeHtml(reservation.name)}</small>
        ${pendingPayment ? `<small class="payment-card-amount">결제 금액 ${escapeHtml(formatAmount(reservation.payment.amount))}</small>` : ""}
      </span>
      <span class="reservation-card-actions">
        ${pendingPayment ? `
          <button class="primary-button compact" type="button" data-action="start-payment" data-reservation-id="${reservation.id}">
            결제하기
          </button>
        ` : `
          <button class="secondary-button" type="button" data-action="edit-reservation" data-reservation-id="${reservation.id}" ${past || editing ? "disabled" : ""}>
            ${past ? "변경 불가" : "변경"}
          </button>
        `}
        <button class="danger-button" type="button" data-action="delete-reservation" data-delete-mode="cancel" data-reservation-id="${reservation.id}" ${past ? "disabled" : ""}>
          ${past ? "취소 불가" : "예약 취소"}
        </button>
      </span>
      ${pendingPayment ? renderPaymentPanel(reservation, {compact: false}) : ""}
      ${editing && !pendingPayment ? renderReservationEditForm(reservation) : ""}
    </article>
  `;
}

function renderReservationStatusBadge(reservation, past) {
    if (isPaymentPendingReservation(reservation)) {
        return `<em class="payment-badge">결제 대기</em>`;
    }

    if (past) {
        return `<em class="lock-badge">지난 예약</em>`;
    }

    return `<em class="open-badge">변경 가능</em>`;
}

function renderPaymentPanel(reservation, options = {}) {
    const payment = reservation.payment || {};
    const title = options.title || "결제 준비";
    const description = options.description || "결제를 완료하면 예약이 확정됩니다.";
    const processing = state.payment.processing;
    const sameOrder = processing.orderId && payment.orderId && processing.orderId === payment.orderId;
    const message = sameOrder ? processing.message : "";
    const classes = `payment-ready-panel ${options.compact ? "is-compact" : ""}`;

    return `
    <section class="${classes}" aria-label="결제 대기 안내">
      <div class="payment-ready-copy">
        <p class="section-kicker">${escapeHtml(title)}</p>
        <strong>${escapeHtml(reservation.theme.name)}</strong>
        <span>${escapeHtml(reservation.date)} ${escapeHtml(reservation.time.startAt)} · ${escapeHtml(reservation.name)}</span>
        <small>${escapeHtml(description)}</small>
        ${message ? `<small class="payment-status-message">${escapeHtml(message)}</small>` : ""}
      </div>
      <dl class="payment-ready-summary">
        <div>
          <dt>금액</dt>
          <dd>${escapeHtml(formatAmount(payment.amount))}</dd>
        </div>
        <div>
          <dt>주문</dt>
          <dd>${escapeHtml(payment.orderId || "-")}</dd>
        </div>
      </dl>
      <button class="primary-button payment-button" type="button" data-action="start-payment" data-reservation-id="${reservation.id}">
        결제하기
      </button>
    </section>
  `;
}

function findRetryablePaymentReservation(orderId) {
    if (!orderId) {
        return null;
    }

    const contextReservation = state.payment.pendingContext?.reservation;

    if (isMatchingPendingPaymentReservation(contextReservation, orderId)) {
        return contextReservation;
    }

    return [...state.searchedReservations, ...state.reservations]
        .find((reservation) => isMatchingPendingPaymentReservation(reservation, orderId)) || null;
}

function isMatchingPendingPaymentReservation(reservation, orderId) {
    return isPaymentPendingReservation(reservation) && reservation.payment?.orderId === orderId;
}

function renderMyWaitingItem(waiting) {
    const past = isPastReservation(waiting);
    const rank = Number(waiting.rank);
    const totalRankCount = Number(waiting.totalRankCount);
    const hasRank = Number.isInteger(rank) && rank > 0;
    const hasTotalRankCount = Number.isInteger(totalRankCount) && totalRankCount > 0;
    const maxPostponeSteps = hasRank && hasTotalRankCount ? Math.max(totalRankCount - rank, 0) : 0;
    const canPostpone = !past && maxPostponeSteps > 0 && !state.submitting;
    const rankText = hasRank
        ? `${rank}번째 대기${hasTotalRankCount ? ` / 총 ${totalRankCount}명` : ""}`
        : "대기 중";

    return `
    <article class="my-reservation-item is-waiting">
      <span class="row-thumb"><img src="${escapeAttr(waiting.theme.thumbnailImgUrl)}" alt="" loading="lazy" data-cover></span>
      <span class="row-main">
        <span class="reservation-card-heading">
          <strong>${escapeHtml(waiting.theme.name)}</strong>
          <em class="waiting-badge">${escapeHtml(rankText)}</em>
          ${past ? `<em class="lock-badge">지난 대기</em>` : `<em class="open-badge">대기 신청</em>`}
        </span>
        <small>${escapeHtml(waiting.date)} ${escapeHtml(waiting.time.startAt)} · ${escapeHtml(waiting.name)} · ${escapeHtml(waiting.status)}</small>
      </span>
      <span class="reservation-card-actions">
        <form class="waiting-postpone-form" data-waiting-postpone-form data-waiting-id="${waiting.id}">
          <label for="waiting-postpone-${waiting.id}">미룰 칸</label>
          <input id="waiting-postpone-${waiting.id}" name="steps" type="number" min="1" max="${maxPostponeSteps}" value="${canPostpone ? 1 : ""}" inputmode="numeric" ${canPostpone ? "" : "disabled"}>
          <button class="secondary-button" type="submit" ${canPostpone ? "" : "disabled"}>
            ${state.submitting ? "처리 중" : (maxPostponeSteps > 0 ? "미루기" : "미루기 불가")}
          </button>
        </form>
        <button class="danger-button" type="button" data-action="delete-waiting" data-waiting-id="${waiting.id}" ${past ? "disabled" : ""}>
          ${past ? "취소 불가" : "대기 취소"}
        </button>
      </span>
    </article>
  `;
}

function renderReservationEditForm(reservation) {
    const selectedTimeId = Number(state.reservationEdit.timeId);
    const canSubmit = Boolean(state.reservationEdit.date && selectedTimeId && !state.submitting && !state.reservationEdit.loading);

    return `
    <form id="reservation-edit-form" class="reservation-edit-form" data-reservation-id="${reservation.id}">
      <div class="form-row">
        <label for="reservation-edit-date">변경 날짜</label>
        <input id="reservation-edit-date" name="date" type="date" min="${todayString()}" value="${escapeAttr(state.reservationEdit.date)}">
      </div>
      ${renderReservationEditTimes(reservation)}
      <div class="reservation-edit-actions">
        <button class="secondary-button" type="button" data-action="cancel-reservation-edit">취소</button>
        <button class="primary-button" type="submit" ${canSubmit ? "" : "disabled"}>
          ${state.submitting ? "저장 중" : "변경 저장"}
        </button>
      </div>
    </form>
  `;
}

function renderReservationEditTimes(reservation) {
    if (state.reservationEdit.loading) {
        return `
      <div class="time-section">
        <div class="subsection-heading"><h3>변경 시간</h3></div>
        <div class="time-grid compact">${Array.from({length: 6}, () => `<span class="time-skeleton"></span>`).join("")}</div>
      </div>
    `;
    }

    if (state.reservationEdit.times.length === 0) {
        return renderPanelNotice("선택한 날짜에 등록된 시간이 없습니다.");
    }

    return `
    <div class="time-section">
      <div class="subsection-heading">
        <h3>변경 시간</h3>
        <span>회색은 마감</span>
      </div>
      <div class="time-grid compact">
        ${state.reservationEdit.times.map((time) => {
        const current = isCurrentReservationTime(reservation, time);
        const disabled = !time.available && !current;
        const selected = Number(state.reservationEdit.timeId) === Number(time.id);

        return `
            <button class="time-slot ${selected ? "is-active" : ""} ${current ? "is-current" : ""}" type="button" data-action="select-edit-time" data-time-id="${time.id}" ${disabled ? "disabled" : ""}>
              ${escapeHtml(time.startAt)}
            </button>
          `;
    }).join("")}
      </div>
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
          ${renderMetric(state.reservations.length, "예약")}
        </div>
      </section>

      <section class="admin-layout">
        <aside class="admin-tabs" aria-label="운영 메뉴">
          ${renderAdminTab("themes", "테마")}
          ${renderAdminTab("times", "시간")}
          ${renderAdminTab("reservations", "예약")}
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

    if (state.adminTab === "reservations") {
        return renderReservationsAdmin();
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
      ${state.themes.map(renderThemeAdminRow).join("") || renderEmpty("등록된 테마가 없습니다.")}
    </div>
  `;
}

function renderThemeAdminRow(theme) {
    const reservationCount = countReservationsByTheme(theme.id);
    const locked = reservationCount > 0;

    return `
    <article class="data-row">
      <span class="row-thumb"><img src="${escapeAttr(theme.thumbnailImgUrl)}" alt="" loading="lazy" data-cover></span>
      <span class="row-main">
        <strong>${escapeHtml(theme.name)}</strong>
        <small>${escapeHtml(theme.description)}</small>
        ${locked ? `<em class="lock-badge">${reservationCount}건 예약</em>` : `<em class="open-badge">삭제 가능</em>`}
      </span>
      <button class="danger-button" type="button" data-action="delete-theme" data-theme-id="${theme.id}" ${locked ? `disabled title="예약이 연결된 테마는 삭제할 수 없습니다."` : ""}>
        ${locked ? "삭제 불가" : "삭제"}
      </button>
    </article>
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
      ${state.adminTimes.map(renderTimeAdminItem).join("") || renderEmpty("등록된 시간이 없습니다.")}
    </div>
  `;
}

function renderTimeAdminItem(time) {
    const reservationCount = countReservationsByTime(time.id);
    const locked = reservationCount > 0;

    return `
    <div class="time-admin-item">
      <span class="time-admin-copy">
        <strong>${escapeHtml(time.startAt)}</strong>
        ${locked ? `<em class="lock-badge">${reservationCount}건 예약</em>` : `<em class="open-badge">삭제 가능</em>`}
      </span>
      <button class="danger-button" type="button" data-action="delete-time" data-time-id="${time.id}" ${locked ? `disabled title="예약이 연결된 시간은 삭제할 수 없습니다."` : ""}>
        ${locked ? "삭제 불가" : "삭제"}
      </button>
    </div>
  `;
}

function renderReservationsAdmin() {
    return `
    <div class="admin-panel-header">
      <div>
        <p class="section-kicker">Reservation</p>
        <h2>예약 목록</h2>
      </div>
      <button class="secondary-button" type="button" data-action="refresh-all">새로고침</button>
    </div>

    <div class="reservation-table" role="table" aria-label="예약 목록">
      <div class="table-head" role="row">
        <span>예약자</span>
        <span>테마</span>
        <span>일정</span>
        <span></span>
      </div>
      ${state.reservations.map(renderReservationAdminRow).join("") || renderEmpty("예약이 없습니다.")}
    </div>
  `;
}

function renderReservationAdminRow(reservation) {
    const past = isPastReservation(reservation);

    return `
    <div class="table-row" role="row">
      <span>${escapeHtml(reservation.name)}</span>
      <span>${escapeHtml(reservation.theme.name)}</span>
      <span>${escapeHtml(reservation.date)} ${escapeHtml(reservation.time.startAt)}</span>
      <span>
        <button class="danger-button" type="button" data-action="delete-reservation" data-reservation-id="${reservation.id}" ${past ? "disabled" : ""}>
          ${past ? "삭제 불가" : "삭제"}
        </button>
      </span>
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
          <button class="danger-button strong" type="button" data-action="confirm-ok">${escapeHtml(state.confirm.confirmLabel || "삭제")}</button>
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

function countReservationsByTheme(themeId) {
    return state.reservations.filter((reservation) => Number(reservation.theme?.id) === Number(themeId)).length;
}

function countReservationsByTime(timeId) {
    return state.reservations.filter((reservation) => Number(reservation.time?.id) === Number(timeId)).length;
}

function isCurrentReservationTime(reservation, time) {
    return reservation.date === state.reservationEdit.date &&
        Number(reservation.time.id) === Number(time.id);
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
