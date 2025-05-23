package roomescape.presentation.response;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.waiting.WaitingWithRank;

public record UserReservationResponse(
        long id,
        ThemeResponse theme,
        LocalDate date,
        TimeSlotResponse time,
        String status

) {

    public static UserReservationResponse fromReservation(final Reservation reservation) {
        return new UserReservationResponse(
                reservation.id(),
                ThemeResponse.from(reservation.theme()),
                reservation.date(),
                TimeSlotResponse.from(reservation.timeSlot()),
                "예약"
        );
    }

    public static UserReservationResponse fromWaitingWithRank(final WaitingWithRank waitingWithRank) {
        return new UserReservationResponse(
                waitingWithRank.waiting().id(),
                ThemeResponse.from(waitingWithRank.waiting().theme()),
                waitingWithRank.waiting().date(),
                TimeSlotResponse.from(waitingWithRank.waiting().timeSlot()),
                waitingWithRank.rank() + "번째 예약대기"
        );
    }
}
