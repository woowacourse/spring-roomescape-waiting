package roomescape.theme.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;

public interface ThemeRepository {
    Theme save(String name, String description, String thumbnail);

    void deleteById(Long id);

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    List<Theme> findPopularThemeDuringAWeek(int limit, LocalDate now);
}
