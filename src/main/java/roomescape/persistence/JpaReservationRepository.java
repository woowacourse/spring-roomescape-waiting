package roomescape.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Reservation;

import java.time.LocalDate;
import java.util.List;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
        SELECT r
        FROM Reservation r
        JOIN FETCH r.time t
        JOIN FETCH r.theme tm
        JOIN FETCH r.member m
        WHERE (:memberId IS NULL OR r.member.id = :memberId)
        AND (:themeId IS NULL OR r.theme.id = :themeId)
        AND (:dateFrom IS NULL OR r.date >= :dateFrom)
        AND (:dateTo IS NULL OR r.date <= :dateTo)
        ORDER BY r.id
    """)
    List<Reservation> findReservationsInConditions(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);

    @Query("""
        SELECT r
        FROM Reservation r
        JOIN FETCH r.time
        JOIN FETCH r.theme
        JOIN FETCH r.member
        WHERE r.member.id = :memberId
    """)
    List<Reservation> findByMemberIdWithDetails(Long memberId);

    boolean existsByTimeId(Long reservationTimeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate reservationDate, Long timeId, Long themeId);

    boolean existsByThemeId(Long themeId);

    @Query("""
        SELECT COUNT(r)
        FROM Reservation r
        WHERE r.date = :date
        AND r.time.id = :timeId
        AND r.theme.id = :themeId
        AND r.status = 'WAITING'
    """)
    int countWaitingReservations(LocalDate date, Long timeId, Long themeId);
}
