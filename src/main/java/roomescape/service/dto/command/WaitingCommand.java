package roomescape.service.dto.command;

import java.time.LocalDate;
import roomescape.controller.dto.request.WaitingRequest;

public record WaitingCommand(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {
    public static WaitingCommand from(WaitingRequest request) {
        return new WaitingCommand(request.name(), request.date(), request.timeId(), request.themeId());
    }
}