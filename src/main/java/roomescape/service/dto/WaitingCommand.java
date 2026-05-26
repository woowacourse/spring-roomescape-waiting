package roomescape.service.dto;

import java.time.LocalDate;

public record WaitingCommand(String name, LocalDate date, Long timeId, Long themeId) {
}
