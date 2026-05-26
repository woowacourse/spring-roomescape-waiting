package roomescape.service.dto.command;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.dto.request.WaitingRequest;

public record WaitingCommand(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId,
        LocalDateTime createAt
) {
    public static WaitingCommand from(WaitingRequest request) {
        return new WaitingCommand(request.name(), request.date(), request.timeId(), request.themeId(), request.createdAt());
    }
}
