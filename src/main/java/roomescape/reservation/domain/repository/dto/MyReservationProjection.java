package roomescape.reservation.domain.repository.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public interface MyReservationProjection {
    Long getId();
    String getThemeName();

    LocalDate getDate();

    LocalTime getTime();
    int getWaitingNumber();
}
