package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
            SELECT th, count(r)
            FROM Theme as th
            LEFT JOIN Reservation as r on th = r.theme AND r.date BETWEEN :startDate and :endDate
            GROUP BY th
            ORDER BY COUNT(r) desc
            LIMIT :count
            """)
    List<Theme> findThemeRanking(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("count") int count
    );
}
