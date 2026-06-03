package roomescape.theme.application.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.RoomEscapeException;
import roomescape.theme.application.dto.PopularThemeQueryResult;
import roomescape.theme.application.dto.ThemeCreateCommand;
import roomescape.theme.application.dto.ThemeQueryResult;
import roomescape.theme.application.exception.ThemeErrorCode;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@RequiredArgsConstructor
@Transactional
@Service
public class ThemeService {

    private final ThemeRepository themeRepository;

    @Transactional(readOnly = true)
    public ThemeQueryResult findById(Long id) {
        return ThemeQueryResult.from(themeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeException(ThemeErrorCode.THEME_NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public List<ThemeQueryResult> findAll() {
        return themeRepository.findAll().stream()
                .map(ThemeQueryResult::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PopularThemeQueryResult> findPopularThemes(LocalDate today) {
        return themeRepository.findTop10PopularThemesBetween(today.minusWeeks(1), today.minusDays(1))
                .stream()
                .map(PopularThemeQueryResult::from)
                .toList();
    }

    public ThemeQueryResult save(ThemeCreateCommand request) {
        Theme theme = request.toEntity();
        validateDuplicateTheme(theme);
        return ThemeQueryResult.from(themeRepository.save(theme));
    }

    public int delete(long id) {
        return themeRepository.delete(id);
    }

    private void validateDuplicateTheme(Theme theme) {
        if (themeRepository.existsByNameAndDescription(theme)) {
            throw new RoomEscapeException(ThemeErrorCode.DUPLICATE_THEME);
        }
    }
}
