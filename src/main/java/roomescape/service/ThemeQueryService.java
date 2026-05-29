package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.dao.ThemeDao;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThemeQueryService {

    private final ThemeDao themeDao;

    public List<Theme> findAllThemes() {
        return themeDao.findAllThemes().stream()
                .toList();
    }

    public List<Theme> findPopularThemesBy(LocalDate startAt, LocalDate endAt, int limit) {
        return themeDao.findSortedPopularThemesBy(startAt, endAt, limit).stream()
                .toList();
    }
}
