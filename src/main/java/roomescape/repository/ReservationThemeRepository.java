package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTheme;

public interface ReservationThemeRepository {

    Optional<ReservationTheme> findById(final Long id);

    List<ReservationTheme> findWeeklyThemeOrderByCountDesc();

    List<ReservationTheme> findAll();

    ReservationTheme save(final ReservationTheme reservationTheme);

    int deleteById(final long id);

    boolean existsByName(final String name);
}
