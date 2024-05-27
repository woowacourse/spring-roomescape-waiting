package roomescape.domain.dto;

import java.time.LocalDate;

public record WaitingRequest(LocalDate date, Long timeId, Long themeId, Long memberId) {
    public WaitingRequest with(Long memberId) {
        return new WaitingRequest(date, timeId, themeId, memberId);
    }
}
