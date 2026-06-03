package roomescape.domain;

import static roomescape.domain.exception.DomainErrorCode.UNAUTHORIZED_RESERVATION;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.exception.RoomEscapeException;

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

    public void verifyCancelableBy(String name) {
        verifyReservedBy(name, "본인의 대기 예약만 취소할 수 있습니다.");
    }

    private void verifyReservedBy(String other, String message) {
        if (!this.name.equals(other)) {
            throw new RoomEscapeException(UNAUTHORIZED_RESERVATION, message);
        }
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
