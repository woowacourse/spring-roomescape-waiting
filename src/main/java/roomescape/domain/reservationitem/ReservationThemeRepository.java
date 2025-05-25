package roomescape.domain.reservationitem;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationThemeRepository {

    Optional<ReservationTheme> findById(final Long id);

    List<ReservationTheme> findWeeklyThemeOrderByCountDesc(int amount, LocalDate dateFrom, LocalDate dateTo);

    List<ReservationTheme> findAll();

    ReservationTheme save(final ReservationTheme reservationTheme);

    void deleteById(final long id);

    boolean existsByName(final String name);
}
