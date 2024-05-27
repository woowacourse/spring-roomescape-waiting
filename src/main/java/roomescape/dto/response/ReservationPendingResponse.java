package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.dto.ReservationRank;

public record ReservationPendingResponse(Long id, MemberResponse member, LocalDate date,
                                         TimeSlotResponse time, ThemeResponse theme) {

    public static ReservationPendingResponse from(ReservationRank reservationRank) {
        Reservation reservation = reservationRank.reservation();
        return new ReservationPendingResponse(
                reservationRank.rank(),
                MemberResponse.from(reservation.getMember()),
                reservation.getDate(),
                TimeSlotResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
