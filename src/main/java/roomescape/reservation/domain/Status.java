package roomescape.reservation.domain;

import io.micrometer.common.util.StringUtils;
import java.util.Arrays;
import java.util.Optional;

public enum Status {

    RESERVATION,
    WAITING,
    ;

    Status() {

    }

    public static Optional<Status> from(final String name) {
        if (StringUtils.isBlank(name)) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(status -> status.name().equals(name.trim()))
                .findAny()
                .map(Optional::of)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 잘못된 예약 상태를 입력하였습니다."));
    }

    public boolean isReservation() {
        return this == RESERVATION;
    }
}
