package roomescape.reservationtime.application.port.in;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservationtime.application.dto.response.AvailableTimeFindResponse;
import roomescape.reservationtime.application.dto.response.ReservationTimeFindResponse;

public interface FindReservationTimeUseCase {
    List<ReservationTimeFindResponse> findAll();
    List<AvailableTimeFindResponse> findTimesByDateAndThemeId(LocalDate date, long themeId);
}
