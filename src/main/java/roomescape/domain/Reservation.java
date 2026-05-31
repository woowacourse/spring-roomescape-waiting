package roomescape.domain;

import static roomescape.domain.exception.DomainErrorCode.PAST_RESERVATION;
import static roomescape.domain.exception.DomainErrorCode.UNAUTHORIZED_RESERVATION;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.exception.RoomEscapeException;

public class Reservation {
    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(
            Long id,
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(
            String name,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        this(null, name, date, time, theme);
    }

    public void verifyReservable(LocalDateTime now) {
        if (isPast(now)) {
            throw new RoomEscapeException(PAST_RESERVATION, "과거 시점에 예약할 수 없습니다.");
        }
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(now);
    }

    public void verifyCancelableBy(String name, LocalDateTime now) {
        verifyReservedBy(name, "본인의 예약만 취소할 수 있습니다.");
        if (isPast(now)) {
            throw new RoomEscapeException(PAST_RESERVATION, "이미 지난 예약은 취소할 수 없습니다.");
        }
    }

    public Reservation changeBy(String name, LocalDateTime now, LocalDate newDate, ReservationTime newTime) {
        verifyReservedBy(name, "본인의 예약만 변경할 수 있습니다.");
        if (isPast(now)) {
            throw new RoomEscapeException(PAST_RESERVATION, "이미 지난 예약은 변경할 수 없습니다.");
        }
        if (LocalDateTime.of(newDate, newTime.getStartAt()).isBefore(now)) {
            throw new RoomEscapeException(PAST_RESERVATION, "과거 시점으로 변경할 수 없습니다.");
        }
        return new Reservation(id, this.name, newDate, newTime, theme);
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

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}
