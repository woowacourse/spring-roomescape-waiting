package roomescape.application.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.WaitingRank;

public record WaitingWitStatusResult(
        Long waitingId,
        String theme,
        LocalDate date,
        LocalTime time,
        long rank
) {
    public static WaitingWitStatusResult from(WaitingRank waitingRank) {
        ReservationSlot reservationSlot = waitingRank.waiting().getThemeSchedule();
        return new WaitingWitStatusResult(
                waitingRank.waiting().getId(),
                reservationSlot.theme().getName(),
                reservationSlot.date(),
                reservationSlot.time().getStartAt(),
                waitingRank.rank()
        );
    }
}
