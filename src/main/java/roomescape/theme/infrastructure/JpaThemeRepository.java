package roomescape.theme.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.theme.domain.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    boolean existsByName(String name);

    @Query("""
                SELECT t
                FROM Reservation r
                JOIN r.theme t
                WHERE r.date BETWEEN :from AND :to
                GROUP BY t
                ORDER BY COUNT(r.id) DESC
            """)
    List<Theme> findTopNThemesByReservationCountInDateRange(
            final @Param("from") LocalDate dateFrom,
            final @Param("to") LocalDate dateTo,
            final Pageable pageable);
}
