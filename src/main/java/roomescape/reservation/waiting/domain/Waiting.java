package roomescape.reservation.waiting.domain;

import jakarta.persistence.*;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime createdAt;
    private LocalDate reservationDate;
    @ManyToOne
    private ReservationTime reservationTime;
    @ManyToOne
    private Member member;
    @ManyToOne
    private Theme theme;

    public Waiting(Long id, LocalDateTime createdAt, LocalDate reservationDate, ReservationTime reservationTime, Member member, Theme theme) {
        this.id = id;
        this.createdAt = createdAt;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.member = member;
        this.theme = theme;
    }

    public Waiting() {
    }

    public static Waiting register(LocalDate reservationDate, ReservationTime reservationTime, Member member, Theme theme) {
        return new Waiting(null, LocalDateTime.now(), reservationDate, reservationTime, member, theme);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Member getMember() {
        return member;
    }

    public Theme getTheme() {
        return theme;
    }
}
