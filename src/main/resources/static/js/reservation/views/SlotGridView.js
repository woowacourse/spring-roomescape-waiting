import View from "../../common/View.js";
import { clearElement, createElement, delegate, formatTime } from "../../common/helpers.js";

export default class SlotGridView extends View {
  constructor(element) {
    super(element);
    delegate(this.element, "click", ".slot-btn:not(.disabled)", (event) => {
      const timeId = Number(event.target.closest(".slot-btn").dataset.id);
      this.emit("@slotselect", { timeId });
    });
  }

  showIdle() {
    this.element.innerHTML = '<div class="slot-empty">테마와 날짜를 먼저 선택하세요.</div>';
  }

  showLoading() {
    this.element.innerHTML = '<div class="slot-empty">불러오는 중...</div>';
  }

  showError() {
    this.element.innerHTML = '<div class="slot-empty">불러오기 실패</div>';
  }

  render(slots, selectedTimeId) {
    clearElement(this.element);

    if (!slots.length) {
      this.element.innerHTML = '<div class="slot-empty">등록된 시간이 없습니다.</div>';
      return;
    }

    slots.forEach((slot) => {
      const status = slot.status || (slot.isReservable ? "RESERVABLE" : "WAITING_AVAILABLE");
      const button = createElement("button", "slot-btn");
      button.type = "button";
      button.dataset.id = slot.id;
      button.appendChild(createElement("span", "slot-time", formatTime(slot.startAt)));
      button.appendChild(createElement("span", "slot-status", this.statusText(status)));

      if (status === "WAITING_AVAILABLE") {
        button.classList.add("waiting");
      }

      if (status === "UNAVAILABLE") {
        button.classList.add("disabled");
        button.disabled = true;
      }

      if (slot.id === selectedTimeId) {
        button.classList.add("selected");
      }

      this.element.appendChild(button);
    });
  }

  statusText(status) {
    if (status === "WAITING_AVAILABLE") {
      return "대기 가능";
    }
    if (status === "UNAVAILABLE") {
      return "마감";
    }
    return "예약 가능";
  }
}
