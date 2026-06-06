package roomescape.controller.dto.response;

import java.util.List;
import roomescape.service.dto.WaitInfo;

public record WaitListResponse(
        List<WaitResponse> items
) {
    public static WaitListResponse from(List<WaitInfo> waitInfos) {
        return new WaitListResponse(waitInfos.stream()
                .map(WaitResponse::from)
                .toList()
        );
    }
}
