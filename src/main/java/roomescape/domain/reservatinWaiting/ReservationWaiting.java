package roomescape.domain.reservatinWaiting;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ExpiredDateTimeException;

public class ReservationWaiting {

    private Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private Long sequence;
    private final LocalDateTime createdAt;

    public ReservationWaiting(String name, LocalDate date, ReservationTime time, Theme theme) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.createdAt = LocalDateTime.now();
    }

    public ReservationWaiting(Long id, String name, LocalDate date, ReservationTime time, Theme theme, Long sequence, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.sequence = sequence;
        this.createdAt = createdAt;
    }

    public ReservationWaiting withReservationWaitingId(Long id) {
        return new ReservationWaiting(id, this.name, this.date, this.time, this.theme, this.sequence, this.createdAt);
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

    public void validatePastDateTime() {
        if (LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now())) {
            throw new ExpiredDateTimeException();
        }
    }
}
