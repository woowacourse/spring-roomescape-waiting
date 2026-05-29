package roomescape.domain.reservatinWaiting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ExpiredDateTimeException;

public class ReservationWaiting {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final Long sequence;
    private final LocalDateTime createdAt;

    private ReservationWaiting(Long id, String name, LocalDate date, ReservationTime time, Theme theme, Long sequence, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.sequence = sequence;
        this.createdAt = createdAt;
    }

    public static ReservationWaiting create(String name, LocalDate date, ReservationTime time, Theme theme) {
        validatePastDateTime(date, time.getStartAt());
        return new ReservationWaiting(null, name, date, time, theme, null, LocalDateTime.now());
    }

    public static ReservationWaiting restore(Long id, String name, LocalDate date, ReservationTime time, Theme theme, Long sequence, LocalDateTime createdAt) {
        return new ReservationWaiting(id, name, date, time, theme, sequence, createdAt);
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

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Long getSequence() {
        return sequence;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void validateDeletable() {
        validatePastDateTime(date, time.getStartAt());
    }

    private static void validatePastDateTime(LocalDate date, LocalTime time) {
        if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now())) {
            throw new ExpiredDateTimeException();
        }
    }
}
