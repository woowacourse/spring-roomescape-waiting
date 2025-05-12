package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

import roomescape.member.dto.response.MemberGetResponse;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;

public record ReservationGetResponse(
    Long id,
    MemberGetResponse member,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
    ReservationTime time,
    Theme theme) {

    public static ReservationGetResponse from(Reservation reservation) {
        return new ReservationGetResponse(
            reservation.getId(),
            MemberGetResponse.from(reservation.getMember()),
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme());
    }
}
