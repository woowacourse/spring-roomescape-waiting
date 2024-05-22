package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    boolean existsByName(String name);
}
