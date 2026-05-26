package roomescape.service.dto;

import java.time.LocalDate;

public record WaitingCommand(String name, LocalDate date, Long timeId, Long themeId) {

    public static WaitingCommand withoutName(LocalDate date, Long timeId, Long themeId) {
        return new WaitingCommand(null, date, timeId, themeId);
    }

}
