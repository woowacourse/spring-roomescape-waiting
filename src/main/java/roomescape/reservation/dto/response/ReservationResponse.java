package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import roomescape.auth.dto.response.LoginMemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.theme.dto.response.ThemeResponse;

import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        LocalDate date,
        @JsonProperty("time") ReservationTimeResponse reservationTimeResponse,
        @JsonProperty("theme") ThemeResponse themeResponse,
        @JsonProperty("member") LoginMemberResponse loginMemberResponse
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(reservation.getId(),
                reservation.getSchedule().getDate(),
                ReservationTimeResponse.from(reservation.getSchedule().getTime()),
                ThemeResponse.from(reservation.getSchedule().getTheme()),
                LoginMemberResponse.from(reservation.getMember())
        );
    }
}
