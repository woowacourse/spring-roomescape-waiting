package roomescape.repository;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import roomescape.domain.Theme;

public interface ThemeRepository extends CrudRepository<Theme, Long> {

    List<Theme> findAll();
}
