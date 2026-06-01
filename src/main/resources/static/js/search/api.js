import {requestJson} from "../common/http.js";

export function searchReservations(name, page = 1, size = 20) {
    return requestJson(
        `/api/reservations?name=${encodeURIComponent(name)}&page=${page}&size=${size}`
    );
}

export function cancelReservation(id) {
    return requestJson(`/api/reservations/${id}`, {
        method: "DELETE"
    });
}
