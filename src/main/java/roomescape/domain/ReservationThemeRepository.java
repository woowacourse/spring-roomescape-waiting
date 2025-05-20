package roomescape.domain;

import java.util.List;
import java.util.Optional;

public interface ReservationThemeRepository {

    Optional<ReservationTheme> findById(final Long id);

    List<ReservationTheme> findWeeklyThemeOrderByCountDesc();

    List<ReservationTheme> findAll();

    ReservationTheme save(final ReservationTheme reservationTheme);

    void deleteById(final long id);

    boolean existsByName(final String name);
}
