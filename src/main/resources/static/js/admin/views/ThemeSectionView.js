import View from "../../common/View.js";
import {clearElement, delegate, emit, formatCurrency, qs} from "../../common/helpers.js";

export default class ThemeSectionView extends View {
    constructor(element) {
        super(element);
        this.form = qs('[data-role="theme-form"]', element);
        this.tableBody = qs('[data-role="theme-table"]', element);
        this.bindEvents();
    }

    bindEvents() {
        this.form.addEventListener("submit", (event) => {
            event.preventDefault();
            const formData = new FormData(this.form);
            emit(this.element, "@create-theme", {
                name: String(formData.get("name") || "").trim(),
                description: String(formData.get("description") || "").trim(),
                thumbnailImageUrl: String(formData.get("thumbnailImageUrl") || "").trim(),
                price: Number(formData.get("price"))
            });
        });

        delegate(this.tableBody, "click", ".btn-toggle", (event) => {
            const button = event.target.closest(".btn-toggle");
            const id = Number(button.dataset.id);
            const status = button.dataset.status === "true";

            emit(this.element, "@toggle-theme", {id, status});
        });
    }

    resetForm() {
        this.form.reset();
    }

    render(themes) {
        clearElement(this.tableBody);

        if (!themes.length) {
            this.tableBody.innerHTML = '<tr class="empty-row"><td colspan="5">등록된 테마가 없습니다.</td></tr>';
            return;
        }

        themes.forEach((theme) => {
            const isActive = theme.isActive;
            const row = document.createElement("tr");
            row.innerHTML = `
        <td><span class="badge badge-gray">${theme.id}</span></td>
        <td class="td-name ${isActive ? '' : 'text-gray'}">${theme.name}</td>
        <td class="td-description">${theme.description}</td>
        <td class="td-price">${formatCurrency(theme.price)}</td>
        <td>
          <button class="btn-toggle ${isActive ? 'btn-deactivate' : 'btn-activate'}" 
                  type="button" 
                  data-id="${theme.id}" 
                  data-status="${theme.isActive}">
            ${isActive ? "비활성화" : "활성화"}
          </button>
        </td>
      `;
            this.tableBody.appendChild(row);
        });
    }
}
