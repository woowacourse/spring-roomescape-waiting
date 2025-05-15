package roomescape.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {
    Optional<Theme> findByName(String name);

}
