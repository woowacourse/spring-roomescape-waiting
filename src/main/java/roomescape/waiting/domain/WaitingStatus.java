package roomescape.waiting.domain;

import java.util.Arrays;
import roomescape.exception.BadRequestException;
import roomescape.exception.ExceptionCause;

public enum WaitingStatus {
    PENDING("예약 대기"),
    APPROVED("예약 대기 승인"),
    DENIED("예약 대기 거절");

    private final String label;

    WaitingStatus(String label) {
        this.label = label;
    }

    public static WaitingStatus getStatus(String status) {
        return Arrays.stream(WaitingStatus.values())
                .filter(waitingStatus -> waitingStatus.name().equals(status))
                .findAny()
                .orElseThrow(() -> new BadRequestException(ExceptionCause.WAITING_STATUS_INVALID));
    }

    public String getLabel() {
        return label;
    }
}
