package roomescape.domain;

import jakarta.persistence.Entity;

@Entity
public class Waiting extends ReservationBase{

    public Waiting(final Member member, final Schedule schedule) {
        super(member, schedule);
    }

    protected Waiting() {
    }
}
