package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationWaiting;
import roomescape.theme.dto.ThemeResponse;
import roomescape.time.dto.ReservationTimeResponse;

public record ReservationResponse(Long id,
                                  MemberResponse member,
                                  LocalDate date,
                                  ReservationTimeResponse time,
                                  ThemeResponse theme) {

    public ReservationResponse(Reservation reservation) {
        this(reservation.getId(), new MemberResponse(reservation.getMember()), reservation.getDate(),
                new ReservationTimeResponse(reservation.getTime()),
                new ThemeResponse(reservation.getTheme()));
    }

    public ReservationResponse(ReservationWaiting reservationWaiting) {
        this(reservationWaiting.getId(),
                new MemberResponse(reservationWaiting.getMember()),
                reservationWaiting.getDate(),
                new ReservationTimeResponse(reservationWaiting.getTime()),
                new ThemeResponse(reservationWaiting.getTheme()));
    }
}
