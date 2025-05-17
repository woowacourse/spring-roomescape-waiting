package roomescape.theme.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeName;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    boolean existsByName(ThemeName name);
}
