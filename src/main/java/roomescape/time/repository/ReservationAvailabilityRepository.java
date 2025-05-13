package roomescape.time.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.time.domain.ReservationAvailability;
import roomescape.theme.domain.Theme;

public interface ReservationAvailabilityRepository {

    List<ReservationAvailability> findAllByDateAndTheme(LocalDate date, Theme theme);
}
