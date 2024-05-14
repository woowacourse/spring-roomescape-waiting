package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {
}
