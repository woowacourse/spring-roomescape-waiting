package roomescape.theme.domain.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import roomescape.theme.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @NativeQuery(value = """
            SELECT
                t.id AS theme_id,
                t.name AS theme_name,
                t.description,
                t.thumbnail
            FROM
                reservation r
            JOIN
                theme t ON r.theme_id = t.id
            WHERE
                r.date BETWEEN DATEADD('DAY', -7, CURRENT_DATE) AND DATEADD('DAY', -1, CURRENT_DATE)
            GROUP BY
                t.id, t.name, t.description, t.thumbnail
            ORDER BY
                COUNT(r.id) DESC
            LIMIT 10
            """)
    List<Theme> findByRank();

    boolean existsByName(String name);
}
