package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.theme.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
            SELECT t FROM Theme t
            JOIN Reservation r ON r.theme.id = t.id
            WHERE r.date BETWEEN :startDate AND :endDate
            GROUP BY t
            ORDER BY COUNT(r) DESC
            LIMIT 10
            """)
    List<Theme> findAllPopular(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
