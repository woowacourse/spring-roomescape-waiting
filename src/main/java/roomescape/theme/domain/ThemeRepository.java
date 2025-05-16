package roomescape.theme.domain;

import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.ReservationPeriod;

public interface ThemeRepository {

    Theme save(Theme theme);

    List<Theme> findAll();

    void deleteById(Long id);

    Optional<Theme> findById(Long id);

    List<Theme> findPopularThemes(ReservationPeriod period, int popularCount);
}
