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

    isCancellationRestricted(date) {
        const [year, month, day] = date.split("-").map(Number);

        const reservationDate = new Date(year, month - 1, day);
        const tomorrow = new Date();
        tomorrow.setHours(0, 0, 0, 0);
        tomorrow.setDate(tomorrow.getDate() + 1);

        return reservationDate <= tomorrow;
    }
}
