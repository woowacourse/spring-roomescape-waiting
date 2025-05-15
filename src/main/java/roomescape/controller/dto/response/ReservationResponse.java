package roomescape.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.entity.Reservation;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;

public record ReservationResponse(
    Long id,
    MemberResponse member,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
    ReservationTime time,
    Theme theme
) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            MemberResponse.from(reservation.getMember()),
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme()
        );
    }
}
