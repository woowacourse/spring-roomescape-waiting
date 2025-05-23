package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class MyReservationJsonResponse implements MyReservationResponse {

    private final Long id;
    private final String themeName;
    private final LocalDate date;
    private final LocalTime time;
    private final String status;

    private MyReservationJsonResponse(Long id, LocalDate date, LocalTime time, String themeName, String status) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.themeName = themeName;
        this.status = status;
    }

    public static MyReservationJsonResponse fromReservationAndStatus(Reservation reservation, ReservationStatus status) {
        return new MyReservationJsonResponse(
                reservation.getId(),
                reservation.getSchedule().getDate(),
                reservation.getSchedule().getTime().getStartAt(),
                reservation.getSchedule().getTheme().getName(),
                status.getDescription()
        );
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    @JsonProperty("theme")
    public String getThemeName() {
        return themeName;
    }

    @Override
    public LocalDate getDate() {
        return date;
    }

    @Override
    public LocalTime getTime() {
        return time;
    }

    @Override
    public String getStatus() {
        return status;
    }
}
