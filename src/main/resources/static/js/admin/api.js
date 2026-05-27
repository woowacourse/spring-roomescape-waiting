import {requestJson} from "../common/http.js";

const ADMIN_HEADERS = {
  "Content-Type": "application/json",
  role: "ADMIN"
};

const ADMIN_ONLY_HEADERS = {
  role: "ADMIN"
};

export function fetchThemes() {
  return requestJson("/api/admin/themes", {
    headers: ADMIN_ONLY_HEADERS
  });
}

export function createTheme(payload) {
  return requestJson("/api/admin/themes", {
    method: "POST",
    headers: ADMIN_HEADERS,
    body: JSON.stringify(payload)
  });
}

export function activateTheme(id) {
  return requestJson(`/api/admin/themes/${id}/activate`, {
    method: "PATCH",
    headers: ADMIN_ONLY_HEADERS
  });
}

export function deactivateTheme(id) {
  return requestJson(`/api/admin/themes/${id}/deactivate`, {
    method: "PATCH",
    headers: ADMIN_ONLY_HEADERS
  });
}

export function fetchReservations() {
  return requestJson("/api/admin/reservations", {
    headers: ADMIN_ONLY_HEADERS
  });
}

export function createReservation(payload) {
  return requestJson("/api/admin/reservations", {
    method: "POST",
    headers: ADMIN_HEADERS,
    body: JSON.stringify(payload)
  });
}

export function deleteReservation(id) {
  return requestJson(`/api/admin/reservations/entries/${id}`, {
    method: "DELETE",
    headers: ADMIN_ONLY_HEADERS
  });
}

export function fetchTimes() {
  return requestJson("/api/admin/times", {
    headers: ADMIN_ONLY_HEADERS
  });
}

export function createTime(payload) {
  return requestJson("/api/admin/times", {
    method: "POST",
    headers: ADMIN_HEADERS,
    body: JSON.stringify(payload)
  });
}

export function activateTime(id) {
  return requestJson(`/api/admin/times/${id}/activate`, {
    method: "PATCH",
    headers: ADMIN_ONLY_HEADERS
  });
}

export function deactivateTime(id) {
  return requestJson(`/api/admin/times/${id}/deactivate`, {
    method: "PATCH",
    headers: ADMIN_ONLY_HEADERS
  });
}
