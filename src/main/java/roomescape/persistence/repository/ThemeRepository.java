package roomescape.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.business.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    boolean existsByName(final String name);
}
