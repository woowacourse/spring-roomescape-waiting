package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.model.Theme;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    boolean existsByName(String name);

    @Query(value = """
            SELECT t.id AS id,
            t.name AS name,
            t.description AS description,
            t.thumbnail AS thumbnail,
            COUNT(r.id) AS reservation_count
            FROM Theme as t
            INNER JOIN Reservation as r ON t.id = r.theme.id
            WHERE r.date < :startDate
            AND r.date >= :endDate
            GROUP BY t.id
            ORDER BY reservation_count DESC
            LIMIT :size
            """)
    List<Theme> findTopReservedThemesSince(
            @Param("startDate") LocalDate startDate,
            @Param("startDate") LocalDate endDate,
            @Param("size") int size);
}
