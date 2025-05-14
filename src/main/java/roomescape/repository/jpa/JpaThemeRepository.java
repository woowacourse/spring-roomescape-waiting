package roomescape.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.entity.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    @Query(
        value = """
                SELECT
                  t.id,
                  t.name,
                  t.description,
                  t.thumbnail
                FROM
                  reservation as r
                  INNER JOIN theme as t
                  ON r.theme_id = t.id
                WHERE
                  r.date >= :startDate
                  AND r.date < :endDate
                GROUP BY
                  theme_id
                ORDER BY
                  COUNT(theme_id) DESC
                LIMIT 10;
            """, nativeQuery = true)
    List<Theme> findTop10ByDateBetween(@Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    boolean existsByName(String name);
}
