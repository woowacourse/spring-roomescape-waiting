package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
            select th.id, th.name, th.content, th.url
            from theme th
            join reservation r on th.id = r.theme_id
            join reservation_date rd on r.date_id = rd.id
            where rd.play_day between :startDay and :endDay
            group by th.id
            order by count(r.id) desc, th.id asc
            limit :rankLimit
            """, nativeQuery = true)
    List<Theme> findPopularThemes(
            @Param("rankLimit") int rankLimit,
            @Param("startDay") LocalDate startDay,
            @Param("endDay") LocalDate endDay
    );
}
