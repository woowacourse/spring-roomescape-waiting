package roomescape.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    Theme findThemeById(Long id);

    List<Theme> findAllThemes();

    Theme save();

    void deleteById(Long id);
}
