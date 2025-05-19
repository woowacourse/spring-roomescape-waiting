package roomescape.theme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.theme.domain.Theme;

public interface ThemeJpaRepository extends JpaRepository<Theme, Long>, ThemeRepository {

}
