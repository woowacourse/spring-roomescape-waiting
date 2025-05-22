package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.theme.Theme;

public interface ThemeRepository extends ListCrudRepository<Theme, Long> {

    @Query("""
            SELECT t
            FROM Theme t
            LEFT JOIN Reservation r ON r.theme = t
            WHERE r.reservationDate.date IS NOT NULL
              AND r.reservationDate.date >= :startDate
              AND r.reservationDate.date < :endDate
            GROUP BY t
            ORDER BY COUNT(r.id) DESC
            """)
    List<Theme> findPopularThemeDuringAWeek(
            @Param("startDate") final LocalDate startDate,
            @Param("endDate") final LocalDate endDate,
            final Pageable pageable
    );
}
