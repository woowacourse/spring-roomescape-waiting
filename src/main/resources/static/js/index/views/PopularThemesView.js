import View from "../../common/View.js";
import { clearElement, createElement, formatPrice } from "../../common/helpers.js";
import { createThemeImage } from "../../common/image.js";

export default class PopularThemesView extends View {
  showLoading() {
    this.element.innerHTML = '<div class="state-box">불러오는 중...</div>';
  }

  showError() {
    this.element.innerHTML = '<div class="state-box">데이터를 불러올 수 없습니다.</div>';
  }

  render(themes) {
    clearElement(this.element);

    if (!themes.length) {
      this.element.innerHTML = '<div class="state-box">인기 테마가 없습니다.</div>';
      return;
    }

    themes.forEach((theme, index) => {
      const card = createElement("a", "popular-card");
      card.href = `/reserve?themeId=${theme.id}`;

      const media = createElement("div", "popular-card-media");
      media.appendChild(createThemeImage(theme.thumbnailImageUrl, theme.name));

      const body = createElement("div", "popular-card-body");
      const rank = createElement("span", "popular-rank", `${index + 1}위`);
      const name = createElement("div", "popular-card-name", theme.name);
      const price = createElement("div", "popular-card-price", formatPrice(theme.price));
      const description = createElement("div", "popular-card-desc", theme.description);

      body.append(rank, name, price, description);
      card.append(media, body);
      this.element.appendChild(card);
    });
  }
}
