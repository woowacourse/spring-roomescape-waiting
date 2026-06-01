import { createElement } from "./helpers.js";

export function createThemeImage(url, alt) {
  const image = createElement("img", "theme-image");
  image.alt = alt;
  image.src = url;
  image.addEventListener("error", () => {
    const placeholder = createElement("div", "img-placeholder", alt);
    image.replaceWith(placeholder);
  });
  return image;
}
