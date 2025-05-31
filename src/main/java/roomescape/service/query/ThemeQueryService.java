package roomescape.service.query;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.slot.Theme;
import roomescape.dto.theme.ThemeResponseDto;
import roomescape.repository.JpaThemeRepository;

@Service
@Transactional
public class ThemeQueryService {

    private static final int POPULAR_THEMES_COUNT = 10;

    private final JpaThemeRepository themeRepository;
    private final Clock clock;

    public ThemeQueryService(JpaThemeRepository themeRepository, Clock clock) {
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    public List<ThemeResponseDto> findAllThemes() {
        List<Theme> allTheme = themeRepository.findAll();
        return allTheme.stream()
                .map(ThemeResponseDto::from)
                .toList();
    }

    public List<ThemeResponseDto> findPopularThemes() {
        LocalDate to = LocalDate.now(clock).minusDays(1);
        LocalDate from = to.minusDays(7);
        return themeRepository.findMostReservedThemesBetweenDate(from, to).stream()
                .limit(POPULAR_THEMES_COUNT)
                .map(ThemeResponseDto::from)
                .toList();
    }
}
