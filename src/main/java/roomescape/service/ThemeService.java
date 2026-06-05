package roomescape.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional(readOnly = true)
    public List<Theme> allTheme() {
        return themeRepository.findAll();
    }

    @Transactional
    public Theme saveTheme(String name, String description, String thumbnailUrl) {
        Theme theme = new Theme(name, description, thumbnailUrl);
        return themeRepository.save(theme);
    }

    @Transactional
    public void removeTheme(long themeId) {
        getThemeOrElseThrow(themeId);
        if (reservationRepository.existsByThemeId(themeId)) {
            throw new CustomException(ErrorCode.THEME_IS_REFERENCED);
        }
        themeRepository.deleteById(themeId);
    }

    @NonNull
    private Theme getThemeOrElseThrow(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new CustomException(ErrorCode.THEME_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Theme> findPopularThemes(Long topCount, Long during) {
        LocalDate fromDate = LocalDate.now().minusDays(during);
        LocalDate toDate = LocalDate.now();
        return themeRepository.findPopularThemes(topCount, fromDate, toDate);
    }
}
