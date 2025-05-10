package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

import roomescape.member.model.Member;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;

public record ReservationResponse(
    Long id,
    Member member,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
    ReservationTime time,
    Theme theme) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getMember(),
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme());
    }
}
