package roomescape.infrastructure.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    List<Theme> findAll();

    Theme save(Theme theme);

    void deleteById(Long id);

    Optional<Theme> findById(Long id);

    @Query("""
        SELECT t
        FROM Theme t
        JOIN Reservation r ON r.theme = t
        WHERE r.date BETWEEN :dateFrom AND :dateTo
        GROUP BY t
        ORDER BY COUNT(r) DESC
    """)
    List<Theme> findRecentPopularThemes(LocalDate dateFrom, LocalDate dateTo, Pageable pageable);
}
