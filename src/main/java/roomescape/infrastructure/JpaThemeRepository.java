package roomescape.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;

@Repository
public interface JpaThemeRepository extends JpaRepository<Theme, Long> {
    Optional<Theme> findByName(String name);
}
