package roomescape.reservation.repository.entity;

public record ReservationEntity(
        Long id,
        String name,
        String email,
        Long slotId,
        String status
) {
}
