package roomescape.reservation.domain;

import jakarta.persistence.*;
import roomescape.member.domain.Member;

import java.time.LocalDate;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Theme theme;

    @Column(nullable = false, length = 7)
    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;

    protected Reservation() {
    }

    public Reservation(Member member, LocalDate date, ReservationTime time, Theme theme, ReservationStatus status) {
        this(null, member, date, time, theme, status);
    }

    public Reservation(Long id, Reservation reservation) {
        this(id, reservation.member, reservation.date, reservation.time, reservation.theme, reservation.status);
    }

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme, ReservationStatus status) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    public boolean isBeforeOrOnToday(LocalDate today) {
        return date.isBefore(today) || date.equals(today);
    }

    public boolean isBooked() {
        return status.isBooking();
    }

    public String getMemberName() {
        return member.getName();
    }

    public Long getTimeId() {
        return time.getId();
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
