package roomescape.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Duration;
import roomescape.domain.Theme;
import roomescape.domain.Themes;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
}
