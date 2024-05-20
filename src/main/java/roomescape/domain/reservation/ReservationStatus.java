package roomescape.domain.reservation;

import java.util.Arrays;
import roomescape.domain.role.Role;

public enum ReservationStatus {
    FINISH("진행 완료"),
    COMPLETE("예약 완료"),
    CANCEL("예약 취소");

    private final String status;

    ReservationStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
