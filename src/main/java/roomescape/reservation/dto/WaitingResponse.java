package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

public record WaitingResponse(
        Long id,
        String name,
        String theme,
        LocalDate date,
        LocalTime startAt

) {

    public static WaitingResponse toResponse(Member member, Theme theme, Reservation reservation,
                                             ReservationTime reservationTime) {
        return new WaitingResponse(reservation.getId(), member.getName(), theme.getName(), reservation.getDate(),
                reservationTime.getStartAt());
    }
}
