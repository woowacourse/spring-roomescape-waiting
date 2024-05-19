package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class WaitingRank {

    @Column(nullable = false)
    private final Long rank;

    protected WaitingRank() {
        this.rank = null;
    }

    public WaitingRank(final Long rank) {
        this.rank = rank;
    }

    public static WaitingRank createFirst() {
        return new WaitingRank(0L);
    }

    public Long getRank() {
        return rank;
    }

    @Override
    public boolean equals(final Object target) {
        if (this == target) {
            return true;
        }
        if (target == null || getClass() != target.getClass()) {
            return false;
        }
        final WaitingRank that = (WaitingRank) target;
        return Objects.equals(getRank(), that.getRank());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getRank());
    }

    @Override
    public String toString() {
        return "WaitingRank{" +
                "rank=" + rank +
                '}';
    }
}
