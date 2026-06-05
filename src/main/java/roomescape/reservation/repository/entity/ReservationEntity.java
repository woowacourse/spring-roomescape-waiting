package roomescape.reservation.repository.entity;

public record ReservationEntity(
        Long id,
        String name,
        Long slotId
) {
}
