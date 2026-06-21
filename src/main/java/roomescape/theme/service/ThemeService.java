package roomescape.theme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.theme.controller.dto.request.ThemeCreateRequest;
import roomescape.theme.controller.dto.response.ThemeResponse;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ThemeService {

    private static final int POPULAR_THEME_PERIOD_DAYS = 7;
    private static final String THEME_NOT_FOUND_MESSAGE = "존재하지 않는 테마입니다.";
    private static final String THEME_IN_USE_MESSAGE = "해당 테마에 예약이 존재하여 삭제할 수 없습니다.";

    private final ThemeRepository themeRepository;

    public ThemeResponse create(final ThemeCreateRequest request) {
        final Theme themeWithoutId = Theme.create(
                request.name(),
                request.description(),
                request.thumbnailUrl(),
                request.price()
        );

        Theme theme = themeRepository.save(themeWithoutId);

        return ThemeResponse.from(theme);
    }

    public void delete(final Long themeId) {
        boolean deleted = deleteTheme(themeId);

        if (!deleted) {
            throw new NotFoundException(THEME_NOT_FOUND_MESSAGE);
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
            throw new ConflictException(THEME_IN_USE_MESSAGE, exception);
        }
    }
}
