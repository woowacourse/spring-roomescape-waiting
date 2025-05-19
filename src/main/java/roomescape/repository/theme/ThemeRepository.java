package roomescape.repository.theme;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme,Long> {
}
