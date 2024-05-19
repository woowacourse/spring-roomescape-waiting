package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    boolean existsByName(ThemeName name);
}
