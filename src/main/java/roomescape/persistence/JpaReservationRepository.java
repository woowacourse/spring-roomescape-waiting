package roomescape.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Reservation;
import roomescape.service.result.WaitingWithRank;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
        SELECT r
        FROM Reservation r
        JOIN FETCH r.time t
        JOIN FETCH r.theme tm
        JOIN FETCH r.member m
        WHERE r.status = 'RESERVED'
        AND (:memberId IS NULL OR r.member.id = :memberId)
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

    @Query("""
        SELECT new roomescape.service.result.WaitingWithRank(
        r.id,
        r.status,
        (SELECT COUNT(r2) FROM Reservation r2
          WHERE r2.status = 'WAITING'
            AND r2.theme = r.theme
            AND r2.date = r.date
            AND r2.time = r.time
            AND r2.id < r.id)
     )
        FROM Reservation r
        WHERE r.status = 'WAITING'
          AND r.member.id = :memberId
    """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    boolean existsByTimeId(Long reservationTimeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate reservationDate, Long timeId, Long themeId);

    boolean existsByThemeId(Long themeId);

    @Query("""
        SELECT r
        FROM Reservation r
        JOIN FETCH r.time
        JOIN FETCH r.theme
        JOIN FETCH r.member
        WHERE r.status = 'WAITING'
    """)
    List<Reservation> findWaitingReservations();

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.status = 'WAITING'
          AND r.theme.id = :themeId
          AND r.time.id = :timeId
          AND r.date = :date
        ORDER BY r.id ASC
        LIMIT 1
    """)
    Optional<Reservation> findFirstWaiting(LocalDate date, Long themeId, Long timeId);

    @Query("""
        SELECT CASE WHEN EXISTS (
            SELECT 1
            FROM Reservation r
            WHERE r.status = 'RESERVED'
              AND r.theme.id = :themeId
              AND r.time.id = :timeId
              AND r.date = :date
        ) THEN false ELSE true END
    """)
    boolean isReservationSlotEmpty(LocalDate date, Long timeId, Long themeId);

    boolean existsByMemberIdAndThemeIdAndTimeIdAndDate(Long memberId, Long themeId, Long timeId, LocalDate date);
}
