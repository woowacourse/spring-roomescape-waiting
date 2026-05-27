package roomescape.domain.waiting.dto;

import java.util.List;

public record MyWaitingsResponse(
        List<MyWaitingResult> waitings
) {

    public static MyWaitingsResponse from(List<MyWaitingResult> responses) {
        return new MyWaitingsResponse(responses);
    }
}
