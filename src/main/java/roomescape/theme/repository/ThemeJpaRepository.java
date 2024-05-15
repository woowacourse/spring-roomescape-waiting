package roomescape.theme.repository;

import org.springframework.data.repository.CrudRepository;
import roomescape.theme.domain.Name;
import roomescape.theme.domain.Theme;

public interface ThemeJpaRepository extends CrudRepository<Theme, Long> {

    boolean existsByName(Name name);
}
