package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.*;
import roomescape.domain.vo.ReservationDeletion;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

@Entity
@Table(
        name = "reservation_slot",
        uniqueConstraints = @UniqueConstraint(columnNames = {"date", "time_id", "theme_id"})
)
public class ReservationSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id", nullable = false)
    private Time time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @OneToMany(mappedBy = "reservationSlot", cascade = CascadeType.PERSIST)
    @OrderBy("updateAt ASC, id ASC")
    private List<Reservation> reservations = new ArrayList<>();

    protected ReservationSlot() {
    }

    public ReservationSlot(
            LocalDate date,
            Time time,
            Theme theme,
            List<Reservation> reservations
    ) {
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.reservations = new ArrayList<>(reservations);
    }

    public Reservation reserve(String name, LocalDateTime now) {
        validateUniqueReservation(name);
        validateNotPastReservation(now, ErrorCode.PAST_DATE_RESERVATION);

        Reservation reservation = new Reservation(this, name, calculateStatus(), now);
        reservations.add(reservation);
        return reservation;
    }

    public Reservation moveIn(Reservation reservation, String name, LocalDateTime now) {
        validateReservationOwner(reservation, name);
        validateUniqueReservation(name);
        validateNotPastReservation(now, ErrorCode.UNALLOWED_UPDATE_PAST_RESERVATION);

        reservation.moveTo(this, now, calculateStatus());
        reservations.add(reservation);
        return reservation;
    }

    public Reservation moveOut(long reservationId, String name, LocalDateTime now) {
        Reservation reservation = findReservation(reservationId);
        validateReservationOwner(reservation, name);
        validateNotPastReservation(now, ErrorCode.UNALLOWED_UPDATE_PAST_RESERVATION);

        promoteReservation(reservation);
        reservations.remove(reservation);
        return reservation;
    }

    private Status calculateStatus() {
        if (reservations.isEmpty()) {
            return Status.RESERVED;
        }
        return Status.WAITING;
    }

    private Optional<Reservation> promoteReservation(Reservation reservation) {
        if (!reservation.isReserved()) {
            return Optional.empty();
        }

        if (reservations.size() < 2) {
            return Optional.empty();
        }
        // ReservationSlot의 reservations는 update_at을 기준으로 정렬되어있다.
        Reservation nextReservation = reservations.get(1);
        nextReservation.promote();
        return Optional.of(nextReservation);
    }

    public ReservationDeletion deleteReservation(long reservationId, String name, LocalDateTime now) {
        Reservation reservation = findReservation(reservationId);
        validateReservationOwner(reservation, name);
        validateNotPastReservation(now, ErrorCode.UNALLOWED_DELETE_PAST_RESERVATION);

        Optional<Reservation> promotedReservation = promoteReservation(reservation);
        reservation.cancel(now);
        reservations.remove(reservation);
        return new ReservationDeletion(reservation, promotedReservation);
    }

    public int calculateOrder(Reservation reservation) {
        if (reservation.isReserved()) {
            return 0;
        }
        return (int) reservations.stream()
                .filter(Reservation::isWaiting)
                .filter(it -> it.isUpdatedAtBefore(reservation))
                .count() + 1;
    }

    private Reservation findReservation(long reservationId) {
        return reservations.stream()
                .filter(r -> r.getId().equals(reservationId))
                .findFirst()
                .orElseThrow(() ->
                        new CustomException(ErrorCode.NOT_EXIST_RESERVATION));
    }

    private void validateNotPastReservation(LocalDateTime now, ErrorCode code) {
        LocalDateTime reservationTime = LocalDateTime.of(date, time.getStartAt());
        if (reservationTime.isBefore(now)) {
            throw new CustomException(code);
        }
    }

    private void validateReservationOwner(Reservation reservation, String name) {
        if (!reservation.getName().equals(name)) {
            throw new CustomException(ErrorCode.COMMON_UNAUTHORIZED);
        }
    }

    private void validateUniqueReservation(String name) {
        boolean duplicated = reservations.stream()
                .anyMatch(reservation -> reservation.getName().equals(name));

        if (duplicated) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS_RESERVATION);
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Time getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public List<Reservation> getReservations() {
        return Collections.unmodifiableList(reservations);
    }
}
