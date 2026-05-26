import { qs, qsAll } from "../common/helpers.js";
import Controller from "./Controller.js";
import Store from "./Store.js";
import ReservationSectionView from "./views/ReservationSectionView.js";
import TabView from "./views/TabView.js";
import ThemeSectionView from "./views/ThemeSectionView.js";
import TimeSectionView from "./views/TimeSectionView.js";
import ToastView from "./views/ToastView.js";

document.addEventListener("DOMContentLoaded", () => {
  const store = new Store();
  const themePanel = qs('[data-panel="theme"]');
  const reservationPanel = qs('[data-panel="reservation"]');
  const timePanel = qs('[data-panel="time"]');

  const views = {
    tabView: new TabView(qs('[data-role="tab-bar"]'), qsAll(".tab-panel")),
    themeSectionView: new ThemeSectionView(themePanel),
    reservationSectionView: new ReservationSectionView(reservationPanel),
    timeSectionView: new TimeSectionView(timePanel),
    toastView: new ToastView(qs('[data-role="toast"]'))
  };

  const controller = new Controller(store, views);
  controller.initialize();
});
