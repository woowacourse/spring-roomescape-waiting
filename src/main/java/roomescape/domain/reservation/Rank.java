package roomescape.domain.reservation;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.util.Objects;

public class Rank {
    private static final int MIN_RANK_VALUE = 0;

    private final int value;

    public Rank(int value) {
        validate(value);
        this.value = value;
    }

    private void validate(int value) {
        if (value < MIN_RANK_VALUE) {
            throw new RoomEscapeException(ErrorCode.INVALID_RANK_VALUE);
        }
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Rank rank = (Rank) o;
        return value == rank.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
