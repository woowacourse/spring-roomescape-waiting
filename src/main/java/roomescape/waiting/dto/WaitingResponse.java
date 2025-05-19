package roomescape.waiting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.member.dto.MemberResponse;
import roomescape.reservationTime.dto.admin.ReservationTimeResponse;
import roomescape.theme.dto.ThemeResponse;
import roomescape.waiting.domain.Waiting;

public record WaitingResponse(
        Long id,
        MemberResponse member,
        ThemeResponse theme,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        ReservationTimeResponse time
) {
    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(waiting.getId(), MemberResponse.from(waiting.getMember()),
                ThemeResponse.from(waiting.getTheme()), waiting.getDate(),
                ReservationTimeResponse.from(waiting.getTime()));
    }
}
