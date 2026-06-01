import {
    activateTheme,
    activateTime,
    createReservation,
    createTheme,
    createTime,
    deactivateTheme,
    deactivateTime,
    deleteReservation,
    fetchReservations,
    fetchThemes,
    fetchTimes
} from "./api.js";

export const TabType = {
    THEME: "theme",
    RESERVATION: "reservation",
    TIME: "time"
};

export default class Store {
    constructor() {
        this.selectedTab = TabType.THEME;
        this.themes = [];
        this.reservations = [];
        this.times = [];
    }

    selectTab(tab) {
        this.selectedTab = tab;
    }

    async loadAll() {
        const [themes, reservations, times] = await Promise.all([
            fetchThemes(),
            fetchReservations(),
            fetchTimes()
        ]);

        console.log(" +==========", times);
        this.themes = themes;
        this.reservations = reservations;
        this.times = times;
    }

    addTheme(payload) {
        return createTheme(payload);
    }

    async toggleThemeStatus(id, currentStatus) {
        if (currentStatus) {
            await deactivateTheme(id);
        } else {
            await activateTheme(id);
        }
    }

    addReservation(payload) {
        return createReservation(payload);
    }

    removeReservation(id) {
        return deleteReservation(id);
    }

    addTime(payload) {
        return createTime(payload);
    }

    async toggleTimeStatus(id, currentStatus) {
        console.log(id, currentStatus);
        if (currentStatus === "ACTIVE") {
            await deactivateTime(id);
        } else {
            await activateTime(id);
        }
    }
}
