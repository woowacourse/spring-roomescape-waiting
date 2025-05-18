package roomescape.reservation.infrastructure.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.theme.ThemeName;

public interface ThemeJpaRepository extends JpaRepository<Theme, Long> {

    boolean existsByThemeName(ThemeName themeName);

    @Query("""
            SELECT r.theme, t.themeName, t.themeDescription, t.themeThumbnail, COUNT(r.id) AS cnt
            FROM Reservation r
            JOIN Theme t ON t = r.theme
            WHERE r.date BETWEEN :from AND :to
            GROUP BY r.theme
            ORDER BY cnt DESC
            LIMIT :count
            """)
    List<Theme> findPopularThemes(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("count") int count);
}
