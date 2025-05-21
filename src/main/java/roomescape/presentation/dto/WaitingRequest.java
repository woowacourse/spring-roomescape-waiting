package roomescape.presentation.dto;

import java.time.LocalDate;

public record WaitingRequest(LocalDate date,  Long timeId, Long themeId) {
}
