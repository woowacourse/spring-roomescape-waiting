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
            const expired = this.isPastReservation(
                slot.date,
                slot.startAt
            );

            item.dataset.id = slot.id;

            item.innerHTML = `
                <div>
                    <div class="res-title-row">
                        <div class="res-theme">${slot.theme}</div>
                        <span class="res-status ${isWaiting ? "waiting" : "reserved"}">
                            ${isWaiting ? `대기 ${slot.waitingRank}번` : "예약"}
                        </span>
                    </div>

                    <div class="res-details">
                        ${slot.date}
                        ·
                        ${slot.startAt.slice(0, 5)}
                        ·
                        ${slot.name}
                    </div>
                </div>

                <div class="res-actions">
                    <button
                        class="btn-change"
                        ${expired || isWaiting ? "disabled" : ""}
                    >
                        변경
                    </button>

                    <button
                        class="btn-cancel"
                        ${expired ? "disabled" : ""}
                    >
                        취소
                    </button>
                </div>
            `;

            this.element.appendChild(item);
        });
    }

    isPastReservation(date, startAt) {
        const [year, month, day] = date.split("-").map(Number);
        const [hour, minute] = startAt.split(":").map(Number);

        const reservationDateTime = new Date(
            year,
            month - 1,
            day,
            hour,
            minute
        );

        return reservationDateTime < new Date();
    }
}
