package roomescape.business.model.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@Getter
@Entity
public class ReservationSlot {

    @EmbeddedId
    private final Id id;
    @ManyToOne
    private Theme theme;
    @ManyToOne
    private ReservationTime reservationTime;
    @Embedded
    private ReservationDate reservationDate;

    protected ReservationSlot() {
        this.id = Id.issue();
    }

    public static ReservationSlot create(final Theme theme, final ReservationTime time, final ReservationDate date) {
        return new ReservationSlot(Id.issue(), theme, time, date);
    }

    public static ReservationSlot restore(final String id, final Theme theme, final ReservationTime time, final ReservationDate date) {
        return new ReservationSlot(Id.create(id), theme, time, date);
    }
}
