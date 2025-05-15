package roomescape.theme.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.theme.domain.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long>, ThemeCustomRepository {
}
