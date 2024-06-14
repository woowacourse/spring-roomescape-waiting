package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.system.exception.error.ErrorType;
import roomescape.system.exception.model.NotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.dto.ThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.dto.ThemesResponse;

@Service
public class ThemeService {
    private final ThemeRepository themeRepository;

    public ThemeService(final ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Theme findThemeById(final Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorType.THEME_NOT_FOUND,
                        String.format("테마(Theme) 정보가 존재하지 않습니다. [themeId: %d]", id)));
    }

    public ThemesResponse findAllThemes() {
        final List<ThemeResponse> response = themeRepository.findAll()
                .stream()
                .map(ThemeResponse::from)
                .toList();

        return new ThemesResponse(response);
    }

    public ThemesResponse getTop10Themes(final LocalDate today) {
        final LocalDate startDate = today.minusDays(7);
        final LocalDate endDate = today.minusDays(1);
        final int limit = 10;

        final List<ThemeResponse> response = themeRepository.findTopNThemeBetweenStartDateAndEndDate(startDate, endDate,
                        limit)
                .stream()
                .map(ThemeResponse::from)
                .toList();

        return new ThemesResponse(response);
    }

    public ThemeResponse addTheme(final ThemeRequest request) {
        final Theme theme = themeRepository.save(new Theme(request.name(), request.description(), request.thumbnail()));

        return ThemeResponse.from(theme);
    }

    public void removeThemeById(final Long id) {
        themeRepository.deleteById(id);
    }
}
