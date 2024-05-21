package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.reservation.Theme;

import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    @Query(value = """
            SELECT
                t.id AS id,
                t.name AS name,
                t.description AS description,
                t.thumbnail AS thumbnail
            FROM Theme AS t
            INNER JOIN Reservation AS r ON r.theme_id = t.id
            WHERE r.date BETWEEN ? AND ?
            GROUP BY t.id
            ORDER BY COUNT(r.id) DESC
            LIMIT ?
            """
            , nativeQuery = true)
    List<Theme> getPopularTheme(final String startDate, final String endDate, int count);


}
