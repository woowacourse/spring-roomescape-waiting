import {formatDateInputValue} from "../common/helpers.js";
import {fetchPopularThemes, fetchThemes} from "./api.js";

export default class Store {
    constructor() {
        this.popularThemes = [];
        this.themes = [];
    }

    async load() {
        const today = new Date();
        const weekAgo = new Date(today);
        weekAgo.setDate(today.getDate() - 7);

        const [popularThemes, themes] = await Promise.all([
            fetchPopularThemes(formatDateInputValue(weekAgo), formatDateInputValue(today)),
            fetchThemes()
        ]);

        this.popularThemes = popularThemes;
        this.themes = themes;
    }
}
