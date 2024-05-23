package roomescape.application.dto.request;

import java.time.LocalDate;

public record WaitingRequest(LocalDate date, Long timeId, Long themeId, Long memberId) {
}
