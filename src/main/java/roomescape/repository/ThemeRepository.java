package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemePopularFilter;

import java.util.List;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(nativeQuery = true,
            value = """
                    SELECT th.id AS id, th.name AS name, th.description AS description, th.thumbnail AS thumbnail
                    FROM theme th
                    LEFT OUTER JOIN reservation r
                    ON r.theme_id = th.id  AND r.date BETWEEN :#{#filter.startDate} AND :#{#filter.endDate}
                    GROUP BY th.id
                    ORDER BY COUNT(r.id) DESC, th.id
                    LIMIT :#{#filter.limit}
                    """)
    List<Theme> findPopularThemesBy(@Param(value = "filter") final ThemePopularFilter filter);
}
