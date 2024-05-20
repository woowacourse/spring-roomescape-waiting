package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.domain.policy.ReservationDueTimePolicy;
import roomescape.exception.reservation.DuplicatedReservationException;
import roomescape.exception.reservation.InvalidDateTimeReservationException;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    @ManyToOne
    private ReservationTime time;
    @ManyToOne
    private Theme theme;
    @ManyToOne
    private Member member;

    public Reservation(Long id, LocalDate date, ReservationTime time, Theme theme, Member member) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public Reservation(LocalDate date, ReservationTime time, Theme theme, Member member) {
        this(null, date, time, theme, member);
    }

    public void validateDuplicateDateTime(List<Reservation> foundReservations) {
        if (foundReservations.isEmpty()) {
            return;
        }
        throw new DuplicatedReservationException();
    }

    public void validateDateTimeReservation(ReservationDueTimePolicy timePolicy) {
        LocalDateTime reservationDateTime = date.atTime(time.getStartAt());
        if (reservationDateTime.isBefore(timePolicy.getDueTime())) {
            throw new InvalidDateTimeReservationException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
