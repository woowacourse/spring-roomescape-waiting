import View from "../../common/View.js";
import {clearElement, createElement, delegate} from "../../common/helpers.js";

export default class SearchResultView extends View {
    constructor(element) {
        super(element);
        this.bindEvents();
    }

    bindEvents() {
        delegate(
            this.element,
            "click",
            ".btn-change:not(:disabled)",
            (event) => {
                const id = event.target.closest(".reservation-item").dataset.id;
                this.emit("@change", { id });
            }
        );

        delegate(
            this.element,
            "click",
            ".btn-cancel:not(:disabled)",
            (event) => {
                const id = event.target.closest(".reservation-item").dataset.id;
                this.emit("@cancel", { id });
            }
        );
    }

    render(page) {
        clearElement(this.element);

        if (!page || !page.content.length) {
            this.element.innerHTML = `
                <div class="empty-state">예약 내역을 검색하세요</div>
            `;
            return;
        }

        page.content.forEach((slot) => {
            const item = createElement("div", "reservation-item");
            const isWaiting = slot.status === "WAITING";
            const cancellationRestricted = !isWaiting && this.isCancellationRestricted(slot.date);
            const paymentLabel = this.paymentLabel(slot);
            const paymentClass = this.paymentClass(slot);

            item.dataset.id = slot.id;

            item.innerHTML = `
                <div class="res-main">
                    <div class="res-title-row">
                        <div class="res-theme">${slot.theme}</div>
                        <span class="res-status ${isWaiting ? "waiting" : "reserved"}">
                            ${isWaiting ? `대기 ${slot.waitingRank}번` : "예약"}
                        </span>
                        <span class="payment-status ${paymentClass}">${paymentLabel}</span>
                    </div>

                    <div class="res-details">
                        ${slot.date}
                        ·
                        ${slot.startAt.slice(0, 5)}
                        ·
                        ${slot.name}
                    </div>

                    <div class="payment-details">
                        <span>주문번호 ${slot.orderId || "-"}</span>
                        <span>결제키 ${slot.paymentKey || "-"}</span>
                        <span>금액 ${slot.paymentAmount ? Number(slot.paymentAmount).toLocaleString("ko-KR") + "원" : "-"}</span>
                    </div>
                </div>

                <div class="res-actions">
                    <button
                        class="btn-change"
                        ${cancellationRestricted || isWaiting ? "disabled" : ""}
                    >
                        변경
                    </button>

                    <button
                        class="btn-cancel"
                        ${cancellationRestricted ? "disabled" : ""}
                        title="${cancellationRestricted ? "예약일 하루 전부터는 취소할 수 없습니다." : ""}"
                    >
                        취소
                    </button>
                </div>
            `;

            this.element.appendChild(item);
        });
    }

    paymentLabel(slot) {
        if (slot.paymentStatus === "DONE" || slot.orderStatus === "PAID") {
            return "결제 확정";
        }
        if (slot.paymentStatus === "CHECK_REQUIRED") {
            return "확인 필요";
        }
        if (slot.paymentStatus === "FAILED" || slot.orderStatus === "FAILED") {
            return "결제 실패";
        }
        return "결제 대기";
    }

    paymentClass(slot) {
        if (slot.paymentStatus === "DONE" || slot.orderStatus === "PAID") {
            return "paid";
        }
        if (slot.paymentStatus === "CHECK_REQUIRED") {
            return "check-required";
        }
        if (slot.paymentStatus === "FAILED" || slot.orderStatus === "FAILED") {
            return "failed";
        }
        return "pending";
    }

    isCancellationRestricted(date) {
        const [year, month, day] = date.split("-").map(Number);

        const reservationDate = new Date(year, month - 1, day);
        const tomorrow = new Date();
        tomorrow.setHours(0, 0, 0, 0);
        tomorrow.setDate(tomorrow.getDate() + 1);

        return reservationDate <= tomorrow;
    }
}
