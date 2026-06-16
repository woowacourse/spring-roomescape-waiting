import {getSearchParam} from "../common/helpers.js";
import {
    changeReservation,
    createReservation,
    createWaitingReservation,
    fetchReservation,
    fetchThemes,
    fetchThemeSlots
} from "./api.js";

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
        this.originalReservation = null;
    }

    async initialize() {
        await this.loadThemes();

        if (this.reservationId) {
            const reservation = await fetchReservation(this.reservationId);

            this.selectedThemeId = reservation.theme.id;
            this.selectedDate = reservation.date;
            this.selectedTimeId = reservation.time.id;
            this.name = reservation.entry.name;
            this.readonly = true;
            this.originalReservation = reservation;

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

    get selectedThemePrice() {
        const theme = this.themes.find((t) => String(t.id) === String(this.selectedThemeId));
        return theme ? theme.price : null;
    }

    canSubmit() {
        const slot = this.selectedSlot();
        return Boolean(
            this.selectedThemeId &&
            this.selectedDate &&
            this.name &&
            this.selectedTimeId &&
            slot &&
            slot.status !== "UNAVAILABLE"
        );
    }

    selectedSlot() {
        return this.slots.find((slot) => slot.id === this.selectedTimeId) || null;
    }

    submitMode() {
        const slot = this.selectedSlot();
        if (!slot) {
            return "reserve";
        }
        if (this.isSameAsOriginalReservation()) {
            return "change";
        }
        return slot.status === "WAITING_AVAILABLE" ? "waiting" : "reserve";
    }

    isSameAsOriginalReservation() {
        return Boolean(
            this.originalReservation &&
            Number(this.selectedThemeId) === this.originalReservation.theme.id &&
            this.selectedDate === this.originalReservation.date &&
            this.selectedTimeId === this.originalReservation.time.id
        );
    }

    async submit() {
        const theme = this.themes.find((t) => String(t.id) === String(this.selectedThemeId));
        const payload = {
            name: this.name,
            date: this.selectedDate,
            themeId: Number(this.selectedThemeId),
            timeId: this.selectedTimeId,
            amount: theme ? theme.price : 0
        };

        if (this.reservationId) {
            return changeReservation(this.reservationId, {
                date: payload.date,
                timeId: payload.timeId
            });
        }

        if (this.submitMode() === "waiting") {
            return createWaitingReservation(payload);
        }

        return createReservation(payload);
    }
}
