package roomescape.theme.application.dao;

import java.util.List;
import roomescape.theme.application.dto.PopularThemeResult;
import roomescape.theme.domain.PopularThemePeriod;

public interface PopularThemeDao {
    List<PopularThemeResult> findTop10PopularThemes(PopularThemePeriod period);
}
