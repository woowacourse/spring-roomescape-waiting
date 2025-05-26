package roomescape.waiting.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.member.domain.Member;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    private LocalDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    private LocalDateTime createdAt;

    protected Waiting() {
    }

    public Waiting(Member member, LocalDate date, ReservationTime time, Theme theme, LocalDateTime createdAt) {
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.createdAt = createdAt;
    }

    public static Waiting createWithoutId(Member member, LocalDate date, ReservationTime time, Theme theme, LocalDateTime createdAt) {
        return new Waiting(member, date, time, theme, createdAt);
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
