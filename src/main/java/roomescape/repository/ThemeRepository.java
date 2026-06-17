package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
        SELECT t.id, t.name, t.description, t.thumbnail_image_url
        FROM reservation r
        JOIN slot s ON r.slot_id = s.id
        JOIN theme t ON s.theme_id = t.id
        WHERE s.date BETWEEN :start AND :end
        GROUP BY t.id, t.name, t.description, t.thumbnail_image_url
        ORDER BY COUNT(*) DESC
        LIMIT 10
        """, nativeQuery = true)
    List<Theme> getPopularTop10Themes(
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );
}
