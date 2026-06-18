package roomescape.domain.reservation;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
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
}
