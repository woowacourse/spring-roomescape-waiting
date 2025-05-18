package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @ManyToOne
    private ReservationTime time;
    @ManyToOne
    private Member member;
    @ManyToOne
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
    }

    public Reservation() {
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
}
