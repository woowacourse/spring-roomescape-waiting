package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.member.entity.Member;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.theme.entity.Theme;

public record ReservationReadResponse(
        Long id,
        LocalDate date,
        ReservationTime time,
        Member member,
        Theme theme
) {
    public static ReservationReadResponse from(Reservation reservation, Member member, Theme theme) {
        return new ReservationReadResponse(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime(),
                member,
                theme
        );
    }
}
