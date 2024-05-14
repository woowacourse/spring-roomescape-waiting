package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface ThemeRepository {

    Theme save(Theme theme);

    List<Theme> findAll();

    default Theme getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 id의 테마가 존재하지 않습니다."));
    }

    Optional<Theme> findById(Long id);

    List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate, int limit);

    void deleteById(Long id);

    boolean existsById(Long id);

    boolean existsByName(String name);
}
