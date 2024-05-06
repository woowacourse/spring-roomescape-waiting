package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface ThemeRepository {

    Theme create(Theme theme);

    Optional<Theme> findById(long id);

    List<Theme> findAll();

    void deleteById(long id);

    List<Theme> findPopularThemesDateBetween(LocalDate startDate, LocalDate endDate, int limit);

    boolean existsByTimeId(long id);

    default Theme getById(long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 테마입니다."));
    }
}
