package roomescape.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.model.Theme;

public interface ThemeDao {
    List<Theme> findAll();

    Long saveTheme(Theme theme);

    Optional<Theme> findById(Long id);

    boolean isDuplicatedNameExisted(String name);

    List<Theme> getTopReservedThemesSince(LocalDate today, int dayRange, int size);

    void deleteById(Long id);
}
