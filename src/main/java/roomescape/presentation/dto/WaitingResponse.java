package roomescape.presentation.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.business.domain.Member;
import roomescape.business.domain.ReservationTime;
import roomescape.business.domain.Theme;
import roomescape.business.domain.Waiting;

public record WaitingResponse(Long id, ReservationTime time, Theme theme, Member member, LocalDate date,
                              LocalDateTime createdAt) {
    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(waiting.getId(), waiting.getTime(), waiting.getTheme(), waiting.getMember(),
                waiting.getDate(), waiting.getCreatedAt());
    }
}
