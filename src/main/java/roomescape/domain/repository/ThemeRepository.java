package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;
import roomescape.dto.AvailableTimeResponse;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    @Query("""
            SELECT rs.theme
            FROM ReservationSlot rs
            WHERE rs.date BETWEEN :from AND :to
            GROUP BY rs.theme
            ORDER BY COUNT(rs.id) DESC
            """)
    List<Theme> findPopularThemes(LocalDate from, LocalDate to, Pageable pageable);

    @Query("""
            SELECT new roomescape.dto.AvailableTimeResponse(
                t.id,
                t.startAt,
                CASE WHEN COUNT(r.id) = 0 THEN true ELSE false END,
                CASE WHEN COUNT(r.id) <= 1 THEN 0L ELSE COUNT(r.id) - 1 END
            )
            FROM Time t
            LEFT JOIN ReservationSlot rs
                ON rs.time = t
                AND rs.theme.id = :themeId
                AND rs.date = :date
            LEFT JOIN rs.reservations r
                ON r.status <> roomescape.domain.Status.CANCELED
            GROUP BY t.id, t.startAt
            ORDER BY t.startAt
            """)
    List<AvailableTimeResponse> findAvailableTimeById(
            @Param("themeId") long themeId,
            @Param("date") LocalDate date
    );
}
