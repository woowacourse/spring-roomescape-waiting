import {qs} from "../common/helpers.js";
import Controller from "./Controller.js";
import Store from "./Store.js";
import PopularThemesView from "./views/PopularThemesView.js";
import ThemeGridView from "./views/ThemeGridView.js";

document.addEventListener("DOMContentLoaded", () => {
    const store = new Store();
    const views = {
        popularThemesView: new PopularThemesView(qs('[data-role="popular-list"]')),
        themeGridView: new ThemeGridView(qs('[data-role="theme-grid"]'))
    };

    const controller = new Controller(store, views);
    controller.initialize();
});
