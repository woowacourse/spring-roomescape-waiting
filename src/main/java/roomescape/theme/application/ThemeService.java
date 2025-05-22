package roomescape.theme.application;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.theme.application.dto.ThemeRequest;
import roomescape.theme.application.dto.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.exception.UsingThemeException;

@Service
@AllArgsConstructor
public class ThemeService {
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeResponse create(ThemeRequest request) {
        Theme theme = new Theme(request.name(), request.description(), request.thumbnail());
        return ThemeResponse.from(themeRepository.save(theme));
    }

    public List<ThemeResponse> findAll() {
        return ThemeResponse.from(themeRepository.findAll());
    }

    public List<ThemeResponse> findRankedByPeriod() {
        List<Theme> topRankedThemes = themeRepository.findRankedByPeriod(
                LocalDate.now().minusDays(7),
                LocalDate.now().minusDays(1),
                10
        );
        return ThemeResponse.from(topRankedThemes);
    }

    public void deleteById(final Long id) {
        validateUnUsedTheme(id);
        themeRepository.deleteById(id);
    }

    private void validateUnUsedTheme(Long id) {
        if (reservationRepository.existsByThemeId(id)) {
            throw new UsingThemeException();
        }
    }
}
