package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    @Query(value = """
            SELECT
            new
            Theme (th.id,
            th.name,
            th.description,
            th.thumbnail)
            FROM Reservation r
            JOIN r.theme th
            WHERE r.date >= :start and r.date < :end
            GROUP BY th.id
            ORDER BY COUNT(r) DESC
            LIMIT 10
            """)
    List<Theme> findPopular(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
