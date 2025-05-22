package roomescape.waiting.domain;

import jakarta.persistence.*;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Waiting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(nullable = false)
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Theme theme;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Member member;

    private LocalDateTime createdAt;

    protected Waiting() {
    }

    public Waiting(LocalDate date, ReservationTime time, Theme theme, Member member, LocalDateTime createdAt) {
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
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

    public Member getMember() {
        return member;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
