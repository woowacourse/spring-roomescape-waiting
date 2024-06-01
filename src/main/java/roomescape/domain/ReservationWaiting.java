package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ReservationWaiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Reservation reservation;

    protected ReservationWaiting() {
    }

    public ReservationWaiting(Member member, Reservation reservation) {
        this.member = member;
        this.reservation = reservation;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Theme getTheme() {
        return reservation.getTheme();
    }

    public LocalDate getDate() {
        return reservation.getDate();
    }

    public LocalTime getStartAt() {
        return reservation.getStartAt();
    }

    public String getThemeName() {
        return reservation.getThemeName();
    }
}
