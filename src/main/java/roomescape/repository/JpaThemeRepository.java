package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
            SELECT
            th.id,
            th.name,
            th.description,
            th.thumbnail
            FROM theme as th
            INNER JOIN reservation as r on r.theme_id = th.id
            WHERE r.date >= ? and r.date < ?
            GROUP BY th.id
            ORDER BY COUNT(th.id) DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Theme> findPopular(LocalDate start, LocalDate end);
}
