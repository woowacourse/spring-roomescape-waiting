package roomescape.domain;

import java.util.List;
import java.util.Optional;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomEscapeException;

public class Waits {

    private static final int MAX_WAIT_SIZE = 3;

    private final List<Wait> waits;

    public Waits(List<Wait> waits) {
        this.waits = waits;
    }

    public void validateAddable(Wait newWait) {
        if (waits.stream().anyMatch(w -> w.isWaitedBy(newWait))) {
            throw new RoomEscapeException(DomainErrorCode.DUPLICATED_WAIT);
        }
        if (waits.size() >= MAX_WAIT_SIZE) {
            throw new RoomEscapeException(DomainErrorCode.WAIT_IS_FULL);
        }
    }

    public Optional<Wait> findFirst() {
        return waits.stream().findFirst();
    }
}
