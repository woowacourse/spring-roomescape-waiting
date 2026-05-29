import {cancelReservation, searchReservations} from "./api.js";

export default class Store {
    constructor() {
        this.name = "";
        this.page = null;
    }

    async search(name, page = 1) {
        this.name = name;
        this.page = await searchReservations(name, page);
    }

    async cancel(id) {
        await cancelReservation(id);
        this.page.content = this.page.content.filter((slot) => slot.id !== Number(id));
    }
}