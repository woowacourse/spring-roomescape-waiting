package roomescape.domain.reservation;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;

public class Rank {
    private final int value;

    public Rank(int value) {
        this.value = value;
    }

    public boolean isFirst(){
        return value == 1;
    }

    public Status decideStatus() {
        if(isFirst()) {
            return Status.APPROVED;
        }

        return Status.WAITING;
    }

    public int getValue() {
        return value;
    }
}
