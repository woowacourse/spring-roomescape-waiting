package roomescape.dto.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.member.NameResponse;
import roomescape.dto.theme.ThemeResponse;
import roomescape.dto.time.ReservationTimeResponse;

public record ReservationResponse(Long id, @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                  NameResponse member,
                                  ThemeResponse theme,
                                  ReservationTimeResponse time) {

    public static ReservationResponse from(Reservation reservation, ReservationTime reservationTime, Theme theme) {
        NameResponse nameResponse = NameResponse.from(reservation.getName());
        ReservationTimeResponse timeResponse = ReservationTimeResponse.from(reservationTime);
        ThemeResponse themeResponse = ThemeResponse.from(theme);

        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                nameResponse,
                themeResponse,
                timeResponse);
    }
}
