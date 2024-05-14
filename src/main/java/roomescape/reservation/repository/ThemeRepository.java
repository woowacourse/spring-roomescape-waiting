package roomescape.reservation.repository;

import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.Theme;

import java.util.List;
import java.util.Optional;

public interface ThemeRepository {

    Optional<Theme> findById(Long themeId);

    List<Theme> findAll();

    Theme save(Theme theme);

    int deleteById(Long themeId);

    List<Theme> findPopularThemes(ReservationDate startAt, ReservationDate endAt, int maximumThemeCount);
}
