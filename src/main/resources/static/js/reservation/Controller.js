export default class Controller {
    constructor(store, views) {
        this.store = store;
        this.views = views;

        this.subscribeViewEvents();
    }

    subscribeViewEvents() {
        this.views.formView.on("@themechange", async (event) => {
            this.store.setThemeId(event.detail.themeId);

            this.views.slotGridView.showLoading();

            try {
                await this.store.loadSlots();
                this.render();
            } catch (error) {
                this.views.slotGridView.showError();
                this.views.toastView.show(error.message);
            }
        });

        this.views.formView.on("@datechange", async (event) => {
            this.store.setDate(event.detail.date);

            this.views.slotGridView.showLoading();

            try {
                await this.store.loadSlots();
                this.render();
            } catch (error) {
                this.views.slotGridView.showError();
                this.views.toastView.show(error.message);
            }
        });

        this.views.formView.on("@namechange", (event) => {
            this.store.setName(event.detail.name);
            this.render();
        });

        this.views.slotGridView.on("@slotselect", (event) => {
            this.store.setSelectedTimeId(event.detail.timeId);
            this.render();
        });

        this.views.formView.on("@submit", async () => {
            try {
                const wasWaiting = this.store.submitMode() === "waiting";
                const result = await this.store.submit();
                const isReserved = result.entry.status === "RESERVED";

                this.views.toastView.show(
                    this.store.reservationId
                        ? isReserved ? "예약이 변경되었습니다." : "대기가 등록되었습니다."
                        : wasWaiting && isReserved ? "기존 예약이 취소되어 예약으로 전환되었습니다."
                            : isReserved ? "예약이 완료되었습니다." : "대기가 등록되었습니다."
                );

                location.href = this.store.reservationId ? "/search" : "/";
            } catch (error) {
                this.views.toastView.show(error.message);
            }
        });
    }

    async initialize() {
        try {
            await this.store.initialize();

            if (!this.store.selectedThemeId || !this.store.selectedDate) {
                this.views.slotGridView.showIdle();
            }

            this.render();
        } catch (error) {
            this.views.slotGridView.showError();
            this.views.toastView.show(error.message);
        }
    }

    render() {
        this.views.formView.renderThemes(
            this.store.themes,
            this.store.selectedThemeId
        );

        this.views.formView.sync({
            selectedThemeId: this.store.selectedThemeId,
            selectedThemePrice: this.store.selectedThemePrice,
            selectedDate: this.store.selectedDate,
            name: this.store.name,
            canSubmit: this.store.canSubmit(),
            readonly: this.store.readonly,
            isEdit: Boolean(this.store.reservationId),
            submitMode: this.store.submitMode()
        });

        this.views.slotGridView.render(
            this.store.slots,
            this.store.selectedTimeId
        );
    }
}
