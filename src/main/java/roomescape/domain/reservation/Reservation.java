package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.reservation.InvalidReservationException;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 10)
    private String name;
    private LocalDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private ReservationStatus reservationStatus;

    protected Reservation() {

    }

    public Reservation(Long id, String name, LocalDate date, ReservationTime time, Theme theme) {
        validate(name, date, time);
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.reservationStatus = ReservationStatus.RESERVATION;
    }

    private void validate(String name, LocalDate date, ReservationTime time) {
        if (name == null || name.isBlank()) {
            throw new InvalidReservationException("이름은 공백일 수 없습니다");
        }
        if (date == null || time == null) {
            throw new InvalidReservationException("시간은 공백일 수 없습니다.");
        }
    }

    public boolean isBeforeDateTime(LocalDateTime compareDateTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, getStartAt());
        return reservationDateTime.isBefore(compareDateTime);
    }

    public boolean isSameTheme(long themeId) {
        return getTheme().isSameTheme(themeId);
    }

    public boolean isBetweenDate(LocalDate dateFrom, LocalDate dateTo) {
        return isSameOrBeforeDate(dateFrom) && isSameOrAfterDate(dateTo);
    }

    private boolean isSameOrAfterDate(LocalDate dateTo) {
        return dateTo.isAfter(date) || dateTo.isEqual(date);
    }

    private boolean isSameOrBeforeDate(LocalDate dateFrom) {
        return dateFrom.isEqual(date) || dateFrom.isBefore(date);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getReservationTime() {
        return time;
    }

    public LocalTime getStartAt() {
        return time.getTime();
    }

    public Theme getTheme() {
        return theme;
    }

    public String getThemeName() {
        return theme.getName();
    }

    public String getReservationStatus() {
        return reservationStatus.getStatus();
    }
}
