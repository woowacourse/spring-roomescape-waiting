package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ReservationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long priority;

    protected ReservationStatus() {
    }

    public ReservationStatus(Long priority) {
        this.priority = priority;
    }

    public Long getId() {
        return id;
    }

    public Long getPriority() {
        return priority;
    }

    public boolean isWaiting() {
        return priority > 1;
    }

    public boolean isConfirmed() {
        return priority == 1;
    }
}
