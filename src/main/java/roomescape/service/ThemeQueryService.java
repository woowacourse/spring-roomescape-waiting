package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ThemeDao;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeQueryService {

    private final ThemeDao themeDao;

    public List<Theme> findAllThemes() {
        return themeDao.findAllThemes();
    }

    public List<Theme> findPopularThemesBy(LocalDate startAt, LocalDate endAt, int limit) {
        return themeDao.findSortedPopularThemesBy(startAt, endAt, limit);
    }
}
