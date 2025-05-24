package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import roomescape.exception.BadRequestException;

@Embeddable
public class Priority implements Comparable<Priority> {

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

    static Priority next(Priority priority) {
        return new Priority(priority.value + 1);
    }

    private void validate(Integer value) {
        if (value == null || value < 1) {
            throw new BadRequestException("예약 우선순위는 1 이상이어야 합니다.");
        }
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
