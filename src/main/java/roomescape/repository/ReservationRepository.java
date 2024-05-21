package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.reservation.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"member", "theme", "time"})
    Optional<Reservation> findById(Long id);

    @EntityGraph(attributePaths = {"member", "theme", "time"})
    List<Reservation> findAll();

    List<Reservation> findByMemberId(Long memberId);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    @Query("""
            SELECT r
            FROM Reservation r
            JOIN FETCH r.member
            JOIN FETCH r.time
            JOIN FETCH r.theme
            WHERE (:memberId IS NULL OR r.member.id = :memberId)
            AND (:themeId IS NULL OR r.theme.id = :themeId)
            AND (:startDate IS NULL OR r.date >= :startDate)
            AND (:endDate IS NULL OR r.date <= :endDate)
            """)
    List<Reservation> findByMemberOrThemeOrDateRange(Long memberId, Long themeId, LocalDate startDate, LocalDate endDate);

    @Query("""
              SELECT r.theme.id
              FROM Reservation r
              WHERE r.date BETWEEN :startDate AND :endDate
              GROUP BY r.theme.id
              ORDER BY count(*) DESC
              LIMIT 10
            """)
    List<Long> findTopThemeIdsByReservationCountsForDate(LocalDate startDate, LocalDate endDate);

    boolean existsByTimeId(Long id);

    boolean existsByThemeId(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
