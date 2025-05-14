package roomescape.repository;

import org.springframework.data.repository.ListCrudRepository;
import roomescape.domain.Theme;

public interface ThemeRepository extends ListCrudRepository<Theme, Long> {

}
