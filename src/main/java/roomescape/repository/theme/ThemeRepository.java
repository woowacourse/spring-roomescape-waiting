package roomescape.repository.theme;

import java.util.List;
import java.util.Optional;
import roomescape.domain.theme.Theme;

public interface ThemeRepository {

    long save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(long id);

    void deleteById(long id);
}
