package roomescape.repository.theme;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.theme.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

}
