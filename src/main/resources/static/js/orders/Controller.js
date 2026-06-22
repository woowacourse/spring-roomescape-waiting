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

                if (!this.store.orders || !this.store.orders.length) {
                    this.views.toastView.show("주문/결제 내역이 없습니다.");
                }

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

        this.views.resultView.render(this.store.orders);
    }
}
