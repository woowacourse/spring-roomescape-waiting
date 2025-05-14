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
            count(*) as count,
            t.id as id,
            t.name as name,
            t.description as description,
            t.thumbnail as thumbnail
        FROM theme as t
        LEFT JOIN reservation as r ON t.id = r.theme_id
        WHERE r.date >= :start_date AND r.date <= :end_date
        GROUP BY id, name, description, thumbnail
        ORDER BY count DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Theme> findPopularThemes(@Param("start_date") LocalDate start, @Param("end_date") LocalDate end, @Param("limit") int popularCount);

    List<Theme> findAll();
}
