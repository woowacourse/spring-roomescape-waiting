package roomescape.theme.repository;

import org.springframework.data.domain.Pageable;
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

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.theme.id = :themeId")
    boolean existsReservationByThemeId(@Param("themeId") Long themeId);

    @Query("""
            SELECT r.theme.id
            FROM Reservation r
            WHERE r.date BETWEEN :startDate AND :endDate
            GROUP BY r.theme.id
            ORDER BY COUNT(r.id) DESC
            """)
    List<Long> findTopThemeIds(@Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate,
                               Pageable pageable);
}
