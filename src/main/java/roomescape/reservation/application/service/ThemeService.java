package roomescape.reservation.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.application.repository.ReservationRepository;
import roomescape.reservation.application.repository.ThemeRepository;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.presentation.dto.ThemeRequest;
import roomescape.reservation.presentation.dto.ThemeResponse;

@Service
public class ThemeService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ThemeService(ReservationRepository reservationRepository, ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    @Transactional
    public ThemeResponse createTheme(final ThemeRequest themeRequest) {
        return new ThemeResponse(themeRepository.insert(themeRequest));
    }

    public List<ThemeResponse> getThemes() {
        List<Theme> themes = themeRepository.findAllThemes();
        return themes.stream()
                .map(ThemeResponse::new)
                .toList();
    }

    @Transactional
    public void deleteTheme(final Long id) {
        validateIsDuplicated(id);

        if (themeRepository.delete(id) == 0) {
            throw new IllegalStateException("이미 삭제되어 있는 리소스입니다.");
        }
    }

    private void validateIsDuplicated(Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new IllegalStateException("예약이 이미 존재하는 테마입니다.");
        }
    }

    public List<ThemeResponse> getPopularThemes() {
        return themeRepository.findPopularThemes().stream()
                .map(ThemeResponse::new)
                .toList();
    }
}
