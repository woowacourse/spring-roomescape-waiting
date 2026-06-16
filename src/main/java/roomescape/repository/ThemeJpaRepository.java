package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.Theme;

@Repository
public interface ThemeJpaRepository extends JpaRepository<Theme, Long> {
}
