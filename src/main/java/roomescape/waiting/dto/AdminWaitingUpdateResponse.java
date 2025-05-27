package roomescape.waiting.dto;

import roomescape.waiting.domain.WaitingStatus;

public record AdminWaitingUpdateResponse(
        String message
) {

    public static AdminWaitingUpdateResponse from(WaitingStatus status) {
        if (status == WaitingStatus.APPROVED) {
            return new AdminWaitingUpdateResponse("예약 대기를 승인하였습니다.");
        }
        return new AdminWaitingUpdateResponse("예약 대기를 거절하였습니다.");
    }
}
