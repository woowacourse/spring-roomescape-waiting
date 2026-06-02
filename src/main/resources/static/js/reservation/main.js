import {qs} from "../common/helpers.js";
import Controller from "./Controller.js";
import Store from "./Store.js";
import ReservationFormView from "./views/ReservationFormView.js";
import SlotGridView from "./views/SlotGridView.js";
import ToastView from "./views/ToastView.js";

document.addEventListener("DOMContentLoaded", () => {
    const store = new Store();
    const views = {
        formView: new ReservationFormView(qs('[data-role="reservation-form"]')),
        slotGridView: new SlotGridView(qs('[data-role="slot-grid"]')),
        toastView: new ToastView(qs('[data-role="toast"]'))
    };

    const controller = new Controller(store, views);
    controller.initialize();
});
