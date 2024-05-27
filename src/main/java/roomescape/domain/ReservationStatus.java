package roomescape.domain;

import static roomescape.domain.ReservationStatus.Status.RESERVED;
import static roomescape.domain.ReservationStatus.Status.WAITING;

import java.util.function.LongFunction;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.exception.wait.InvalidPriorityException;

@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class ReservationStatus {
    public enum Status {
        RESERVED, WAITING
    }

    
    public static final long RESERVE_NUMBER = 0L;
    private static final LongFunction<Status> STATUS_GENERATOR = insertPriority -> insertPriority > 0 ? WAITING
            : RESERVED;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;
    @Column(name = "priority", nullable = false)
    private long priority;

    public ReservationStatus(long priority) {
        validatePriority(priority);
        this.priority = priority;
        this.status = STATUS_GENERATOR.apply(priority);
    }

    private void validatePriority(long priority) {
        if (priority < RESERVE_NUMBER) {
            throw new InvalidPriorityException();
        }
    }

    public void reserve() {
        status = RESERVED;
        priority = RESERVE_NUMBER;
    }

    public boolean isSameAs(Status other) {
        return status == other;
    }
}
