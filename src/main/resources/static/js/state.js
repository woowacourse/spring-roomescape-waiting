export const ADMIN_TABS = new Set(["themes", "times"]);

export const state = {
    route: "reserve",
    adminTab: "themes",
    themes: [],
    popularThemes: [],
    adminTimes: [],
    myReservations: [],
    myPaymentOrders: [],
    myWaitings: [],
    availableTimes: [],
    selectedThemeId: null,
    selectedDate: tomorrowString(),
    selectedTimeId: null,
    editingReservationId: null,
    guestName: sessionStorage.getItem("roomescape.guestName") || "",
    themeQuery: "",
    loading: {
        boot: false,
        themes: false,
        times: false,
        reservations: false
    },
    submitting: false,
    toast: null,
    confirm: null
};

export function selectedTheme() {
    return state.themes.find((theme) => Number(theme.id) === Number(state.selectedThemeId)) || null;
}

export function selectedTime() {
    return state.availableTimes.find((time) => Number(time.id) === Number(state.selectedTimeId)) || null;
}

export function canSubmitReservation() {
    return Boolean(
        state.selectedThemeId &&
        state.selectedDate &&
        state.selectedTimeId &&
        state.guestName.trim() &&
        !state.submitting
    );
}

export function todayString() {
    return toDateInputValue(new Date());
}

export function tomorrowString() {
    const date = new Date();
    date.setDate(date.getDate() + 1);
    return toDateInputValue(date);
}

export function toDateInputValue(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");
    return `${year}-${month}-${day}`;
}
