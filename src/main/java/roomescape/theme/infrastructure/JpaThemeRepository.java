package roomescape.theme.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.theme.domain.Theme;

public interface JpaThemeRepository extends CrudRepository<Theme, Long> {

    @Query(value = """
        SELECT
            t
        FROM Theme t
        LEFT JOIN Reservation r ON r.theme = t
        WHERE r.date >= :start_date AND r.date <= :end_date
        GROUP BY t.id, t.name, t.description, t.thumbnail
        ORDER BY COUNT(r) DESC
    """)
    List<Theme> findPopularThemes(@Param("start_date") LocalDate start, @Param("end_date") LocalDate end);

    List<Theme> findAll();
}
