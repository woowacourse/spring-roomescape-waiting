package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Theme;

public interface ThemeDao extends JpaRepository<Theme, Long> {
}
