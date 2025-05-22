package roomescape.business.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class ReservationSlot {

    @EmbeddedId
    private final Id id = Id.issue();
    @ManyToOne
    private ReservationTime time;
    @Embedded
    private ReservationDate date;
    @ManyToOne
    private Theme theme;
    @OneToMany(mappedBy = "slot", cascade = {CascadeType.REMOVE}, orphanRemoval = true)
    private final List<Reservation> reservations = new ArrayList<>();

    public ReservationSlot(final ReservationTime time, final LocalDate date, final Theme theme) {
        this.time = time;
        this.date = new ReservationDate(date);
        this.theme = theme;
    }

    public void addReservation(final Reservation reservation) {
        reservations.add(reservation);
    }

    public void removeReservation(final Reservation reservation) {
        reservations.remove(reservation);
    }

    public int waitingNumberOf(final Id userId) {
        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).isSameReserver(userId.value())) {
                return i;
            }
        }
        throw new NotFoundException(RESERVATION_NOT_EXIST);
    }

    public Reservation reservationOf(final Id userId) {
        return reservations.stream()
                .filter(reservation -> reservation.isSameReserver(userId.value()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(RESERVATION_NOT_EXIST));
    }
}
