package roomescape.presentation.dto.response;

import roomescape.domain.Waiting;

import java.time.LocalDate;

public record WaitingResponse(Long waitingId, LocalDate date, Long memberId, Long themeId, Long timeId) {

    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(waiting.getId(), waiting.getDate(), waiting.getMemberId(), waiting.getTheme().getId(), waiting.getTime().getId());
    }
}
