package roomescape.waiting.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import roomescape.auth.dto.response.LoginMemberResponse;
import roomescape.reservationtime.dto.response.ReservationTimeResponse;
import roomescape.theme.dto.response.ThemeResponse;
import roomescape.waiting.domain.Waiting;

import java.time.LocalDate;

public record WaitingCreateResponse(
        Long id,
        LocalDate date,
        @JsonProperty("time") ReservationTimeResponse reservationTimeResponse,
        @JsonProperty("theme") ThemeResponse themeResponse,
        @JsonProperty("member") LoginMemberResponse loginMemberResponse
) {
    public static WaitingCreateResponse from(Waiting waiting) {
        return new WaitingCreateResponse(
                waiting.getId(),
                waiting.getSchedule().getDate(),
                ReservationTimeResponse.from(waiting.getSchedule().getTime()),
                ThemeResponse.from(waiting.getSchedule().getTheme()),
                LoginMemberResponse.from(waiting.getMember())
        );
    }
}
