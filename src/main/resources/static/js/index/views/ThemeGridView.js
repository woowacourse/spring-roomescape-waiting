import View from "../../common/View.js";
import { clearElement, createElement, formatPrice } from "../../common/helpers.js";
import { createThemeImage } from "../../common/image.js";

export default class ThemeGridView extends View {
  showLoading() {
    this.element.innerHTML = '<div class="state-box state-grid">불러오는 중...</div>';
  }

  showError() {
    this.element.innerHTML = '<div class="state-box state-grid">데이터를 불러올 수 없습니다.</div>';
  }

  render(themes) {
    clearElement(this.element);

    if (!themes.length) {
      this.element.innerHTML = '<div class="state-box state-grid">등록된 테마가 없습니다.</div>';
      return;
    }

    themes.forEach((theme) => {
      const reservePath = `/reserve?themeId=${theme.id}`;
      const card = createElement("article", "theme-card");
      const media = createElement("div", "theme-card-media");
      const body = createElement("div", "theme-card-body");
      const footer = createElement("div", "theme-card-footer");
      const reserveButton = createElement("button", "btn-reserve", "예약하기");

      media.appendChild(createThemeImage(theme.thumbnailImageUrl, theme.name));

      const name = createElement("div", "theme-card-name", theme.name);
      const price = createElement("div", "theme-card-price", formatPrice(theme.price));
      const description = createElement("div", "theme-card-desc", theme.description);
      body.append(name, price, description);

      card.addEventListener("click", () => {
        window.location.href = reservePath;
      });

      reserveButton.type = "button";
      reserveButton.addEventListener("click", (event) => {
        event.stopPropagation();
        window.location.href = reservePath;
      });
      footer.appendChild(reserveButton);

      card.append(media, body, footer);
      this.element.appendChild(card);
    });
  }
}
