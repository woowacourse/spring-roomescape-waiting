package roomescape.domain.repository;

import org.springframework.data.repository.Repository;
import roomescape.domain.Theme;

public interface ThemeCommandRepository extends Repository<Theme, Long> {

    Theme save(Theme theme);

    void delete(Theme theme);
}
