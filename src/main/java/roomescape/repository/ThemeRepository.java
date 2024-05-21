package roomescape.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemePopularFilter;

import java.util.List;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
            SELECT th
            FROM Theme AS th
            LEFT OUTER JOIN Reservation AS r
            ON r.theme.id = th.id  AND r.date BETWEEN :#{#filter.startDate} AND :#{#filter.endDate}
            GROUP BY th.id ORDER BY COUNT(r.id) DESC
            """)
    List<Theme> findPopularThemesBy(@Param(value = "filter") final ThemePopularFilter filter, Pageable pageable);
}
