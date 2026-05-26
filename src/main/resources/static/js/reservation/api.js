import {requestJson} from "../common/http.js";

export function fetchThemes() {
  return requestJson("/api/themes");
}

export function fetchThemeSlots(themeId, date) {
  return requestJson(`/api/themes/${themeId}/times?date=${date}`);
}

export function createReservation(payload) {
  return requestJson("/api/reservations", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}

export function fetchReservation(id) {
  return requestJson(`/api/reservations/${id}`);
}

export function changeReservation(id, payload) {
  return requestJson(`/api/reservations/${id}`, {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
}