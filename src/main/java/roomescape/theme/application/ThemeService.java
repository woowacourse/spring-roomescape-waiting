package roomescape.theme.application;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.presentation.ThemeRequest;
import roomescape.theme.presentation.ThemeResponse;

@Service
public class ThemeService {
    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ThemeService(ReservationRepository reservationRepository, ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public ThemeResponse createTheme(final ThemeRequest request) {
        Theme theme = request.toTheme();
        return new ThemeResponse(themeRepository.save(theme));
    }

    public List<ThemeResponse> getThemes() {
        return themeRepository.findAll().stream()
                .map(ThemeResponse::new)
                .toList();
    }

    public List<ThemeResponse> getPopularThemes() {
        return themeRepository.findPopularThemes().stream()
                .map(ThemeResponse::new)
                .toList();
    }

    @Transactional
    public void deleteTheme(final Long themeId) {
        validateThemeHasNoReservations(themeId);

        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalStateException("이미 삭제되어 있는 리소스입니다."));

        themeRepository.delete(theme);
    }

    private void validateThemeHasNoReservations(final Long themeId) {
        if (reservationRepository.existsByThemeId(themeId)) {
            throw new IllegalStateException("해당 테마로 예약된 정보가 있어 삭제할 수 없습니다.");
        }
    }
}
