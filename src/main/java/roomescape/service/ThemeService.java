package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.infrastructure.ReservationRepository;
import roomescape.infrastructure.ThemeRepository;
import roomescape.service.exception.ReservationExistsException;
import roomescape.service.request.ThemeAppRequest;
import roomescape.service.response.ThemeAppResponse;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private static final int MAX_POPULAR_THEME_COUNT = 10;
    private static final int BASED_ON_PERIOD_POPULAR_THEME = 7;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ThemeAppResponse save(ThemeAppRequest request) {
        Theme theme = new Theme(request.name(), request.description(), request.thumbnail());
        validateDuplication(request);
        Theme savedTheme = themeRepository.save(theme);

        return ThemeAppResponse.from(savedTheme);
    }

    private void validateDuplication(ThemeAppRequest request) {
        if (themeRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("이미 존재하는 테마 입니다.");
        }
    }

    @Transactional
    public void delete(Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new ReservationExistsException();
        }
        themeRepository.deleteById(id);
    }

    public List<ThemeAppResponse> findAll() {
        return themeRepository.findAll().stream()
                .map(ThemeAppResponse::from)
                .toList();

    }

    public List<ThemeAppResponse> findPopular() {
        LocalDate from = LocalDate.now().minusDays(BASED_ON_PERIOD_POPULAR_THEME);
        LocalDate to = LocalDate.now().minusDays(1);
        Pageable pageable = PageRequest.of(0, MAX_POPULAR_THEME_COUNT);
        Page<Theme> page = themeRepository.findMostReservedThemesInPeriod(pageable, from, to);
        return page.getContent().stream()
                .map(ThemeAppResponse::from)
                .toList();
    }
}
