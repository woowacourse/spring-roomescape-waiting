package roomescape.theme.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.theme.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    boolean existsByName(String name);

    List<Theme> findAll();
}
