package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.entity.Theme;

@Repository
public interface ThemeRepository {
    Theme add(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    boolean existsReservationByThemeId(long id);

    void deleteById(Long id);
}
