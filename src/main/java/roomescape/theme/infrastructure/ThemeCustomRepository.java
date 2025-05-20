package roomescape.theme.infrastructure;

import java.util.List;
import roomescape.reservation.domain.ReservationPeriod;
import roomescape.theme.domain.Theme;

public interface ThemeCustomRepository {
    List<Theme> findPopularThemes(ReservationPeriod period, int popularCount);
}
