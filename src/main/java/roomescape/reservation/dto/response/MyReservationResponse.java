package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public interface MyReservationResponse {

    Long getId();

    String getThemeName();

    LocalDate getDate();

    LocalTime getTime();

    String getStatus();
}
