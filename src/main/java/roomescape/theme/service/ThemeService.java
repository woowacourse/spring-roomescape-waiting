package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.exception.BusinessException;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.presentation.dto.PopularThemeResponse;
import roomescape.theme.presentation.dto.ThemeRequest;
import roomescape.theme.presentation.dto.ThemeResponse;

@Service
public class ThemeService {

    private static final int POPULAR_THEME_COUNT = 10;
    public static final int POPULAR_THEME_RANGE_START_SUBTRACT = 8;
    public static final int POPULAR_THEME_RANGE_END_SUBTRACT = 1;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(final ThemeRepository themeRepository,
                        final ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse createTheme(final ThemeRequest request) {
        Theme theme = themeRepository.save(
            Theme.createWithoutId(request.name(), request.description(), request.thumbnail()));

        return ThemeResponse.from(theme);
    }

    public void deleteThemeById(final Long id) {
        validateExistsIdToDelete(id);
        validateExistsTheme(id);
        themeRepository.deleteById(id);
    }

    private void validateExistsTheme(Long id) {
        if (!themeRepository.existsById(id)) {
            throw new BusinessException("해당 테마가 존재하지 않습니다.");
        }
    }

    private void validateExistsIdToDelete(final Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new BusinessException("해당 테마의 예약이 존재해서 삭제할 수 없습니다.");
        }
    }

    public List<ThemeResponse> getThemes() {
        return themeRepository.findAll().stream()
            .map(ThemeResponse::from)
            .toList();
    }

    public List<PopularThemeResponse> getPopularThemes() {
        LocalDate now = LocalDate.now();

        LocalDate start = now.minusDays(POPULAR_THEME_RANGE_START_SUBTRACT);
        LocalDate end = now.minusDays(POPULAR_THEME_RANGE_END_SUBTRACT);

        return themeRepository.findPopularThemes(start, end).stream()
            .map(theme -> new PopularThemeResponse(theme.getName(), theme.getDescription(), theme.getThumbnail()))
            .limit(POPULAR_THEME_COUNT)
            .toList();
    }
}
