package roomescape.theme.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.CreateThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.theme.repository.ThemeRepository;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(final ThemeRepository themeRepository, final ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ThemeResponse createTheme(final CreateThemeRequest createThemeRequest) {
        Theme theme = createThemeRequest.convertToTheme();
        if (themeRepository.existsByName(theme.getThemeName())) {
            throw new IllegalArgumentException("해당 이름의 테마는 이미 존재합니다.");
        }
        final Theme savedTheme = themeRepository.save(theme);
        return new ThemeResponse(savedTheme);
    }

    public List<ThemeResponse> findAll() {
        final List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public void deleteThemeById(final Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new IllegalArgumentException("예약이 존재하는 테마는 삭제할 수 없습니다.");
        }
        themeRepository.deleteById(id);
    }

    public List<ThemeResponse> findPopularThemes() {
        final int POPULAR_THEME_LIMIT = 10;
        final LocalDate SEVEN_DAYS_AGO = LocalDate.now().minusDays(7);
        final LocalDate ONE_DAY_AGO = LocalDate.now().minusDays(1);

        final List<Theme> themes = reservationRepository.findByDateBetween(SEVEN_DAYS_AGO, ONE_DAY_AGO).stream()
                .map(Reservation::getTheme)
                .toList();
        final Map<Theme, Integer> themeCount = new HashMap<>();
        for (Theme theme : themes) {
            themeCount.putIfAbsent(theme, 0);
            themeCount.computeIfPresent(theme, (t, count) -> count + 1);
        }
        return themeCount.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .map(Map.Entry::getKey)
                .map(ThemeResponse::new)
                .limit(POPULAR_THEME_LIMIT)
                .toList();
    }
}
