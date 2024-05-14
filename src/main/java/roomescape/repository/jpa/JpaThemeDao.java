package roomescape.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Theme;

public interface JpaThemeDao extends JpaRepository<Theme, Long> {
    @Query(nativeQuery = true, value = """
            SELECT
                th.id AS id, th.name AS name, description, thumbnail, COUNT(*) AS count
            FROM theme th
            JOIN reservation r
                ON r.theme_id = th.id
            WHERE
                PARSEDATETIME(r.date,'yyyy-MM-dd') >= PARSEDATETIME(:start,'yyyy-MM-dd')
            AND
                PARSEDATETIME(r.date,'yyyy-MM-dd') <= PARSEDATETIME(:end,'yyyy-MM-dd')
            GROUP BY th.id
            ORDER BY count DESC
            LIMIT :count
            """)
    List<Theme> findAndOrderByPopularity(LocalDate start, LocalDate end, int count);
}
