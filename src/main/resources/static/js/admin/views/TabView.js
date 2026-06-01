import View from "../../common/View.js";
import { delegate, qsAll } from "../../common/helpers.js";

export default class TabView extends View {
  constructor(element, panels) {
    super(element);
    this.panels = panels;
    delegate(this.element, "click", ".tab-btn", function () {
      this.dispatchEvent(new CustomEvent("@tabchange", {
        bubbles: true,
        detail: { tab: this.dataset.tab }
      }));
    });
  }

  render(selectedTab) {
    qsAll(".tab-btn", this.element).forEach((button) => {
      button.classList.toggle("active", button.dataset.tab === selectedTab);
    });

    this.panels.forEach((panel) => {
      panel.classList.toggle("active", panel.dataset.panel === selectedTab);
    });
  }
}
