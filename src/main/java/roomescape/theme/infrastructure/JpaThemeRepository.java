package roomescape.theme.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeName;

@Repository
public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    boolean existsByName(ThemeName name);
}
