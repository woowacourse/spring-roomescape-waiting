import View from "../../common/View.js";
import {emit, formatCurrency, formatDateInputValue, on, qs} from "../../common/helpers.js";

export default class ReservationFormView extends View {
    constructor(element) {
        super(element);

        this.themeSelect = qs("#themeSelect", element);
        this.nameInput = qs("#nameInput", element);
        this.dateInput = qs("#dateInput", element);
        this.submitButton = qs('[data-role="submit-button"]', element);
        this.priceDisplay = qs('[data-role="price-display"]', element);

        this.bindEvents();
    }

    bindEvents() {
        on(this.themeSelect, "change", () => {
            this.emit("@themechange", {themeId: this.themeSelect.value});
        });

        on(this.dateInput, "change", () => {
            this.emit("@datechange", {date: this.dateInput.value});
        });

        on(this.nameInput, "input", () => {
            this.emit("@namechange", {name: this.nameInput.value});
        });

        on(this.element, "submit", (event) => {
            event.preventDefault();
            emit(this.element, "@submit");
        });
    }

    initializeDate(today = new Date()) {
        const value = formatDateInputValue(today);
        this.dateInput.min = value;
        this.dateInput.value = value;
        return value;
    }

    renderThemes(themes, selectedThemeId) {
        this.themeSelect.innerHTML = '<option value="">테마를 선택하세요</option>';

        themes.forEach((theme) => {
            const option = document.createElement("option");
            option.value = theme.id;
            option.textContent = theme.name;
            this.themeSelect.appendChild(option);
        });

        if (selectedThemeId) {
            this.themeSelect.value = String(selectedThemeId);
        }
    }

    sync({selectedThemeId, selectedThemePrice, selectedDate, name, canSubmit, readonly, isEdit, submitMode}) {
        this.themeSelect.value = selectedThemeId || "";
        this.dateInput.value = selectedDate || this.dateInput.value;
        this.nameInput.value = name;

        this.themeSelect.disabled = readonly;
        this.nameInput.disabled = readonly;

        this.priceDisplay.textContent = selectedThemePrice !== null
            ? formatCurrency(selectedThemePrice)
            : "테마를 선택하세요.";

        this.submitButton.textContent = submitMode === "waiting" ? "대기 등록" : isEdit ? "예약 변경" : "예약 확정";
        this.submitButton.disabled = !canSubmit;
    }
}
