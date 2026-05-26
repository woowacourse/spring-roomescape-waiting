package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Waitlist {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final LocalDateTime createdAt;
    private final ReservationTime time;
    private final Theme theme;

    public Waitlist(
            Long id,
            String name,
            LocalDate date,
            LocalDateTime createdAt,
            ReservationTime time,
            Theme theme
    ) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.createdAt = createdAt;
        this.time = time;
        this.theme = theme;
    }

    public Waitlist(
            String name,
            LocalDate date,
            LocalDateTime createdAt,
            ReservationTime time,
            Theme theme
    ) {
        this(null, name, date, createdAt, time, theme);
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}
