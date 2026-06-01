package roomescape.theme.application.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.theme.application.dto.PopularThemeResult;
import roomescape.theme.application.dto.ThemeResult;
import roomescape.theme.application.dao.PopularThemeDao;
import roomescape.theme.domain.PopularThemePeriod;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ThemeQueryService {

    private final ThemeRepository themeRepository;
    private final PopularThemeDao popularThemeDao;

    public List<ThemeResult> findAll() {
        List<Theme> themes = themeRepository.findAll();

        return themes.stream().map(ThemeResult::from)
                .toList();
    }

    public List<PopularThemeResult> findPopularThemes(LocalDate now) {
        return popularThemeDao.findTop10PopularThemes(PopularThemePeriod.from(now));
    }
}
