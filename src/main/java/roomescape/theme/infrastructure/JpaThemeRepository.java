package roomescape.theme.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.theme.domain.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    boolean existsByName(String name);

    @Query(value = """
                SELECT
                    t.id AS id,
                    t.name AS name,
                    t.description AS description,
                    t.thumbnail AS thumbnail,
                    COUNT(r.id) AS reservation_count
                FROM themes t
                LEFT JOIN reservations r 
                    ON t.id = r.theme_id AND r.date BETWEEN ? AND ?
                GROUP BY t.id, t.name, t.description, t.thumbnail
                ORDER BY reservation_count DESC
                LIMIT ?;
                """, nativeQuery = true)
    List<Theme> findTopNThemesByReservationCountInDateRange(
            final LocalDate dateFrom,
            final LocalDate dateTo,
            final int limit
    );

}
