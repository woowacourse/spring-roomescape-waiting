package roomescape.presentation.response;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.waiting.Waiting;

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

    public static UserReservationResponse fromWaiting(final Waiting waiting) {
        return new UserReservationResponse(
                waiting.id(),
                ThemeResponse.from(waiting.theme()),
                waiting.date(),
                TimeSlotResponse.from(waiting.timeSlot()),
                "대기"
        );
    }
}
