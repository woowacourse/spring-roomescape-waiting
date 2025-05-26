package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.exception.BadRequestException;

@Embeddable
public class Priority implements Comparable<Priority> {

    private static final Priority HIGHEST = new Priority(0);

    @Column(name = "priority", nullable = false)
    private Integer value;

    protected Priority() {
    }

    Priority(Integer value) {
        validate(value);
        this.value = value;
    }

    static Priority first() {
        return new Priority(1);
    }

    private void validate(Integer value) {
        if (value == null || value < 0) {
            throw new BadRequestException("예약 우선순위는 0 이상이어야 합니다.");
        }
    }

    public Priority approve() {
        return HIGHEST;
    }

    public boolean isHighest() {
        return this.equals(HIGHEST);
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Priority priority)) {
            return false;
        }
        return Objects.equals(value, priority.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "Priority{" +
            "value=" + value +
            '}';
    }

    @Override
    public int compareTo(Priority o) {
        return value.compareTo(o.value);
    }
}
