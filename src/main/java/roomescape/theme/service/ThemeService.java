package roomescape.theme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.exception.ThemeInUseException;
import roomescape.theme.domain.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.service.dto.request.ThemeCreateRequest;
import roomescape.theme.service.dto.response.ThemeResponse;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private static final int POPULAR_THEME_PERIOD_DAYS = 7;
    private final ThemeRepository themeRepository;

    public ThemeResponse create(final ThemeCreateRequest request) {
        final Theme themeWithoutId = Theme.create(
                request.name(),
                request.description(),
                request.thumbnailUrl()
        );

        Theme theme = themeRepository.save(themeWithoutId);

        return ThemeResponse.from(theme);
    }

    public void delete(final Long themeId) {
        boolean deleted = deleteTheme(themeId);

        if (!deleted) {
            throw new ThemeNotFoundException();
        }
    }

    public List<ThemeResponse> getPopularThemes() {
        final LocalDate today = LocalDate.now();
        final LocalDate startDate = today.minusDays(POPULAR_THEME_PERIOD_DAYS);

        return themeRepository.findPopularThemes(startDate, today)
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    private boolean deleteTheme(final Long themeId) {
        try {
            return themeRepository.deleteById(themeId);
        } catch (DataIntegrityViolationException exception) {
            throw new ThemeInUseException(exception);
        }
    }
}
