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
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;


@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    protected Reservation() {
    }

    private Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme,
                        ReservationStatus status) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    public static Reservation createNew(Member member, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(null, member, date, time, theme, ReservationStatus.RESERVED);
    }

    public static Reservation createWaiting(Member member, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(null, member, date, time, theme, ReservationStatus.WAITING);
    }

    public boolean isPast(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        return reservationDateTime.isBefore(now);
    }

    public long calculateMinutesUntilStart(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());

        return Duration.between(now, reservationDateTime).toMinutes();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reservation other)) {
            return false;
        }
        if (this.id == null || other.id == null) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return System.identityHashCode(this);
        }
        return Objects.hash(id);
    }
}
