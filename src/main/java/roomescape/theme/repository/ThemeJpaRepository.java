package roomescape.theme.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.theme.domain.Name;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;

public interface ThemeJpaRepository extends JpaRepository<Theme, Long> {

    boolean existsByName(Name name);

    @Query(value = """
            SELECT r.theme
            FROM Reservation r
            WHERE r.date BETWEEN :startDate AND :endDate
            GROUP BY r.theme
            ORDER BY COUNT(r.theme) DESC
            """)
    List<Theme> findTrendingThemesBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);
}
