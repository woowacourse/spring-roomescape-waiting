import View from "../../common/View.js";

export default class ToastView extends View {
    show(message, type = "") {
        this.element.textContent = message;
        this.element.className = `toast show${type ? ` ${type}` : ""}`;

        window.clearTimeout(this.timeoutId);
        this.timeoutId = window.setTimeout(() => {
            this.element.className = "toast";
        }, 3000);
    }
}
