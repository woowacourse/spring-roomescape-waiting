package roomescape.reservationtime.application.dao;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservationtime.application.dto.AvailableReservationTimeResult;

public interface AvailableTimeDao {
    List<AvailableReservationTimeResult> findByThemeAndDate(Long themeId, LocalDate date);
}
