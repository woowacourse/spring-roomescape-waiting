package roomescape.theme.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.theme.domain.Theme;

public interface ThemeJpaRepository extends JpaRepository<Theme, Long> {
    boolean existsByName(String name);
}
