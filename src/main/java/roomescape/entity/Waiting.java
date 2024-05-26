package roomescape.entity;

import jakarta.persistence.Entity;
import roomescape.domain.ReservationBase;
import roomescape.domain.Schedule;

@Entity
public class Waiting extends ReservationBase {

    public Waiting(final Member member, final Schedule schedule) {
        super(member, schedule);
    }

    protected Waiting() {
    }
}
