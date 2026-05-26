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

        page.content.forEach((reservation) => {
            const item = createElement("div", "reservation-item");
            const expired = this.isPastReservation(
                reservation.date,
                reservation.startAt
            );

            item.dataset.id = reservation.id;

            item.innerHTML = `
                <div>
                    <div class="res-theme">${reservation.theme}</div>

                    <div class="res-details">
                        ${reservation.date}
                        ·
                        ${reservation.startAt.slice(0, 5)}
                        ·
                        ${reservation.name}
                    </div>
                </div>

                <div class="res-actions">
                    <button
                        class="btn-change"
                        ${expired ? "disabled" : ""}
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