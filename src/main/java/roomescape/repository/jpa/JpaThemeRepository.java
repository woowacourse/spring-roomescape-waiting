package roomescape.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {
}
