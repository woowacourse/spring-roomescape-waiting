package roomescape.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.repository.ThemeDao;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ThemeQueryService {

    private final ThemeDao themeDao;

    public List<Theme> findAllThemes() {
        return themeDao.findAll().stream()
                .toList();
    }

    public List<Theme> findPopularThemesBy(LocalDate startAt, LocalDate endAt, int limit) {
        return themeDao.findPopularBetween(startAt, endAt, limit).stream()
                .toList() ;
    }
}
