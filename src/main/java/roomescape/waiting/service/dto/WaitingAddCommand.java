package roomescape.waiting.service.dto;

import java.time.LocalDate;

public record WaitingAddCommand(LocalDate date, long timeId, long themeId, long memberId) {
}
