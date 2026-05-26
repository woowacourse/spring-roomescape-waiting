import View from "../../common/View.js";
import {clearElement, delegate, emit, formatTime, qs} from "../../common/helpers.js";

export default class TimeSectionView extends View {
  constructor(element) {
    super(element);
    this.form = qs('[data-role="time-form"]', element);
    this.tableBody = qs('[data-role="time-table"]', element);
    this.bindEvents();
  }

  bindEvents() {
    this.form.addEventListener("submit", (event) => {
      event.preventDefault();
      const formData = new FormData(this.form);
      emit(this.element, "@create-time", {
        startAt: String(formData.get("startAt") || "")
      });
    });

    delegate(this.tableBody, "click", ".btn-toggle", (event) => {
      const button = event.target.closest(".btn-toggle");
      const id = Number(button.dataset.id);
      const status = button.dataset.status;

      emit(this.element, "@toggle-time", { id, status });
    });
  }

  resetForm() {
    this.form.reset();
  }

  render(times) {
    console.log(times);
    clearElement(this.tableBody);

    if (!times.length) {
      this.tableBody.innerHTML = '<tr class="empty-row"><td colspan="3">등록된 시간이 없습니다.</td></tr>';
      return;
    }

    times.forEach((time) => {
      const isActive = time.status === "ACTIVE";
      const row = document.createElement("tr");
      row.innerHTML = `
      <td><span class="badge badge-gray">${time.id}</span></td>
      <td class="td-name ${isActive ? '' : 'text-gray'}">${formatTime(time.startAt)}</td>
      <td>
        <button class="btn-toggle ${isActive ? 'btn-deactivate' : 'btn-activate'}" 
                type="button" 
                data-id="${time.id}" 
                data-status="${time.status}">
          ${isActive ? "비활성화" : "활성화"}
        </button>
      </td>
    `;
      this.tableBody.appendChild(row);
    });
  }
}
