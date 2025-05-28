package roomescape.reservation.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;

@Embeddable
public class WaitingId {

    private static final long WAITING_ID_SEQUENCE = 1L;

    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", sequenceName = "WAITING_ID_SEQUENCE", allocationSize = 1)
    private Long value;

    protected WaitingId() {
    }

    public WaitingId(final Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WaitingId waitingId)) {
            return false;
        }
        return Objects.equals(getValue(), waitingId.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getValue());
    }
}
