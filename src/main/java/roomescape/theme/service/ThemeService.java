package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;

import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.dto.ThemeSaveServiceDto;

public interface ThemeService {
    List<Theme> getAll();
    Theme create(ThemeSaveServiceDto theme);
    void deleteById(Long id);
    List<ReservationTime> getAvailableTimes(Long themeId, LocalDate date);
    List<Theme> getBestThemes();

}
