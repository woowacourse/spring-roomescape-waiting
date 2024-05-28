package roomescape.service.dto.response.reservation;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.service.dto.response.member.MemberIdAndNameResponse;
import roomescape.service.dto.response.reservationTime.ReservationTimeResponse;
import roomescape.service.dto.response.theme.ThemeResponse;

public record ReservationResponse(Long id,
                                  MemberIdAndNameResponse member,
                                  LocalDate date,
                                  ReservationTimeResponse time,
                                  ThemeResponse theme) {

    public ReservationResponse(Reservation reservation) {
        this(reservation.getId(),
                new MemberIdAndNameResponse(reservation.getMember().getId(), reservation.getMember().getName().getValue()),
                reservation.getDate(),
                new ReservationTimeResponse(reservation.getReservationTime()),
                new ThemeResponse(reservation.getTheme()));

    }
}
