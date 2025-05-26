package roomescape.waiting.dto.response;

import roomescape.waiting.domain.Waiting;

public record WaitingResponse(
        Long id,
        String name,
        String theme,
        String date,
        String startAt) {
    public static WaitingResponse from(Waiting save) {
        return new WaitingResponse(
                save.getId(),
                save.getMember().getName(),
                save.getTheme().getName(),
                save.getDate().toString(),
                save.getTime().getStartAt().toString()
        );
    }
}
