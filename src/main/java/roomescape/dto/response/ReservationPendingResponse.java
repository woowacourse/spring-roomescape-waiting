package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRank;

public record ReservationPendingResponse(Long id, MemberResponse member, LocalDate date,
                                         TimeSlotResponse time, ThemeResponse theme) {

    public static ReservationPendingResponse from(ReservationRank reservationRank) {
        Reservation reservation = reservationRank.getReservation();
        return new ReservationPendingResponse(
                reservationRank.getRank(),
                MemberResponse.from(reservation.getMember()),
                reservation.getDate(),
                TimeSlotResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
