package roomescape.dao;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
            SELECT th.id, th.name, th.description, th.thumbnail_url
            FROM reservation AS r
            INNER JOIN theme AS th ON r.theme_id = th.id
            WHERE r.date BETWEEN :from AND :to
            GROUP BY th.id, th.name, th.description, th.thumbnail_url
            ORDER BY COUNT(r.id) DESC
            LIMIT :size
            """, nativeQuery = true)
    List<Theme> findPopularThemes(@Param("size") int size, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query(value = """
            SELECT rt.id, rt.start_at,
                   CASE WHEN r.id IS NULL THEN TRUE ELSE FALSE END AS available
            FROM reservation_time rt
            LEFT JOIN reservation r
                ON rt.id = r.time_id
                AND r.theme_id = :themeId
                AND r.date = :date
                AND r.status = 'CONFIRMED'
            """, nativeQuery = true)
    List<AvailableTimeRow> findAvailableTimesForTheme(@Param("themeId") long themeId, @Param("date") LocalDate date);
}
