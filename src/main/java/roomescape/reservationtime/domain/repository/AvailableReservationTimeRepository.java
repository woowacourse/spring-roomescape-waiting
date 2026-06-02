package roomescape.reservationtime.domain.repository;

import java.time.LocalDate;
import java.util.List;

public interface AvailableReservationTimeRepository {

    List<AvailableReservationTime> findByThemeAndDate(Long themeId, LocalDate date);
}
