package roomescape.presentation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.business.domain.Waiting;

public record WaitingResponse(Long id, ReservationTimeResponse time, ThemeResponse theme, MemberResponse member,
                              LocalDate date, LocalDateTime createdAt) {
    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                ReservationTimeResponse.from(waiting.getTime()),
                ThemeResponse.from(waiting.getTheme()),
                MemberResponse.from(waiting.getMember()),
                waiting.getDate(),
                waiting.getCreatedAt());
    }
}
