package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.theme.domain.Theme;
import roomescape.theme.dto.CreateThemeRequest;
import roomescape.theme.dto.ThemeResponse;
import roomescape.reservation.repository.ReservationRepository;
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
        final LocalDate from = LocalDate.now().minusDays(7);
        final LocalDate to = LocalDate.now().minusDays(1);
        final List<Theme> popularThemes = themeRepository.findPopularThemes(from, to, 10);
        return popularThemes.stream()
                .map(ThemeResponse::new)
                .toList();
    }
}
