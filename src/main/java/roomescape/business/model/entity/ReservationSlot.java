package roomescape.business.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;
import roomescape.exception.business.NotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static roomescape.exception.ErrorCode.RESERVATION_NOT_EXIST;

@ToString
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
    @OneToMany(mappedBy = "slot", cascade = {CascadeType.REMOVE}, orphanRemoval = true)
    private final List<Reservation> reservations = new ArrayList<>();

    protected ReservationSlot() {
        this.id = Id.issue();
    }

    private ReservationSlot(final Id id, final ReservationTime time, final ReservationDate date, final Theme theme) {
        this.id = id;
        this.time = time;
        this.date = date;
        this.theme = theme;
    }

    public static ReservationSlot create(final ReservationTime time, final LocalDate date, final Theme theme) {
        return new ReservationSlot(Id.issue(), time, new ReservationDate(date), theme);
    }

    public static ReservationSlot restore(final String id, final ReservationTime time, final LocalDate date, final Theme theme) {
        return new ReservationSlot(Id.create(id), time, new ReservationDate(date), theme);
    }

    public void addReservation(final Reservation reservation) {
        reservations.add(reservation);
    }

    public void removeReservation(final Reservation reservation) {
        reservations.remove(reservation);
    }

    public int calculateTurnOf(final Reservation reservation) {
        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).equals(reservation)) {
                return i + 1;
            }
        }
        throw new NotFoundException(RESERVATION_NOT_EXIST);
    }
}
