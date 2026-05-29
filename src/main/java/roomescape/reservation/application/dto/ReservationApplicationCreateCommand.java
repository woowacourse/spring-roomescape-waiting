package roomescape.reservation.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.User;
import roomescape.reservation.domain.Waiting;

public record ReservationApplicationCreateCommand(
        String name,
        LocalDate date,
        Long themeId,
        Long timeId,
        LocalDateTime now
) {
    public Reservation toReservation(ReservationSlot slot) {
        return Reservation.builder()
                .user(toUser(name))
                .slot(slot)
                .build();
    }

    public Waiting toWaiting(ReservationSlot slot) {
        return Waiting.builder()
                .user(toUser(name))
                .slot(slot)
                .build();
    }

    public ReservationSlot toSlot(LocalTime startAt) {
        return ReservationSlot.builder()
                .date(date)
                .themeId(themeId)
                .timeId(timeId)
                .startAt(startAt)
                .build();
    }

    private User toUser(String name) {
        return User.builder()
                .name(name)
                .build();
    }
}
