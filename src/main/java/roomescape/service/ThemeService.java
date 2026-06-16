package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Theme;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ThemeService {

    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ThemeService(ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Theme> allTheme() {
        return themeRepository.findAll();
    }

    public Theme saveTheme(String name, String description, String thumbnailUrl, Long price) {
        Theme theme = new Theme(name, description, thumbnailUrl, price);
        return themeRepository.save(theme);
    }

    public void removeTheme(long themeId) {
        getThemeOrElseThrow(themeId);
        if (reservationRepository.existsByThemeId(themeId)) {
            throw new CustomException(ErrorCode.THEME_IS_REFERENCED);
        }
        themeRepository.deleteById(themeId);
    }

    public Theme findTheme(long themeId) {
        return getThemeOrElseThrow(themeId);
    }

    private Theme getThemeOrElseThrow(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new CustomException(ErrorCode.THEME_NOT_FOUND));
    }

    public List<Theme> findPopularThemes(Long topCount, Long during) {
        LocalDate fromDate = LocalDate.now().minusDays(during);
        LocalDate toDate = LocalDate.now();
        return themeRepository.findPopularThemes(topCount, fromDate, toDate);
    }
}
