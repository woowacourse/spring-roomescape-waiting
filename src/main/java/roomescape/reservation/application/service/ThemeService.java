package roomescape.reservation.application.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.time.CurrentDateTime;
import roomescape.reservation.application.dto.ThemeCreateCommand;
import roomescape.reservation.application.dto.ThemeInfo;
import roomescape.reservation.domain.reservation.ReservationRepository;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.theme.ThemeRepository;

@Service
public class ThemeService {
    private static final int POPULAR_THEME_FROM_DAYS_AGO = 7;
    private static final int POPULAR_THEME_TO_DAYS_AGO = 1;
    private static final int POPULAR_THEME_MAX_COUNT = 10;

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;
    private final CurrentDateTime currentDateTime;

    public ThemeService(final ThemeRepository themeRepository, final ReservationRepository reservationRepository,
                        final CurrentDateTime currentDateTime) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
        this.currentDateTime = currentDateTime;
    }

    public ThemeInfo createTheme(final ThemeCreateCommand command) {
        final Theme theme = command.convertToTheme();
        if (themeRepository.existsByThemeName(theme.themeName())) {
            throw new IllegalArgumentException("해당 이름의 테마는 이미 존재합니다.");
        }
        final Theme savedTheme = themeRepository.save(theme);
        return new ThemeInfo(savedTheme);
    }

    public List<ThemeInfo> findAll() {
        final List<Theme> themes = themeRepository.findAll();
        return themes.stream()
                .map(ThemeInfo::new)
                .toList();
    }

    public void deleteThemeById(final Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new IllegalArgumentException("예약이 존재하는 테마는 삭제할 수 없습니다.");
        }
        themeRepository.deleteById(id);
    }

    public List<ThemeInfo> findPopularThemes() {
        final LocalDate today = currentDateTime.getDate();
        final LocalDate from = today.minusDays(POPULAR_THEME_FROM_DAYS_AGO);
        final LocalDate to = today.minusDays(POPULAR_THEME_TO_DAYS_AGO);
        final List<Theme> popularThemes = themeRepository.findPopularThemes(from, to, POPULAR_THEME_MAX_COUNT);
        return popularThemes.stream()
                .map(ThemeInfo::new)
                .toList();
    }
}
