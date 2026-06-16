package roomescape.repository.theme.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThemeJpaRepository extends JpaRepository<ThemeJpaEntity, Long> {

    boolean existsByName(String name);

    @Query(value = """
            SELECT t.*
            FROM reservation r
            INNER JOIN reservation_slot s ON r.slot_id = s.id
            INNER JOIN theme t ON s.theme_id = t.id
            WHERE s.date >= :startDate AND s.date <= :endDate
            GROUP BY t.id, t.name, t.description, t.thumbnail_url
            ORDER BY COUNT(*) DESC, t.id ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<ThemeJpaEntity> findPopularThemes(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("limit") int limit
    );
}
