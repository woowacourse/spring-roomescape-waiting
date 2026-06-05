package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.domain.Slot;

public class MyReservationResult {

    public enum Status {RESERVED, WAITING}

    private final Long id;
    private final LocalDate date;
    private final ReservationTimeResult time;
    private final ThemeResult theme;
    private final Status status;
    private final Integer waitingOrder;

    private MyReservationResult(Long id, LocalDate date,
                                ReservationTimeResult time, ThemeResult theme,
                                Status status, Integer waitingOrder) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.waitingOrder = waitingOrder;
    }

    public static MyReservationResult ofReservation(Long id, Slot slot) {
        return new MyReservationResult(
                id,
                slot.getDate(),
                ReservationTimeResult.from(slot.getTime()),
                ThemeResult.from(slot.getTheme()),
                Status.RESERVED, null
        );
    }

    public static MyReservationResult ofWaiting(Long id, Slot slot, int order) {
        return new MyReservationResult(
                id,
                slot.getDate(),
                ReservationTimeResult.from(slot.getTime()),
                ThemeResult.from(slot.getTheme()),
                Status.WAITING, order
        );
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTimeResult getTime() {
        return time;
    }

    public ThemeResult getTheme() {
        return theme;
    }

    public Status getStatus() {
        return status;
    }

    public Integer getWaitingOrder() {
        return waitingOrder;
    }
}
