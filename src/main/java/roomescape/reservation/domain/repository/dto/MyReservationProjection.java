package roomescape.reservation.domain.repository.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationStatus;

public interface MyReservationProjection {
    Long getId();
    String getThemeName();
    LocalDate getDate();
    LocalTime getTime();
    ReservationStatus getStatus();
    int getWaitingNumber();
}
