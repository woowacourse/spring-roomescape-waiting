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
    private LocalDate date;
    @ManyToOne
    private ReservationTime time;
    @ManyToOne
    private Member member;
    @ManyToOne
    private Theme theme;

    public Waiting(Long id, LocalDateTime createdAt, LocalDate date, ReservationTime time, Member member, Theme theme) {
        this.id = id;
        this.createdAt = createdAt;
        this.date = date;
        this.time = time;
        this.member = member;
        this.theme = theme;
    }

    public Waiting() {
    }

    public Waiting(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Waiting register(LocalDate date, ReservationTime time, Member member, Theme theme) {
        return new Waiting(null, LocalDateTime.now(), date, time, member, theme);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Member getMember() {
        return member;
    }

    public Theme getTheme() {
        return theme;
    }
}
