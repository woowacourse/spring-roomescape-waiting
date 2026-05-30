package roomescape.reservation.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.User;

public record ReservationCreateCommand(
        String name,
        LocalDate date,
        Long themeId,
        Long timeId,
        LocalDateTime now
) {
    public Reservation toReservation(ReservationSlot slot) {
        return Reservation.create(toUser(name), slot, now);
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
