package roomescape.reservation.dto.request;

public record ReservationRequest(
        String name,
        String date,
        Long timeId,
        Long themeId
) {
}

