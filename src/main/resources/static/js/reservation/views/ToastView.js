import View from "../../common/View.js";

export default class ToastView extends View {
  show(message, isError = false) {
    this.element.textContent = message;
    this.element.className = `toast show${isError ? " error" : ""}`;

    window.clearTimeout(this.timeoutId);
    this.timeoutId = window.setTimeout(() => {
      this.element.className = "toast";
    }, 3000);
  }
}
