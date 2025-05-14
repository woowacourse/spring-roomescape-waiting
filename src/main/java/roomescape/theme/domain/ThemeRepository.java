package roomescape.theme.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ThemeRepository {

    Theme save(Theme theme);

    Optional<Theme> findById(Long id);

    void deleteById(Long id);

    List<Theme> findPopularThemes(LocalDate start, LocalDate end, int popularCount);

    List<Theme> findAll();
}
