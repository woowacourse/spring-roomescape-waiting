package roomescape.service.dto.response.reservation;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.service.dto.response.reservationTime.ReservationTimeResponse;
import roomescape.service.dto.response.theme.ThemeResponse;
import roomescape.service.dto.response.member.MemberIdAndNameResponse;

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
