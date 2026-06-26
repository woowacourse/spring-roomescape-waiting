package roomescape.domain;

import java.util.Objects;
import roomescape.domain.exception.DomainValidationException;

public class WaitingOrder {

    private static final long MIN_ORDER = 0L;

    private final long order;

    public WaitingOrder(long order) {
        if (order < MIN_ORDER) {
            throw new DomainValidationException("대기 순번은 0 이상이어야 합니다.");
        }
        this.order = order;
    }

    public long value() {
        return order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WaitingOrder that)) {
            return false;
        }
        return order == that.order;
    }

    @Override
    public int hashCode() {
        return Objects.hash(order);
    }
}
