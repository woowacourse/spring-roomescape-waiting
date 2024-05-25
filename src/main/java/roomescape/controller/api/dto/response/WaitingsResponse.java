package roomescape.controller.api.dto.response;

import java.util.List;
import roomescape.service.dto.output.WaitingOutput;

public record WaitingsResponse(List<WaitingResponse> data) {
    public static WaitingsResponse toResponse(List<WaitingOutput> outputs) {
        return new WaitingsResponse(
                outputs.stream()
                        .map(WaitingResponse::toResponse)
                        .toList()
        );
    }
}
