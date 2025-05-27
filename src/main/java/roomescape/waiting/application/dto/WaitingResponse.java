package roomescape.waiting.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;
import roomescape.member.application.dto.MemberResponse;
import roomescape.reservationTime.application.dto.TimeResponse;
import roomescape.theme.application.dto.ThemeResponse;
import roomescape.waiting.domain.Waiting;

public record WaitingResponse(
        Long id,
        MemberResponse member,
        ThemeResponse theme,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        TimeResponse time
) {
    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(waiting.getId(), MemberResponse.from(waiting.getMember()),
                ThemeResponse.from(waiting.getTheme()), waiting.getDate(),
                TimeResponse.from(waiting.getTime()));
    }

    public static List<WaitingResponse> from(List<Waiting> waitings) {
        return waitings.stream()
                .map(WaitingResponse::from)
                .toList();
    }
}
