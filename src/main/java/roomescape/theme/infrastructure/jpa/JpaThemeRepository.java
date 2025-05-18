package roomescape.theme.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.theme.domain.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long>, ThemeCustomRepository {
}
