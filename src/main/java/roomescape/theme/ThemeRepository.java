package roomescape.theme;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository {

    Long save(Theme theme);

    Theme findById(Long id);
    List<Theme> findAll();
    List<Theme> findAllOrderByRank(LocalDate from, LocalDate to, int size);

    void deleteById(Long id);

    Boolean existsById(Long id);
}
