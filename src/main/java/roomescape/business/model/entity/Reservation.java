package roomescape.business.model.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import roomescape.business.model.vo.Id;

@ToString
@EqualsAndHashCode(of = "id")
@Getter
@Entity
public class Reservation {

    @EmbeddedId
    private final Id id;
    @ManyToOne
    private User user;
    @ManyToOne
    private ReservationSlot slot;

    protected Reservation() {
        id = Id.issue();
    }

    private Reservation(final Id id, final User user, final ReservationSlot slot) {
        this.id = id;
        this.user = user;
        this.slot = slot;
        slot.addReservation(this);
    }

    public static Reservation create(final User user, final ReservationSlot reservationSlot) {
        reservationSlot.getDate().validateFresh();
        return new Reservation(Id.issue(), user, reservationSlot);
    }

    public static Reservation restore(final String id, final User user, final ReservationSlot reservationSlot) {
        return new Reservation(Id.create(id), user, reservationSlot);
    }

    public boolean isSameReserver(final String userId) {
        return user.isSameUser(userId);
    }

    public void cancel() {
        slot.removeReservation(this);
    }
}
