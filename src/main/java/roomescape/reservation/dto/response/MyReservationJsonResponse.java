package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MyReservationJsonResponse that)) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(themeName, that.themeName)
            && Objects.equals(date, that.date) && Objects.equals(time, that.time)
            && Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, themeName, date, time, status);
    }
}
