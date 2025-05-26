package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.exception.BadRequestException;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Priority {

    private static final Priority HIGHEST = new Priority(0);

    @Column(name = "priority", nullable = false)
    private Integer value;

    private Priority(Integer value) {
        validate(value);
        this.value = value;
    }

    static Priority valueOf(Integer value) {
        return new Priority(value);
    }

    static Priority first() {
        return new Priority(1);
    }

    static Priority next(Priority current) {
        return new Priority(current.value + 1);
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
}
