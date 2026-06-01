import {qs} from "../common/helpers.js";

import Store from "./Store.js";
import Controller from "./Controller.js";

import SearchFormView from "./views/SearchFormView.js";
import SearchResultView from "./views/SearchResultView.js";
import ToastView from "./views/ToastView.js";

document.addEventListener("DOMContentLoaded", () => {
    const store = new Store();

    const views = {
        formView: new SearchFormView(qs('[data-role="search-form"]')),
        resultView: new SearchResultView(qs('[data-role="search-results"]')),
        toastView: new ToastView(qs('[data-role="toast"]'))
    };

    new Controller(store, views).initialize();
});