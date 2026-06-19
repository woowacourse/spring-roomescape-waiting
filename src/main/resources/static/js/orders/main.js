import {qs} from "../common/helpers.js";

import Store from "./Store.js";
import Controller from "./Controller.js";

import OrdersFormView from "./views/OrdersFormView.js";
import OrdersResultView from "./views/OrdersResultView.js";
import ToastView from "./views/ToastView.js";

document.addEventListener("DOMContentLoaded", () => {
    const store = new Store();

    const views = {
        formView: new OrdersFormView(qs('[data-role="orders-form"]')),
        resultView: new OrdersResultView(qs('[data-role="orders-results"]')),
        toastView: new ToastView(qs('[data-role="toast"]'))
    };

    new Controller(store, views).initialize();
});
