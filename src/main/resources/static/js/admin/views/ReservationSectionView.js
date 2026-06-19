import View from "../../common/View.js";
import { clearElement, delegate, emit, formatDateInputValue, formatPrice, formatTime, qs } from "../../common/helpers.js";

export default class ReservationSectionView extends View {
  constructor(element) {
    super(element);
    this.form = qs('[data-role="reservation-form"]', element);
    this.tableBody = qs('[data-role="reservation-table"]', element);
    this.themeSelect = qs("#reservationTheme", element);
    this.timeSelect = qs("#reservationTime", element);
    this.dateInput = qs("#reservationDate", element);
    this.bindEvents();
  }

  bindEvents() {
    this.form.addEventListener("submit", (event) => {
      event.preventDefault();
      const formData = new FormData(this.form);
      emit(this.element, "@create-reservation", {
        name: String(formData.get("name") || "").trim(),
        date: String(formData.get("date") || ""),
        themeId: Number(formData.get("themeId")),
        timeId: Number(formData.get("timeId"))
      });
    });

    delegate(this.tableBody, "click", ".btn-delete", function () {
      emit(this.closest('[data-panel="reservation"]'), "@delete-reservation", {
        id: Number(this.dataset.id)
      });
    });
  }

  initializeDate(today = new Date()) {
    const date = formatDateInputValue(today);
    this.dateInput.min = date;
    this.dateInput.value = date;
  }

  resetForm() {
    const preservedDate = this.dateInput.value;
    const preservedThemeId = this.themeSelect.value;
    const preservedTimeId = this.timeSelect.value;
    this.form.reset();
    this.dateInput.value = preservedDate;
    this.themeSelect.value = preservedThemeId;
    this.timeSelect.value = preservedTimeId;
  }

  renderOptions(themes, times) {
    const selectedThemeId = this.themeSelect.value;
    const selectedTimeId = this.timeSelect.value;

    this.themeSelect.innerHTML = '<option value="">테마 선택</option>';
    themes.forEach((theme) => {
      const option = document.createElement("option");
      option.value = theme.id;
      option.textContent = `${theme.name} (${formatPrice(theme.price)})`;
      this.themeSelect.appendChild(option);
    });

    this.timeSelect.innerHTML = '<option value="">시간 선택</option>';
    times.forEach((time) => {
      const option = document.createElement("option");
      option.value = time.id;
      option.textContent = formatTime(time.startAt);
      this.timeSelect.appendChild(option);
    });

    this.themeSelect.value = selectedThemeId;
    this.timeSelect.value = selectedTimeId;
  }

  render(reservations) {
    clearElement(this.tableBody);

    if (!reservations.length) {
      this.tableBody.innerHTML = '<tr class="empty-row"><td colspan="5">예약이 없습니다.</td></tr>';
      return;
    }

    reservations.forEach((slot) => {
      const reservation = slot.reservation;
      const row = document.createElement("tr");
      row.innerHTML = `
        <td><span class="badge badge-blue">${reservation.id}</span></td>
        <td class="td-name">${reservation.name}</td>
        <td>${slot.date}</td>
        <td>${formatTime(slot.time.startAt)}</td>
        <td><button class="btn-delete" type="button" data-id="${reservation.id}">취소</button></td>
      `;
      this.tableBody.appendChild(row);
    });
  }
}
