package roomescape.reservation.domain;

import java.util.Arrays;

public enum Status {
    SUCCESS("예약"),
    CANCEL("취소"),
    WAIT("대기"),
    ;

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public static Status from(String inputStatus) {
        return Arrays.stream(values())
                .filter(status -> status.name().equals(inputStatus))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("일치하는 예약 상태가 없습니다."));
    }

    public String getDisplayName() {
        return displayName;
    }
}
