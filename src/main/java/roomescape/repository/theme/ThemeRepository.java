package roomescape.repository.theme;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;
import roomescape.domain.theme.Theme;

@org.springframework.stereotype.Repository
public interface ThemeRepository extends Repository<Theme, Long> {

    Theme save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(long id);

    void deleteById(long id);
}
