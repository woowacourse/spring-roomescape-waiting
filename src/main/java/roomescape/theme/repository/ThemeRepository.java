package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.projection.PopularThemeResult;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    List<Theme> findAllByIsActiveOrderByNameAsc(boolean isActive);

    @Query("""
            SELECT new roomescape.theme.repository.projection.PopularThemeResult(
                t.id,
                t.name,
                t.description,
                t.thumbnailUrl,
                t.isActive,
                COUNT(r)
            )
            FROM reservation r
            JOIN r.theme t
            WHERE t.isActive = true
              AND r.status = roomescape.reservation.domain.ReservationStatus.RESERVED
              AND r.date.date >= :startDate
              AND r.date.date <= :endDate
            GROUP BY t.id, t.name, t.description, t.thumbnailUrl, t.isActive
            ORDER BY COUNT(r) DESC
            LIMIT :limit
        """)
    List<PopularThemeResult> findPopularThemes(@Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("limit") int limit);
//
//    Optional<Theme> findById(Long id);
//
//    List<Theme> findAll();
//
//    List<Theme> findByIsActive(boolean isActive);
//
//    List<PopularThemeResult> findPopularThemes(LocalDate startDate, LocalDate endDate, int limit);
//
//    Theme save(Theme theme);
//
//    boolean updateStatus(Theme theme);

}
