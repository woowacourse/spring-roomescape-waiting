package roomescape.reservation.service.dto;

public record ReservationSaveServiceRequest(String name, Long themeId, Long timeId, Long amount) {
}
