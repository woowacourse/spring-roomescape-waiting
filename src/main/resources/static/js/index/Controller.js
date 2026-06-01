export default class Controller {
    constructor(store, views) {
        this.store = store;
        this.views = views;
    }

    async initialize() {
        this.views.popularThemesView.showLoading();
        this.views.themeGridView.showLoading();

        try {
            await this.store.load();
            this.views.popularThemesView.render(this.store.popularThemes);
            this.views.themeGridView.render(this.store.themes);
        } catch (error) {
            this.views.popularThemesView.showError();
            this.views.themeGridView.showError();
        }
    }
}
