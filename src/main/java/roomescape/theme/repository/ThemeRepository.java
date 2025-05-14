package roomescape.theme.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    List<Theme> findAll();

    Optional<Theme> findById(Long id);

    Theme save(Theme theme);

    void deleteById(Long id);
}
