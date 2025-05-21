package roomescape.domain.entity;

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
import roomescape.domain.ReservationStatus;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    private Reservation(
            Long id,
            Member member,
            Theme theme,
            LocalDate date,
            ReservationTime time,
            ReservationStatus status
    ) {
        this.id = id;
        this.member = member;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
        this.time.addReservation(this);
    }

    protected Reservation() {
    }

    public static Reservation of(
            Long id,
            Member member,
            Theme theme,
            LocalDate date,
            ReservationTime time,
            ReservationStatus status
    ) {
        return new Reservation(id, member, theme, date, time, status);
    }

    public static Reservation withoutId(
            Member member,
            Theme theme,
            LocalDate reservationDate,
            ReservationTime reservationTime,
            ReservationStatus status
    ) {
        return new Reservation(null, member, theme, reservationDate, reservationTime, status);
    }

    public boolean isPast() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        return reservationDateTime.isBefore(now);
    }

    public boolean isDuplicated(Reservation other) {
        return this.date.equals(other.date)
               && this.time.equals(other.time)
               && this.theme.equals(other.theme);
    }

    public boolean isAlreadyBookedTime(LocalDate date, Long themeId, Long timeId) {
        return this.date.equals(date)
               && this.theme.getId().equals(themeId)
               && this.time.getId().equals(timeId);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Theme getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Reservation that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
