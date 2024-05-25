package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.service.dto.request.CreateThemeRequest;
import roomescape.service.dto.response.ThemeResponse;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;

    public ThemeService(ReservationRepository reservationRepository, ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
    }

    public List<ThemeResponse> getAllThemes() {
        List<Theme> themes = themeRepository.findAll();

        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional
    public ThemeResponse addTheme(CreateThemeRequest request) {
        Theme theme = request.toTheme();
        validateDuplicateName(theme);
        Theme savedTheme = themeRepository.save(theme);
        return ThemeResponse.from(savedTheme);
    }

    private void validateDuplicateName(Theme theme) {
        if (themeRepository.existsByName(theme.getName())) {
            throw new IllegalArgumentException("해당 이름의 테마는 이미 존재합니다.");
        }
    }

    @Transactional
    public void deleteThemeById(Long id) {
        Theme theme = getThemeById(id);
        validateReservedTheme(theme);
        themeRepository.delete(theme);
    }

    private Theme getThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 id의 테마가 존재하지 않습니다."));
    }

    private void validateReservedTheme(Theme theme) {
        if (reservationRepository.existsByTheme(theme)) {
            throw new IllegalArgumentException("해당 테마를 사용하는 예약이 존재합니다.");
        }
    }

    public List<ThemeResponse> getPopularThemes(LocalDate startDate, LocalDate endDate, int limit) {
        List<Theme> themes = themeRepository.findPopularThemes(startDate, endDate, limit);

        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }
}
