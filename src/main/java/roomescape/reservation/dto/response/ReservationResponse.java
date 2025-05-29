package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import roomescape.auth.dto.response.LoginMemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.theme.dto.response.ThemeResponse;

public record ReservationResponse(
    Long id,
    LocalDate date,
    @JsonProperty("time") ReservationTimeResponse reservationTimeResponse,
    @JsonProperty("theme") ThemeResponse themeResponse,
    @JsonProperty("member") LoginMemberResponse loginMemberResponse
) {

    public static ReservationResponse fromReservation(Reservation reservation) {
        return new ReservationResponse(reservation.getId(),
            reservation.getDate(),
            ReservationTimeResponse.fromReservationTime(reservation.getTime()),
            ThemeResponse.fromTheme(reservation.getTheme()),
            LoginMemberResponse.from(reservation.getMember())
        );
    }
}
