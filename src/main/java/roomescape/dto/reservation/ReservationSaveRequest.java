package roomescape.dto.reservation;

public record ReservationSaveRequest(
        String date,
        Long timeId,
        Long themeId
) {
}
