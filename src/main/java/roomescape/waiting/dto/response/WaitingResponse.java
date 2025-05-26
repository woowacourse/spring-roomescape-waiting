package roomescape.waiting.dto.response;

import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingStatus;

public record WaitingResponse(
        Long id,
        String name,
        String theme,
        String date,
        String startAt,
        WaitingStatus status) {
    public static WaitingResponse from(Waiting save) {
        return new WaitingResponse(
                save.getId(),
                save.getMember().getName(),
                save.getTheme().getName(),
                save.getDate().toString(),
                save.getTime().getStartAt().toString(),
                save.getStatus()
        );
    }
}
