package roomescape.waiting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Entity
@Table(name = "waiting")
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date")
    @NotNull
    private LocalDate date;

    @JoinColumn(name = "member_id")
    @ManyToOne
    @NotNull
    private Member member;

    @JoinColumn(name = "theme_id")
    @ManyToOne
    @NotNull
    private Theme theme;

    @JoinColumn(name = "time_id")
    @ManyToOne
    @NotNull
    private ReservationTime time;

    @Column(name = "created_at")
    @Temporal(value = TemporalType.TIMESTAMP)
    @NotNull
    private LocalDateTime createdAt;

    // protected 로 한 이유는 뭐야?
    protected Waiting() {
    }

    public Waiting(LocalDate date, Member member, Theme theme, ReservationTime time, LocalDateTime createdAt) {
        this.date = date;
        this.member = member;
        this.theme = theme;
        this.time = time;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    // @NotNull 은 왜 사용한거야? 이게 동작해?
    public @NotNull LocalDate getDate() {
        return date;
    }

    public @NotNull Member getMember() {
        return member;
    }

    public @NotNull Theme getTheme() {
        return theme;
    }

    public @NotNull ReservationTime getTime() {
        return time;
    }

    public @NotNull LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
