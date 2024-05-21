package roomescape.domain;

import static roomescape.exception.ExceptionType.WAITING_AT_ALREADY_RESERVATION;
import static roomescape.exception.ExceptionType.WAITING_WITHOUT_MEMBER;
import static roomescape.exception.ExceptionType.WAITING_WITHOUT_RESERVATION;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import roomescape.exception.RoomescapeException;

@Entity
public class ReservationWaiting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Reservation reservation;
    @ManyToOne
    private Member waitingMember;

    public ReservationWaiting(Reservation reservation, Member waitingMember) {
        this(null, reservation, waitingMember);
    }

    public ReservationWaiting(Long id, Reservation reservation, Member waitingMember) {
        validateReservation(reservation);
        validateWaitingMember(waitingMember);
        validateSameMemberWithReservation(reservation, waitingMember);
        this.id = id;
        this.reservation = reservation;
        this.waitingMember = waitingMember;
    }

    private void validateReservation(Reservation reservation) {
        if (reservation == null) {
            throw new RoomescapeException(WAITING_WITHOUT_RESERVATION);
        }
    }

    private void validateWaitingMember(Member waitingMember) {
        if (waitingMember == null) {
            throw new RoomescapeException(WAITING_WITHOUT_MEMBER);
        }
    }

    private void validateSameMemberWithReservation(Reservation reservation, Member waitingMember) {
        Member reservationMember = reservation.getReservationMember();
        if (reservationMember.equals(waitingMember)) {
            throw new RoomescapeException(WAITING_AT_ALREADY_RESERVATION);
        }
    }

    public ReservationWaiting(Long id, ReservationWaiting reservationWaiting) {
        this(id, reservationWaiting.reservation, reservationWaiting.waitingMember);
    }

    protected ReservationWaiting() {
    }

    public boolean hasIdOf(long id) {
        return this.id == id;
    }

    public Long getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Member getWaitingMember() {
        return waitingMember;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ReservationWaiting that = (ReservationWaiting) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public String toString() {
        return "ReservationWaiting{" +
                "id=" + id +
                ", reservation=" + reservation +
                ", waitingMember=" + waitingMember +
                '}';
    }
}
