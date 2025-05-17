package roomescape.theme.application.service;

import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.theme.domain.ThemeName;

import java.util.List;

public interface ThemeQueryService {

    boolean existsById(ThemeId id);

    boolean existsByName(ThemeName name);

    List<Theme> getAll();

    Theme get(ThemeId id);

    List<Theme> getRanking(ReservationDate startDate, ReservationDate endDate, int count);
}
