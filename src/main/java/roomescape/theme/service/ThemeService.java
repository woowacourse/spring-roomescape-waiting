package roomescape.theme.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.dto.request.ThemeCreateRequest;
import roomescape.theme.service.dto.response.ThemeResponse;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private static final int POPULAR_THEME_PERIOD_DAYS = 7;

    private final ThemeRepository themeRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<Theme> findAll() {
        return themeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Theme getById(final long themeId) {
        return themeRepository.findById(themeId)
            .orElseThrow(ThemeNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<ThemeResponse> getPopularThemes() {
        final LocalDate today = LocalDate.now(clock);
        final LocalDate startDate = today.minusDays(POPULAR_THEME_PERIOD_DAYS);

        return themeRepository.findPopularThemes(startDate, today)
            .stream()
            .map(ThemeResponse::from)
            .toList();
    }

    @Transactional
    public ThemeResponse create(final ThemeCreateRequest request) {
        final Theme themeWithoutId = Theme.create(
            request.name(),
            request.description(),
            request.thumbnailUrl()
        );

        Theme theme = themeRepository.save(themeWithoutId);
        return ThemeResponse.from(theme);
    }

    @Transactional
    public void delete(final Long themeId) {
        boolean deleted = deleteTheme(themeId);

        if (!deleted) {
            throw new ThemeNotFoundException();
        }
    }

    private boolean deleteTheme(final Long themeId) {
        return themeRepository.deleteById(themeId);
    }
}
