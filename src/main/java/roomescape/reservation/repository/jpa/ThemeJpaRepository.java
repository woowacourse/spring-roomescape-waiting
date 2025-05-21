package roomescape.reservation.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThemeJpaRepository extends JpaRepository<Theme, Long> {

    boolean existsByName(ThemeName name);

    @Query("""
        SELECT r.theme, t.name, t.thumbnail, t.description, COUNT(r.id) AS cnt
        FROM Reservation r
        JOIN Theme t ON t = r.theme
        WHERE r.date BETWEEN :from AND :to
        GROUP BY r.theme
        ORDER BY cnt DESC
        LIMIT :count
        """)
    List<Theme> findPopularThemes(@Param("from") LocalDate from, @Param("to") LocalDate to, @Param("count") int count);
}
