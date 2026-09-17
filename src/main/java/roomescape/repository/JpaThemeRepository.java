package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long>, ThemeRepository {

    @Override
    @Query(value = """
            SELECT
                t.id,
                t.name,
                t.description,
                t.thumbnail_url
            FROM (
                SELECT ts.theme_id
                FROM reservation r
                    INNER JOIN theme_slot ts ON r.theme_slot_id = ts.id
                WHERE ts.date BETWEEN :fromDate AND :toDate
                AND r.status IN ('CONFIRMED', 'COMPLETED')
            ) AS r
            INNER JOIN theme t
            ON r.theme_id = t.id
            GROUP BY t.id, t.name, t.description, t.thumbnail_url
            ORDER BY COUNT(*) DESC
            LIMIT :topCount
            """, nativeQuery = true)
    List<Theme> findPopularThemes(
            @Param("topCount") Long topCount,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
