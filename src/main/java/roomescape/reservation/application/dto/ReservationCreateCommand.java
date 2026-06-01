package roomescape.reservation.application.dto;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import roomescape.reservation.domain.ActiveReservation;
import roomescape.reservation.domain.PendingReservation;
import roomescape.reservation.domain.TimeSlot;

@Builder
public record ReservationCreateCommand(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {
    public ActiveReservation toActiveEntity(final TimeSlot slot, final Clock clock) {
        return ActiveReservation.builder()
                .name(name)
                .slot(slot)
                .createdAt(LocalDateTime.now(clock))
                .build();
    }

    public PendingReservation toPendingEntity(final TimeSlot slot, final Clock clock) {
        return PendingReservation.builder()
                .name(name)
                .slot(slot)
                .createdAt(LocalDateTime.now(clock))
                .build();
    }
}
