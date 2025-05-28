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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static roomescape.exception.ErrorCode.RESERVATION_NOT_EXIST;

@ToString(exclude = "reservations")
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@Entity
public class ReservationSlot {

    @EmbeddedId
    private final Id id = Id.issue();
    @ManyToOne
    private final ReservationTime time;
    @Embedded
    private final ReservationDate date;
    @ManyToOne
    private final Theme theme;
    @OneToMany(mappedBy = "slot", cascade = CascadeType.REMOVE)
    private final List<Reservation> reservations = new ArrayList<>();

    public ReservationSlot(final ReservationTime time, final LocalDate date, final Theme theme) {
        this.time = time;
        this.date = new ReservationDate(date);
        this.theme = theme;
    }

    public void addReservation(final Reservation reservation) {
        reservations.add(reservation);
    }

    public static Map<Reservation, Integer> toWaitingNumberAndReservation(final List<ReservationSlot> slots, Id userId) {
        return slots.stream()
                .collect(Collectors.toMap(
                        slot -> slot.reservationOf(userId),
                        slot -> slot.waitingNumberOf(userId)
                ));
    }
    
    private int waitingNumberOf(final Id userId) {
        List<Reservation> sortedReservations = new ArrayList<>(reservations).stream()
                .sorted(Comparator.comparing(Reservation::getCreatedAt))
                .toList();

        for (int i = 0; i < sortedReservations.size(); i++) {
            if (sortedReservations.get(i).isSameReserver(userId.value())) {
                return i;
            }
        }
        throw new NotFoundException(RESERVATION_NOT_EXIST);
    }

    private Reservation reservationOf(final Id userId) {
        return reservations.stream()
                .filter(reservation -> reservation.isSameReserver(userId.value()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(RESERVATION_NOT_EXIST));
    }

    public List<Reservation> getWaitingReservations() {
        return new ArrayList<>(reservations).stream()
                .sorted(Comparator.comparing(Reservation::getCreatedAt))
                .skip(1)
                .toList();
    }

    public Reservation getReservedReservation() {
        return new ArrayList<>(reservations).stream()
                .min(Comparator.comparing(Reservation::getCreatedAt))
                .orElseThrow(() -> new NotFoundException(RESERVATION_NOT_EXIST));
    }
}
