package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import roomescape.theme.domain.Theme;

public interface ThemeRepository extends CrudRepository<Theme, Long> {

    Theme save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(long id);

    void deleteById(long themeId);
}
