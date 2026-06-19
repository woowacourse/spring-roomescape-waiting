export const ADMIN_TABS = new Set(["themes", "times", "reservations"]);

export const state = {
    route: "reserve",
    adminTab: "themes",
    themes: [],
    popularThemes: [],
    adminTimes: [],
    reservations: [],
    searchedReservations: [],
    searchedWaitings: [],
    availableTimes: [],
    selectedThemeId: null,
    selectedDate: tomorrowString(),
    selectedTimeId: null,
    guestName: "",
    reservationSearchName: "",
    reservationSearchSubmitted: false,
    reservationSearchSubmittedName: "",
    reservationEdit: {
        id: null,
        date: "",
        timeId: null,
        times: [],
        loading: false
    },
    themeQuery: "",
    loading: {
        boot: false,
        themes: false,
        times: false,
        reservations: false,
        searchedReservations: false
    },
    submitting: false,
    payment: {
        pendingContext: null,
        processing: {
            status: "idle",
            orderId: "",
            paymentKey: "",
            amount: null,
            message: ""
        },
        failure: {
            code: "",
            message: "",
            orderId: ""
        },
        result: null,
        config: null,
        configPromise: null,
        configStatus: "idle",
        configError: "",
        tossPayments: null,
        widgets: null,
        paymentWindow: null,
        tossClientKey: "",
        sdkError: ""
    },
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

export function isPastReservation(reservation, now = new Date()) {
    const startAt = reservation?.time?.startAt;

    if (!reservation?.date || !startAt) {
        return false;
    }

    const [year, month, day] = reservation.date.split("-").map(Number);
    const [hour, minute] = startAt.split(":").map(Number);
    const reservationDateTime = new Date(year, month - 1, day, hour, minute);

    return reservationDateTime < now;
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
