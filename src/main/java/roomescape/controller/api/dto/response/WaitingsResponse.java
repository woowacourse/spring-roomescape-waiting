package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.WaitingOutput;

import java.util.List;

public record WaitingsResponse(List<WaitingResponse> data) {

    public static WaitingsResponse from(final List<WaitingOutput> outputs) {
        return new WaitingsResponse(
                outputs.stream()
                        .map(WaitingResponse::from)
                        .toList()
        );
    }
}
