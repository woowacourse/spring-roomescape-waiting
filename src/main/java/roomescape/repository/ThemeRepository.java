package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
            SELECT t
            FROM Theme AS t
            LEFT JOIN Reservation AS r
            ON r.theme.id = t.id AND r.date BETWEEN :from AND :to
            GROUP BY t.id
            ORDER BY COUNT(*) DESC
            LIMIT :limit
            """)
    List<Theme> findThemesOrderByReservationCount(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("limit") int limit);
}
