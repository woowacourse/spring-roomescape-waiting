package roomescape.theme.domain;

import java.util.List;
import roomescape.reservation.domain.ReservationPeriod;

public interface ThemeCustomRepository {
     List<Theme> findPopularThemes(ReservationPeriod period, int popularCount);
}
