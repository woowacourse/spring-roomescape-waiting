package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeResponse;

public record ReservationResponse(Long id, @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                  NameResponse member,
                                  ThemeResponse theme,
                                  ReservationTimeResponse time) {

    public static ReservationResponse from(Reservation reservation, ReservationTime reservationTime, Theme theme) {
        NameResponse nameResponse = NameResponse.from(reservation.getMember());
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
