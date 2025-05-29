package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.reservation.domain.Waiting;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeResponse;

public record WaitingResponse(
        Long id, @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        NameResponse member,
        ThemeResponse theme,
        ReservationTimeResponse time
) {

    public static WaitingResponse from(Waiting waiting, ReservationTime reservationTime, Theme theme) {
        NameResponse nameResponse = NameResponse.from(waiting.getMember());
        ReservationTimeResponse timeResponse = ReservationTimeResponse.from(reservationTime);
        ThemeResponse themeResponse = ThemeResponse.from(theme);

        return new WaitingResponse(
                waiting.getId(),
                waiting.getDate(),
                nameResponse,
                themeResponse,
                timeResponse);
    }
}
