package roomescape.repository;

import static roomescape.domain.exception.DomainErrorCode.THEME_NOT_FOUND;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;
import roomescape.domain.exception.RoomEscapeException;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
            SELECT
            t.id, t.name, t.description, t.thumbnail_image_url
            FROM theme as t
            JOIN reservation as r
            ON r.theme_id = t.id
            WHERE r.date BETWEEN :start AND :end
            GROUP BY t.id, t.name, t.description, t.thumbnail_image_url
            ORDER BY COUNT(*) DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Theme> getPopularTop10Themes(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    default Theme getById(Long id, String message) {
        return findById(id).orElseThrow(() -> new RoomEscapeException(THEME_NOT_FOUND, message));
    }
}
