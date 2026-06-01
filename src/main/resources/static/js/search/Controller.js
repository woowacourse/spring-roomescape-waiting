export default class Controller {
    constructor(store, views) {
        this.store = store;
        this.views = views;

        this.subscribeViewEvents();
    }

    subscribeViewEvents() {
        this.views.formView.on("@search", async (event) => {
            try {
                await this.store.search(event.detail.name);

                if (!this.store.page.content.length) {
                    this.views.toastView.show("예약 내역이 없습니다.");
                }

                this.render();
            } catch (error) {
                this.views.toastView.show(error.message);
            }
        });

        this.views.resultView.on("@change", (event) => {
            location.href = `/reserve?id=${event.detail.id}`;
        });

        this.views.resultView.on("@cancel", async (event) => {
            try {
                await this.store.cancel(event.detail.id);
                this.render();
            } catch (error) {
                this.views.toastView.show(error.message);
            }
        });
    }

    initialize() {
        this.render();
    }

    render() {
        this.views.formView.sync({
            name: this.store.name
        });

        this.views.resultView.render(this.store.page);
    }
}