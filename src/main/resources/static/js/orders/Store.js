import {fetchOrders} from "./api.js";

export default class Store {
    constructor() {
        this.name = "";
        this.orders = null;
    }

    async search(name) {
        this.name = name;
        this.orders = await fetchOrders(name);
    }
}
