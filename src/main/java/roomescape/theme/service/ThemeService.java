package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeRankResponse;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ThemeService {
    public static final int NUMBER_OF_ONE_DAY = 1;
    public static final int NUMBER_OF_ONE_WEEK = 7;
    public static final int TOP_THEMES_LIMIT = 10;

    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<ThemeResponse> findThemes() {
        List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(ThemeResponse::fromTheme)
                .toList();
    }

    public List<ThemeRankResponse> findRankedThemes() {
        LocalDate yesterday = LocalDate.now().minusDays(NUMBER_OF_ONE_DAY);
        LocalDate beforeOneWeek = yesterday.minusDays(NUMBER_OF_ONE_WEEK);

        List<Theme> rankedThemes = themeRepository
                .findThemesByReservationDateOrderByReservationCountDesc(beforeOneWeek, yesterday);
        return rankedThemes.stream()
                .limit(TOP_THEMES_LIMIT)
                .map(ThemeRankResponse::fromTheme)
                .toList();
    }

    public ThemeResponse addTheme(ThemeRequest themeRequest) {
        Theme theme = themeRequest.toTheme();
        Theme savedTheme = themeRepository.save(theme);
        return ThemeResponse.fromTheme(savedTheme);
    }

    public void removeTheme(long id) {
        themeRepository.deleteById(id);
    }
}
