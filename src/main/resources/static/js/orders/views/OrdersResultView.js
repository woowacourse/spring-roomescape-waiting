import View from "../../common/View.js";
import {clearElement, createElement} from "../../common/helpers.js";

export default class OrdersResultView extends View {
    constructor(element) {
        super(element);
    }

    render(orders) {
        clearElement(this.element);

        if (!orders || !orders.length) {
            this.element.innerHTML = `
                <div class="empty-state">주문/결제 내역을 검색하세요</div>
            `;
            return;
        }

        orders.forEach((order) => {
            const item = createElement("div", "reservation-item order-item");
            const badge = this.resolveBadge(order);

            item.dataset.reservationId = order.reservationId;

            item.innerHTML = `
                <div class="order-main">
                    <div class="res-title-row">
                        <div class="res-theme">${escapeHtml(order.themeName)}</div>
                        <span class="res-status ${badge.className}">${badge.label}</span>
                    </div>

                    <div class="res-details">
                        ${order.date}
                        ·
                        ${formatTime(order.time)}
                    </div>

                    ${this.renderPaymentDetail(order, badge)}
                </div>
            `;

            this.element.appendChild(item);
        });
    }

    renderPaymentDetail(order, badge) {
        if (badge.kind === "UNKNOWN") {
            const link = buildRetryLink(order);
            return `
                <div class="order-notice unknown">
                    확인 필요 — 잠시 후 다시 확인해주세요
                </div>
                ${link ? `<a class="order-retry-btn" href="${link}">다시 확인하기</a>` : ""}
            `;
        }

        if (badge.kind === "CONFIRMED") {
            return `
                <div class="order-payment">
                    <div class="order-amount">${formatAmount(order.amount)}</div>
                    <div class="order-meta">주문번호 ${escapeHtml(order.orderId)}</div>
                    <div class="order-meta">결제키 ${escapeHtml(order.paymentKey)}</div>
                </div>
            `;
        }

        return "";
    }

    resolveBadge(order) {
        if (order.entryStatus === "WAITING" || order.paymentStatus === null || order.paymentStatus === undefined) {
            return {kind: "WAITING", className: "status-muted", label: "대기중"};
        }

        switch (order.paymentStatus) {
            case "CONFIRMED":
                return {kind: "CONFIRMED", className: "status-confirmed", label: "확정"};
            case "FAILED":
                return {kind: "FAILED", className: "status-failed", label: "결제 실패"};
            case "CONFIRM_RESULT_UNKNOWN":
                return {kind: "UNKNOWN", className: "status-unknown", label: "⏳ 확인 필요"};
            case "PENDING":
            default:
                return {kind: "PENDING", className: "status-muted", label: "결제 대기"};
        }
    }
}

function buildRetryLink(order) {
    if (order.paymentKey === null || order.orderId === null || order.amount === null
        || order.paymentKey === undefined || order.orderId === undefined || order.amount === undefined) {
        return null;
    }

    const paymentKey = encodeURIComponent(order.paymentKey);
    const orderId = encodeURIComponent(order.orderId);
    const amount = encodeURIComponent(order.amount);

    return `/payments/success?paymentKey=${paymentKey}&orderId=${orderId}&amount=${amount}`;
}

function formatTime(time) {
    if (!time) {
        return "";
    }
    return time.slice(0, 5);
}

function formatAmount(amount) {
    if (amount === null || amount === undefined) {
        return "";
    }
    return `${Number(amount).toLocaleString("ko-KR")}원`;
}

function escapeHtml(value) {
    if (value === null || value === undefined) {
        return "";
    }
    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}
