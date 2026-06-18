package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
            SELECT t
            FROM Reservation r
            JOIN r.theme t
            WHERE r.date BETWEEN :start AND :end
            GROUP BY t
            ORDER BY COUNT(r) DESC
            """)
    List<Theme> findTopThemes(LocalDate start, LocalDate end, Pageable pageable);

    Theme findThemeById(Long id);
}
