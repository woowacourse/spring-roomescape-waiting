package roomescape.persistence;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Reservation;

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
    List<Reservation> findByMemberId(Long memberId);

    @Query("""
        SELECT CASE WHEN EXISTS (
            SELECT 1
            FROM Reservation r
            WHERE r.theme.id = :themeId
              AND r.time.id = :timeId
              AND r.date = :date
        ) THEN false ELSE true END
    """)
    boolean isReservationSlotEmpty(LocalDate date, Long timeId, Long themeId);

    boolean existsByTimeId(Long reservationTimeId);

    boolean existsByThemeId(Long themeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate reservationDate, Long timeId, Long themeId);

    boolean existsByMemberIdAndThemeIdAndTimeIdAndDate(Long memberId, Long themeId, Long timeId, LocalDate date);
}
