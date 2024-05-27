package roomescape.application.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;

public record ReservationResponse(long id,
                                  String member,
                                  String theme,
                                  LocalDate date,
                                  LocalTime startAt
) {

    public static ReservationResponse from(Reservation reservation) {
        Member member = reservation.getMember();
        ReservationTime time = reservation.getTime();
        Theme theme = reservation.getTheme();

        return new ReservationResponse(
                reservation.getId(),
                member.getName(),
                theme.getName(), reservation.getDate(),
                time.getStartAt()
        );
    }
}
