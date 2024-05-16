package roomescape.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface ThemeRepository extends Repository<Theme, Long> {

    Theme save(Theme theme);

    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    void deleteById(Long id);
}
