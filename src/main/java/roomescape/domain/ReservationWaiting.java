package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

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

    public int getRank(List<ReservationWaiting> waitings) {
        Objects.requireNonNull(waitings, "waitings must not be null");
        List<Long> waitingIds = waitings.stream()
                .map(ReservationWaiting::getId)
                .sorted()
                .toList();
        if (!waitingIds.contains(getId())) {
            throw new IllegalArgumentException("waitings does not contain this reservation");
        }
        return waitingIds.indexOf(getId()) + 1;
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

    public String getMemberName() {
        return member.getName();
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
