package roomescape.waiting.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.member.controller.response.MemberResponse;
import roomescape.theme.controller.response.ThemeResponse;
import roomescape.time.controller.response.ReservationTimeResponse;
import roomescape.waiting.domain.Waiting;

public record WaitingResponse(
        Long id,
        MemberResponse member,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                MemberResponse.from(waiting.getWaiter()),
                waiting.getReservationDatetime().reservationDate().date(),
                ReservationTimeResponse.from(waiting.getReservationDatetime().reservationTime()),
                ThemeResponse.from(waiting.getTheme())
        );
    }
}
