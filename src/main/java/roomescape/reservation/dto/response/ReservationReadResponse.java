package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.member.entity.Member;
import roomescape.reservation.entity.Reservation;
import roomescape.theme.entity.Theme;

public record ReservationReadResponse(
        Long id,
        LocalDate date,
        LocalTime startAt,
        String memberName,
        String themeName
) {
    public static ReservationReadResponse from(Reservation reservation, Member member, Theme theme) {
        return new ReservationReadResponse(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                member.getName(),
                theme.getName()
        );
    }
}
