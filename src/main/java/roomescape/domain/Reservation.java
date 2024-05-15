package roomescape.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    @ManyToOne
    private Member member;
    @ManyToOne
    private ReservationTime time;
    @ManyToOne
    private Theme theme;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    public Reservation() {
    }

    public Reservation(Long id, LocalDate date, Member member, ReservationTime time, Theme theme, ReservationStatus status) {
        this.id = id;
        this.date = date;
        this.member = member;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    public Reservation(LocalDate date, Member member, ReservationTime time, Theme theme, ReservationStatus status) {
        this(null, date, member, time, theme, status);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Member getMember() {
        return member;
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
