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

import java.time.LocalDate;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@Getter
@Entity
public class ReservationSlot {

    @EmbeddedId
    private final Id id;
    @ManyToOne
    private ReservationTime time;
    @Embedded
    private ReservationDate date;
    @ManyToOne
    private Theme theme;

    protected ReservationSlot() {
        this.id = Id.issue();
    }

    public static ReservationSlot create(final ReservationTime time, final LocalDate date, final Theme theme) {
        return new ReservationSlot(Id.issue(), time, new ReservationDate(date), theme);
    }

    public static ReservationSlot restore(final String id, final ReservationTime time, final LocalDate date, final Theme theme) {
        return new ReservationSlot(Id.create(id), time, new ReservationDate(date), theme);
    }
}
