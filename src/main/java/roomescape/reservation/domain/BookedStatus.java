package roomescape.reservation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BookedStatus {

    WAITING("예약 대기"),
    APPROVED("예약 승인"),
    REJECTED("예약 거절"),
    ;

    private final String description;
}
