package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.theme.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {
    @Query("""
            SELECT r.time.id
            FROM Reservation r
            WHERE r.date = :date AND r.theme.id = :themeId
            """)
    List<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    List<Reservation> findByMemberId(Long id);

    @Query("""
            SELECT t
            FROM Reservation r
            LEFT JOIN Theme t ON t.id=r.theme.id
            WHERE r.date > :startDate AND r.date < :endDate
            GROUP BY t.id
            ORDER BY COUNT(*) DESC
            LIMIT 10
            """)
    List<Theme> findThemeByMostPopularReservation(LocalDate startDate, LocalDate endDate);
}
