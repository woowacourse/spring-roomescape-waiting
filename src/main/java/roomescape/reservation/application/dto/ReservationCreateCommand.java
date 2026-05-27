package roomescape.reservation.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;

public record ReservationCreateCommand(
        String name,
        LocalDate date,
        Long themeId,
        Long timeId,
        LocalDateTime now
) {
    public Reservation toEntity(Long themeId, Long timeId, LocalTime startAt) {
        return Reservation.builder()
                .name(name)
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .startAt(startAt)
                .build();
    }

    public Waiting toWaiting(Long themeId, Long timeId, LocalTime startAt) {
        return Waiting.builder()
                .name(name)
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .startAt(startAt)
                .build();
    }
}
