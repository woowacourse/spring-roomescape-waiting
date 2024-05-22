package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.ReservationRank;

public record ReservationPendingResponse(Long id, MemberResponse member, LocalDate date,
                                         TimeSlotResponse time, ThemeResponse theme) {

    public static ReservationPendingResponse from(ReservationRank reservationRank) {
        return new ReservationPendingResponse(
                reservationRank.getRank(),
                MemberResponse.from(reservationRank.getReservation().getMember()),
                reservationRank.getReservation().getDate(),
                TimeSlotResponse.from(reservationRank.getReservation().getTime()),
                ThemeResponse.from(reservationRank.getReservation().getTheme())
        );
    }
}
