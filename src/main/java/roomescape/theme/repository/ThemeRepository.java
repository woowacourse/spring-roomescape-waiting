package roomescape.theme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Override
    @Query("SELECT t FROM Theme t ORDER BY t.id ASC")
    List<Theme> findAll();

    @Query("SELECT t FROM Theme t WHERE t.id IN :ids ORDER BY t.id ASC")
    List<Theme> findAllByIds(@Param("ids") List<Long> ids);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM reservation WHERE theme_id = :themeId)", nativeQuery = true)
    boolean existsReservationByThemeId(@Param("themeId") Long themeId);

    @Query(value = """
            SELECT r.theme_id
            FROM reservation r
            WHERE r.date BETWEEN :startDate AND :endDate
            GROUP BY r.theme_id
            ORDER BY COUNT(r.id) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Long> findTopThemeIds(@Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate,
                               @Param("limit") int limit);
}
