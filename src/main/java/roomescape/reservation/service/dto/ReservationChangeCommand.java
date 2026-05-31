package roomescape.reservation.service.dto;

public record ReservationChangeCommand(
        Long id,
        String requesterName,
        Long dateId,
        Long timeId
) {
}
