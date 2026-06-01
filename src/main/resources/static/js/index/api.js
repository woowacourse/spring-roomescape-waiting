import { requestJson } from "../common/http.js";

export function fetchThemes() {
  return requestJson("/api/themes");
}

export function fetchPopularThemes(startDate, endDate) {
  const searchParams = new URLSearchParams({ startDate, endDate });
  return requestJson(`/api/themes/popular?${searchParams.toString()}`);
}
