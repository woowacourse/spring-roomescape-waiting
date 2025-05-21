package roomescape.admin.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationWithRank;

public record AdminWaitingReservationResponse(
        Long id,
        Long rank,
        String name,
        String theme,
        LocalDate date,
        LocalTime startAt
) {

    public AdminWaitingReservationResponse(ReservationWithRank reservationWithRank) {
        this(
                reservationWithRank.reservation().getId(),
                reservationWithRank.rank(),
                reservationWithRank.reservation().getMember().getName(),
                reservationWithRank.reservation().getTheme().getName(),
                reservationWithRank.reservation().getDate(),
                reservationWithRank.reservation().getReservationTime().getStartAt()
        );
    }
}
