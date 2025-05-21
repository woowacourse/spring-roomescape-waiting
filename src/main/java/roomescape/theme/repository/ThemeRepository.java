package roomescape.theme.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeName;

public interface ThemeRepository extends CrudRepository<Theme, Long> {

    List<Theme> findAll();

    boolean existsByName(ThemeName name);
}
