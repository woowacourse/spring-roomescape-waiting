import {getSearchParam} from "../common/helpers.js";
import {changeReservation, createReservation, fetchReservation, fetchThemes, fetchThemeSlots} from "./api.js";

export default class Store {
  constructor() {
    this.themes = [];
    this.slots = [];
    this.selectedThemeId = getSearchParam("themeId") || "";
    this.selectedDate = "";
    this.selectedTimeId = null;
    this.name = "";
    this.reservationId = getSearchParam("id");
    this.readonly = false;
  }

  async initialize() {
    await this.loadThemes();

    if (this.reservationId) {
      const reservation = await fetchReservation(this.reservationId);

      this.selectedThemeId = reservation.themeId;
      this.selectedDate = reservation.date;
      this.selectedTimeId = reservation.time.id;
      this.name = reservation.name;
      this.readonly = true;

      await this.loadSlots();
    }
  }

  async loadThemes() {
    this.themes = await fetchThemes();
  }

  async loadSlots() {
    if (!this.selectedThemeId || !this.selectedDate) {
      this.slots = [];
      this.selectedTimeId = null;
      return;
    }

    this.slots = await fetchThemeSlots(this.selectedThemeId, this.selectedDate);
  }

  setThemeId(themeId) {
    this.selectedThemeId = themeId;
    this.selectedTimeId = null;
  }

  setDate(date) {
    this.selectedDate = date;
    this.selectedTimeId = null;
  }

  setName(name) {
    this.name = name.trim();
  }

  setSelectedTimeId(timeId) {
    this.selectedTimeId = timeId;
  }

  canSubmit() {
    return Boolean(
        this.selectedThemeId &&
        this.selectedDate &&
        this.name &&
        this.selectedTimeId
    );
  }

  submit() {
    const payload = {
      name: this.name,
      date: this.selectedDate,
      themeId: Number(this.selectedThemeId),
      timeId: this.selectedTimeId
    };

    if (this.reservationId) {
      return changeReservation(this.reservationId, {
        date: payload.date,
        timeId: payload.timeId
      });
    }

    return createReservation(payload);
  }
}