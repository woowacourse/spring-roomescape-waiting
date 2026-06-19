import View from "../../common/View.js";
import {on, qs} from "../../common/helpers.js";

export default class OrdersFormView extends View {
    constructor(element) {
        super(element);
        this.nameInput = qs('[name="name"]', element);
        this.bindEvents();
    }

    bindEvents() {
        on(this.element, "submit", (event) => {
            event.preventDefault();
            this.emit("@search", {name: this.nameInput.value.trim()});
        });
    }

    sync({name}) {
        this.nameInput.value = name || "";
    }
}
