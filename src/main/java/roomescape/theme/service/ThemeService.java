package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.util.time.DateTime;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.exception.ThemeException;
import roomescape.theme.presentation.dto.PopularThemeResponse;
import roomescape.theme.presentation.dto.ThemeRequest;
import roomescape.theme.presentation.dto.ThemeResponse;

@Service
public class ThemeService {

    private static final int POPULAR_THEME_COUNT = 10;
    public static final int POPULAR_THEME_RANGE_START_SUBTRACT = 8;
    public static final int POPULAR_THEME_RANGE_END_SUBTRACT = 1;

    private final DateTime dateTime;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(final DateTime dateTime,
                        final ThemeRepository themeRepository,
                        final ReservationRepository reservationRepository) {
        this.dateTime = dateTime;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse createTheme(final ThemeRequest request) {
        Theme theme = Theme.createWithoutId(request.name(), request.description(), request.thumbnail());
        Long id = themeRepository.save(theme);

        return ThemeResponse.from(theme.assignId(id));
    }

    public void deleteThemeById(final Long id) {
        validateExistIdToDelete(id);
        
        boolean isDeleted = themeRepository.deleteById(id);
        validateIsExistsReservationTimeId(isDeleted);
    }

    private void validateExistIdToDelete(final Long id) {
        if (reservationRepository.existBy(id)) {
            throw new ThemeException("해당 테마의 예약이 존재해서 삭제할 수 없습니다.");
        }
    }

    private void validateIsExistsReservationTimeId(boolean isDeleted) {
        if (!isDeleted) {
            throw new ThemeException("존재하지 않는 테마입니다.");
        }
    }

    public List<ThemeResponse> getThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<PopularThemeResponse> getPopularThemes() {
        LocalDate now = dateTime.now().toLocalDate();

        LocalDate start = now.minusDays(POPULAR_THEME_RANGE_START_SUBTRACT);
        LocalDate end = now.minusDays(POPULAR_THEME_RANGE_END_SUBTRACT);

        return themeRepository.findPopularThemes(start, end, POPULAR_THEME_COUNT).stream()
                .map(theme -> new PopularThemeResponse(theme.getName(), theme.getDescription(), theme.getThumbnail()))
                .toList();
    }
}
