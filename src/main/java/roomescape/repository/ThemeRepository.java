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
            SELECT t.*
            FROM theme t
            INNER JOIN reservation r ON t.id = r.theme_id
            WHERE r.date >= :startDate
            AND r.date <  :endDate
            GROUP BY t.id
            ORDER BY COUNT(r.id) DESC
            LIMIT :size
            """, nativeQuery = true)
    List<Theme> findTopReservedThemesSince(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("size") int size
    );
}
