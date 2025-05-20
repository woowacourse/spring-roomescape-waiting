package roomescape.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.domain.Theme;

public interface ThemeRepository extends Repository<Theme, Long> {
    Theme save(Theme theme);

    List<Theme> findAll();

    void deleteById(long id);

    Optional<Theme> findByName(String name);

    Optional<Theme> findById(Long id);
}