package roomescape.controller.dto.response;

import java.util.List;
import java.util.Map;
import roomescape.domain.Wait;

public record WaitListResponse(
        List<WaitResponse> items
) {
    public static WaitListResponse from(Map<Wait, Long> waits) {
        return new WaitListResponse(waits.entrySet().stream()
                .map(entry -> WaitResponse.of(entry.getKey(), entry.getValue()))
                .toList()
        );
    }
}
