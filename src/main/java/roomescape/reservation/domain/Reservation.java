package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.reservation.exception.InvalidReservationTimeException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.user.domain.User;
import roomescape.waiting.domain.Waiting;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;
    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime reservationTime;
    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    protected Reservation() {
    }

    public Reservation(Long id, LocalDate date, ReservationStatus status, ReservationTime reservationTime, Theme theme,
            User user) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.user = user;
    }

    public static Reservation of(LocalDate date, ReservationStatus status, ReservationTime reservationTime, Theme theme,
            User user) {
        LocalDateTime dateTime = LocalDateTime.of(date, reservationTime.getStartAt());
        validateTense(dateTime);
        return new Reservation(null, date, status, reservationTime, theme, user);
    }

    public static Reservation ofWaiting(Waiting waiting) {
        return new Reservation(null, waiting.getDate(), ReservationStatus.BOOKED, waiting.getTime(), waiting.getTheme(), waiting.getMember());
    }

    private static void validateTense(LocalDateTime dateTime) {
        if (isPastTense(dateTime)) {
            throw new InvalidReservationTimeException("과거시점으로 예약을 진행할 수 없습니다.");
        }
    }

    private static boolean isPastTense(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        return dateTime.isBefore(now);
    }

    public boolean isSameDateTime(Reservation compare) {
        return this.getDateTime().isEqual(compare.getDateTime());
    }

    public LocalDateTime getDateTime() {
        return LocalDateTime.of(date, reservationTime.getStartAt());
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }

    public User getUser() {
        return user;
    }
}
