package roomescape.reservation.domain;

import roomescape.reservation.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Theme save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    void deleteById(Long id);

    List<Theme> findAllByDateBetweenAndOrderByReservationCount(LocalDate startDate, LocalDate endDate, int limit);
}
