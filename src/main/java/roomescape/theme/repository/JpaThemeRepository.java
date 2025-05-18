package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.theme.domain.Theme;

public interface JpaThemeRepository extends ListCrudRepository<Theme, Long> {

    @Query("""
            SELECT th
            FROM Theme th
            LEFT JOIN Reservation r
              ON th.id = r.theme.id
             AND r.date BETWEEN ?1 AND ?2
            GROUP BY th.id
            ORDER BY COUNT(r.id) DESC, th.name ASC
            LIMIT 10
            """)
    List<Theme> findTop10PopularThemesWithinLastWeek(LocalDate fromDate, LocalDate toDate);
}
