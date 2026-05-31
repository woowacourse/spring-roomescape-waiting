package roomescape.reservation.service.dto;

public record ReservationSaveCommand(
        Long dateId,
        Long timeId,
        Long themeId
) {
}
