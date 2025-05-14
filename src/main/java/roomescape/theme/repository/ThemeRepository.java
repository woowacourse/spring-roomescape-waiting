package roomescape.theme.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import roomescape.theme.domain.Theme;

public interface ThemeRepository extends CrudRepository<Theme, Long> {

    boolean existsByName(String name);

    List<Theme> findAll();
}
