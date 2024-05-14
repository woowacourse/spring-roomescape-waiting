package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.service.exception.ReservationExistsException;
import roomescape.service.request.ThemeAppRequest;
import roomescape.service.response.ThemeAppResponse;

@Service
public class ThemeService {

    private static final int MAX_POPULAR_THEME_COUNT = 10;
    private static final int BASED_ON_PERIOD_POPULAR_THEME = 7;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeAppResponse save(ThemeAppRequest request) {
        Theme theme = new Theme(request.name(), request.description(), request.thumbnail());
        validateDuplication(request);
        Theme savedTheme = themeRepository.save(theme);

        return ThemeAppResponse.from(savedTheme);
    }

    private void validateDuplication(ThemeAppRequest request) {
        if (themeRepository.isNameExists(request.name())) {
            throw new IllegalArgumentException("이미 존재하는 테마 입니다.");
        }
    }

    public int delete(Long id) {
        if (reservationRepository.isThemeIdExists(id)) {
            throw new ReservationExistsException();
        }
        return themeRepository.deleteById(id);
    }

    public List<ThemeAppResponse> findAll() {
        return themeRepository.findAll().stream()
            .map(ThemeAppResponse::from)
            .toList();
    }

    public List<ThemeAppResponse> findPopular() {
        return themeRepository.findMostReservedThemesInPeriod(BASED_ON_PERIOD_POPULAR_THEME,
                MAX_POPULAR_THEME_COUNT).stream()
            .map(ThemeAppResponse::from)
            .toList();
    }
}
