package roomescape.infrastructure.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
        SELECT t
        FROM Theme t
        JOIN Reservation r ON r.theme = t
        WHERE r.date BETWEEN :dateFrom AND :dateTo
        GROUP BY t
        ORDER BY COUNT(r) DESC, MAX(r.date) DESC
    """)
    List<Theme> findRecentPopularThemes(LocalDate dateFrom, LocalDate dateTo, Pageable pageable);
}
