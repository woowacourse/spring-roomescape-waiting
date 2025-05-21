package roomescape.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.Waiting;

import java.time.LocalDate;

public record WaitingResponse(
        Long id,

        MemberResponse member,

        ThemeResponse theme,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        ReservationTimeResponse time
) {

    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                MemberResponse.from(waiting.getMember()),
                ThemeResponse.from(waiting.getReservation().getTheme()),
                waiting.getReservation().getDate(),
                ReservationTimeResponse.from(waiting.getReservation().getTime())
        );
    }
}
