package roomescape.theme.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import roomescape.common.exception.BusinessException;
import roomescape.common.exception.ErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ThemeService(ThemeRepository themeRepository, Clock clock) {
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    public Theme getById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.THEME_NOT_FOUND));
    }

    public List<ThemeResponse> getTopThemes(int limit) {
        LocalDate startDate = LocalDate.now(clock).minusDays(7);
        LocalDate endDate = LocalDate.now(clock);

        List<Long> themeIds = themeRepository.findTopThemeIds(startDate, endDate, limit);
        Map<Long, Theme> themeMap = themeRepository.findAllByIds(themeIds).stream()
                .collect(Collectors.toMap(Theme::getId, theme -> theme));
        return themeIds.stream()
                .map(themeMap::get)
                .map(ThemeResponse::from)
                .collect(Collectors.toList());
    }
}
