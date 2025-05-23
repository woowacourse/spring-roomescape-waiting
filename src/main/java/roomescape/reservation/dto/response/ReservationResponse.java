package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.member.domain.Member;
import roomescape.member.dto.response.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Waiting;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.response.ThemeResponse;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String reservedStatus
) {
    public static ReservationResponse of(Reservation reservation, ReservationTime reservationTime, Theme theme,
                                         Member member) {
        return new ReservationResponse(reservation.getId(), MemberResponse.from(member),
                reservation.getInfo().getDate(),
                ReservationTimeResponse.from(reservationTime), ThemeResponse.from(theme),
                ReservationStatus.RESERVED.getName()
        );
    }

    public static ReservationResponse of(Waiting reservation, ReservationTime reservationTime, Theme theme,
                                         Member member) {
        return new ReservationResponse(reservation.getId(), MemberResponse.from(member),
                reservation.getInfo().getDate(),
                ReservationTimeResponse.from(reservationTime), ThemeResponse.from(theme),
                ReservationStatus.WAITING.getName()
        );
    }
}
