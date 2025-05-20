package roomescape.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
            SELECT t
            FROM Theme t
            JOIN Reservation r ON r.theme = t
            WHERE r.date >= :startDate and r.date < :endDate
            GROUP BY t
            ORDER BY COUNT(t.id) DESC
            """)
    List<Theme> findPopular(
            @Param("startDate") LocalDate start,
            @Param("endDate") LocalDate end,
            Pageable pageable);
}
