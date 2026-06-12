package roomescape.wating.repository.entity;

public record WaitingEntity(
        Long id,
        String customerName,
        String customerEmail,
        Long slotId
) {
}
