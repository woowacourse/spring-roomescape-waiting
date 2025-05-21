package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDate date;
    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ReservationTime reservationTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Theme theme;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    protected Reservation() {
    }

    public Reservation(
            Long id, LocalDate date, ReservationStatus status,
            ReservationTime time, Theme theme, Member member
    ) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.reservationTime = time;
        this.theme = theme;
        this.member = member;
    }

    public static Reservation createWithoutId(
            LocalDate date, ReservationStatus status, ReservationTime time,
            Theme theme, Member member
    ) {
        return new Reservation(null, date, status, time, theme, member);
    }

    public boolean isPastDateTime() {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, reservationTime.getStartAt());
        return reservationDateTime.isBefore(LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation reservation = (Reservation) o;
        if (this.id == null || reservation.id == null) {
            return false;
        }
        return Objects.equals(id, reservation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
