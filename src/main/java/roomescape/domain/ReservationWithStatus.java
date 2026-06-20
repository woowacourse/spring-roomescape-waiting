package roomescape.domain;

import java.time.LocalDate;

public class ReservationWithStatus {

    private final Long id;
    private final String name;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private final ReservationStatus status;
    private final Integer waitingOrder;

    public ReservationWithStatus(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        ReservationStatus status,
        Integer waitingOrder
    ) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.waitingOrder = waitingOrder;
    }

    public static ReservationWithStatus reserved(Reservation reservation) {
        return new ReservationWithStatus(
            reservation.getId(),
            reservation.getMember().getName(),
            reservation.getDate(),
            reservation.getTime(),
            reservation.getTheme(),
            ReservationStatus.RESERVED,
            null
        );
    }

    public static ReservationWithStatus waiting(Waitlist waitlist, int waitingOrder) {
        return new ReservationWithStatus(
            waitlist.getId(),
            waitlist.getMember().getName(),
            waitlist.getDate(),
            waitlist.getTime(),
            waitlist.getTheme(),
            ReservationStatus.WAITING,
            waitingOrder
        );
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

    public ReservationStatus getStatus() {
        return status;
    }

    public Integer getWaitingOrder() {
        return waitingOrder;
    }
}
