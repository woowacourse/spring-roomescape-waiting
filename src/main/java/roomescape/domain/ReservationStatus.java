package roomescape.domain;

import lombok.Getter;

@Getter
public enum ReservationStatus {

    RESERVED("예약"),
    WAITING("대기"),
    CANCELED("취소"),
    COMPLETED("완료");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }
}
