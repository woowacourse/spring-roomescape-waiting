package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.domain.enums.Waiting;

@Entity
public class ReservationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private Waiting status;

    private Long priority;

    protected ReservationStatus() {
    }

    public ReservationStatus(Waiting status, Long priority) {
        this.status = status;
        this.priority = priority;
    }

    public Long getId() {
        return id;
    }

    public Waiting getStatus() {
        return status;
    }

    public Long getPriority() {
        return priority;
    }
}
