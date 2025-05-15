package roomescape.reservation.dao.theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import roomescape.reservation.model.Theme;

public interface ThemeDao {

    Theme save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    int deleteById(Long id);

    boolean existsByName(String name);
}