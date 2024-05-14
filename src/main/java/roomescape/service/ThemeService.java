package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Theme;
import roomescape.repository.JdbcReservationRepository;
import roomescape.repository.JpaThemeRepository;
import roomescape.service.dto.theme.PopularThemeRequest;
import roomescape.service.dto.theme.ThemeRequest;
import roomescape.service.dto.theme.ThemeResponse;

@Service
public class ThemeService {

    private final JpaThemeRepository themeRepository;
    private final JdbcReservationRepository reservationRepository;

    public ThemeService(JpaThemeRepository themeRepository, JdbcReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ThemeResponse> findAllThemes() {
        return themeRepository.findAll()
                .stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public List<ThemeResponse> findTopBookedThemes(PopularThemeRequest request) {
        List<Theme> topBookedThemes = themeRepository.findTopThemesDescendingByDescription(
                request.getStartDate(), request.getEndDate(), request.getCount());

        return topBookedThemes.stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public ThemeResponse createTheme(ThemeRequest request) {
        Theme theme = themeRepository.save(request.toTheme());
        return new ThemeResponse(theme);
    }

    public void deleteTheme(long id) {
        if (reservationRepository.isReservationExistsByThemeId(id)) {
            throw new IllegalArgumentException("해당 테마에 예약이 있어 삭제할 수 없습니다.");
        }
        themeRepository.deleteById(id);
    }
}
