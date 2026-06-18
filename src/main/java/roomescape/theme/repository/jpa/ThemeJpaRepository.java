package roomescape.theme.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.theme.repository.entity.ThemeEntity;

public interface ThemeJpaRepository extends JpaRepository<ThemeEntity, Long> {

    @Query(nativeQuery = true, value = """
        SELECT t.id, t.name, t.description, t.thumbnail_url
        FROM theme t
        LEFT JOIN reservation r ON r.theme_id = t.id AND r.date >= :startDate AND r.date < :today
        GROUP BY t.id, t.name, t.description, t.thumbnail_url
        ORDER BY COUNT(r.id) DESC, t.name ASC
        LIMIT 10
        """)
    List<ThemeEntity> findPopularThemes(@Param("startDate") LocalDate startDate, @Param("today") LocalDate today);
}
