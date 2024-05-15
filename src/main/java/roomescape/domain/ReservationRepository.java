package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("""
                SELECT r
                FROM  Reservation r
                LEFT JOIN Member m ON m.id=r.theme.id
                LEFT JOIN ReservationTime rt ON rt.id=r.theme.id
                LEFT JOIN Theme t ON t.id=r.theme.id
                WHERE (:memberId IS NULL OR r.member.id = :memberId)
                AND (:themeId IS NULL OR r.theme.id = :themeId)
                AND (:startDate IS NULL OR r.date >= :startDate)
                AND (:endDate IS NULL OR r.date <= :endDate)
            """)
    List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(
            Long memberId, Long themeId, LocalDate startDate, LocalDate endDate);

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
