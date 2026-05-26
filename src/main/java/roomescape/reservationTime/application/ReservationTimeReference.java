package roomescape.reservationTime.application;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservationTime.domain.ReservationTime;

public interface ReservationTimeReference {
    void validateReservationTimeNotReferenced(Long timeId);
    List<ReservationTime> getBookedTimes(LocalDate date, Long themeId);
}
