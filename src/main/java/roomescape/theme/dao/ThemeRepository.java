package roomescape.theme.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import roomescape.theme.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    @Query(value = """
            SELECT th.*
            FROM theme th
            INNER JOIN (
                SELECT theme_id
                FROM reservation
                WHERE date BETWEEN :startDate AND :endDate
                GROUP BY theme_id
                ORDER BY COUNT(theme_id) DESC
            ) r ON th.id = r.theme_id
            """, nativeQuery = true)
    List<Theme> findThemesByReservationDateOrderByReservationCountDesc(@Param("startDate") LocalDate startDate,
                                                                       @Param("endDate") LocalDate endDate);
}
