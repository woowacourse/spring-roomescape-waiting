package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;

public interface ReservationDao extends JpaRepository<Reservation, Long> {

    List<Reservation> findByThemeAndDate(Theme theme, LocalDate date);

    List<Reservation> findAllByMemberId(long userId);

    List<Reservation> findByDateBetween(LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findByThemeIdAndDateBetween(Long themeId, LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findByMemberIdAndDateBetween(Long memberId, LocalDate dateFrom, LocalDate dateTo);

    List<Reservation> findByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId, LocalDate dateFrom,
                                                             LocalDate dateTo);

    @Query("""
            SELECT r.theme, COUNT(r) AS themeCount
            FROM Reservation r
            WHERE r.date BETWEEN :startDate AND :endDate
            GROUP BY r.theme
            ORDER BY themeCount DESC
            LIMIT :limit""")
    List<Theme> findAndOrderByPopularity(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
                                         @Param("limit") int limit);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);
}
