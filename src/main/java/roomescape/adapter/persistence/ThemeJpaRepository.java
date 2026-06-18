package roomescape.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Theme;

public interface ThemeJpaRepository extends JpaRepository<Theme, Long> {
}
