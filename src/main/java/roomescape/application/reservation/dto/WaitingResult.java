package roomescape.application.reservation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.Waiting;

public record WaitingResult(
        long waitingId,
        String memberName,
        String themeName,
        LocalDate date,
        LocalDateTime startedAt
) {
    public static WaitingResult from(Waiting waiting) {
        ReservationSlot reservationSlot = waiting.getThemeSchedule();
        return new WaitingResult(
                waiting.getId(),
                waiting.getMember().getName(),
                reservationSlot.theme().getName(),
                reservationSlot.date(),
                waiting.getStartedAt()
        );
    }
}
