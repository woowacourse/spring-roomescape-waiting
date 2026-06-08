package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Theme;
import roomescape.exception.NotFoundException;
import roomescape.exception.ResourceInUseException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Theme> findAllThemes() {
        return themeRepository.findAll();
    }

    public Theme getThemeById(long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 테마를 찾을 수 없습니다."));
    }

    @Transactional
    public Theme saveTheme(String name, String description, String thumbnailUrl) {
        Theme theme = new Theme(name, description, thumbnailUrl);
        return themeRepository.save(theme);
    }

    @Transactional
    public void removeTheme(long themeId) {
        getThemeById(themeId);
        validateThemeDeletable(themeId);
        themeRepository.deleteById(themeId);
    }

    public List<Theme> findPopularThemes(long limit, long days) {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = LocalDate.now().minusDays(days);
        return themeRepository.findPopularThemes(limit, startDate, endDate);
    }

    private void validateThemeDeletable(long themeId) {
        if (reservationRepository.existsByThemeId(themeId)) {
            throw new ResourceInUseException("테마");
        }
    }
}
