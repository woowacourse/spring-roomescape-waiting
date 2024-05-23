package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.Waiting;

public class MyReservationResponse {

    private final Long id;
    private final String theme;
    private final LocalDate date;
    private final LocalTime time;
    private final String status;

    public MyReservationResponse(final Long id, final LocalDate date, final LocalTime time, final String status,
            final String theme) {
        this.id = id;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public MyReservationResponse(final Reservation reservation, ReservationStatus status) {
        this(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                status.getStatus(),
                reservation.getTheme().getName()
        );
    }

    public MyReservationResponse(final Waiting waiting, ReservationStatus status) {
        this(
                waiting.getId(),
                waiting.getDate(),
                waiting.getReservationTime().getStartAt(),
                status.getStatus(),
                waiting.getTheme().getName()
        );
    }

    public Long getId() {
        return id;
    }

    public String getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }
}
