package roomescape.theme.application.port.out;

import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {
    Theme save(Theme domain);

    void deleteById(long id);

    List<Theme> findThemesBySlotDate(LocalDate date);

    List<Theme> findPopularThemeByCurrentDate(LocalDate currentDate);

    Optional<Theme> findById(long id);

    List<Theme> findAll();

    boolean existsAlreadyTheme(String name);
}
