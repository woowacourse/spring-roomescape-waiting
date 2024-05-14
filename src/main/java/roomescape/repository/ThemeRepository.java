package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Theme.Theme;

public interface ThemeRepository {

    Long save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    void delete(Long id);

    Boolean existId(Long id);

    Boolean existName(String name);
}
