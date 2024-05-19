package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import roomescape.theme.domain.Theme;

public interface ThemeRepository extends CrudRepository<Theme, Long> {

    Theme save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(long id);

    @Query("SELECT r.theme " +
            "FROM Reservation r " +
            "WHERE r.date between :startDate AND :endDate " +
            "GROUP BY r.theme.id " +
            "ORDER BY COUNT(r.theme.id) DESC " +
            "LIMIT :limitCount "
    )
    List<Theme> findLimitedAllByDateOrderByThemeIdCount(LocalDate startDate, LocalDate endDate, int limitCount);

    void deleteById(long themeId);
}
