package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import roomescape.member.domain.Member;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Waiting() {}

    public Waiting(
            final Long id,
            final LocalDate date,
            final Member member,
            final ReservationTime time,
            final Theme theme
    ) {
        this.id = id;
        this.date = date;
        this.member = member;
        this.time = time;
        this.theme = theme;
    }

    public Waiting(
            final LocalDate date,
            final Member member,
            final ReservationTime time,
            final Theme theme
    ) {
        this(null, date, member, time, theme);
    }

    @PrePersist
    protected void createdAt() {
        this.createdAt = LocalDateTime.now();
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

    public LocalTime startTime() {
        return time.getStartAt();
    }

    public Theme getTheme() {
        return theme;
    }

    public String themeName() {
        return theme.getName();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Waiting waiting)) {
            return false;
        }
        return Objects.equals(getId(), waiting.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
