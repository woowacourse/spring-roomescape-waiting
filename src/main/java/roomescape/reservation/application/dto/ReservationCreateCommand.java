package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;

public record ReservationCreateCommand(
        String name,
        LocalDate date,
        Long themeId,
        Long timeId
) {
    public Reservation toEntity(Long themeId, Long timeId) {
        return Reservation.builder()
                .name(name)
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .build();
    }
}
