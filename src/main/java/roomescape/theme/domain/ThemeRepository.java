package roomescape.theme.domain;

import java.util.List;
import roomescape.reservation.domain.ReservationPeriod;

public interface ThemeRepository {

    Long save(Theme theme);

    List<Theme> findPopularThemes(ReservationPeriod period, int popularCount);

    List<Theme> findAll();

    boolean deleteById(Long id);

    Theme findById(Long id);
}
