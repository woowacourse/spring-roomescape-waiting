export default class Controller {
  constructor(store, views) {
    this.store = store;
    this.views = views;
    this.isThemeOptionsRendered = false;

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
        await this.store.submit();

        const submitMode = this.store.submitMode();
        this.views.toastView.show(
            this.store.reservationId
                ? submitMode === "waiting" ? "대기가 등록되었습니다." : "예약이 변경되었습니다."
                : submitMode === "waiting" ? "대기가 등록되었습니다." : "예약이 완료되었습니다."
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
    if (!this.isThemeOptionsRendered) {
      this.views.formView.renderThemes(
          this.store.themes,
          this.store.selectedThemeId
      );
      this.isThemeOptionsRendered = true;
    }

    this.views.formView.sync({
      selectedThemeId: this.store.selectedThemeId,
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
