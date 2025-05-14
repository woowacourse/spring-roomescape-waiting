package roomescape.theme.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.theme.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
}
