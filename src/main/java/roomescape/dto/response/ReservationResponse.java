package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

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
