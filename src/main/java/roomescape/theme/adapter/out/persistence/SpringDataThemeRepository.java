package roomescape.theme.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.theme.domain.Theme;

interface SpringDataThemeRepository extends JpaRepository<Theme, Long> {
    boolean existsByName(String name);

    @Query("""
            SELECT DISTINCT t
            FROM Slot s
            JOIN s.theme t
            WHERE s.date = :date
            ORDER BY t.id ASC
            """)
    List<Theme> findThemesBySlotDate(@Param("date") LocalDate date);

    @Query("""
            SELECT t
            FROM Reservation r
            JOIN r.slot s
            JOIN s.theme t
            WHERE s.date >= :startDate
              AND s.date < :currentDate
            GROUP BY t
            ORDER BY COUNT(r.id) DESC, t.id ASC
            """)
    List<Theme> findPopularThemeByCurrentDate(
            @Param("startDate") LocalDate startDate,
            @Param("currentDate") LocalDate currentDate,
            Pageable pageable
    );
}
