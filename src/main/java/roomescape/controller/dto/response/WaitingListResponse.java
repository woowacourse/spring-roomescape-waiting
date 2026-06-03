package roomescape.controller.dto.response;

import roomescape.service.dto.WaitingResult;

import java.util.List;

public record WaitingListResponse (
        List<WaitingResponse> waitingList
) {

    public static WaitingListResponse from(List<WaitingResult> waitingList) {
        return new WaitingListResponse(waitingList.stream()
                .map(WaitingResponse::from)
                .toList());
    }
}
