package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository {

    Theme save(Theme theme);

    List<Theme> findAll();

    List<Theme> findPopularThemesDateBetween(LocalDate startDate, LocalDate endDate, int limitCount);

    Theme getById(long id);

    void deleteById(long id);
}
