package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public class MyReservationJsonResponse implements MyReservationResponse {

    private final Long id;
    private final String themeName;
    private final LocalDate date;
    private final LocalTime time;
    private final String status;

    private MyReservationJsonResponse(Long id, String themeName, LocalDate date, LocalTime time, String status) {
        this.id = id;
        this.themeName = themeName;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public static MyReservationJsonResponse fromReservationAndStatus(Reservation reservation, String status) {
        return new MyReservationJsonResponse(
            reservation.getId(),
            reservation.getTheme().getName(),
            reservation.getDate(),
            reservation.getTime().getStartAt(),
            status
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
