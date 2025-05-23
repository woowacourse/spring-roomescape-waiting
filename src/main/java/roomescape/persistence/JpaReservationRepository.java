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
        JOIN FETCH r.bookingInfo.time t
        JOIN FETCH r.bookingInfo.theme tm
        JOIN FETCH r.bookingInfo.member m
        WHERE (:memberId IS NULL OR r.bookingInfo.member.id = :memberId)
          AND (:themeId IS NULL OR r.bookingInfo.theme.id = :themeId)
          AND (:dateFrom IS NULL OR r.bookingInfo.date >= :dateFrom)
          AND (:dateTo IS NULL OR r.bookingInfo.date <= :dateTo)
        ORDER BY r.id
    """)
    List<Reservation> findReservationsInConditions(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);

    @Query("""
        SELECT r
        FROM Reservation r
        JOIN FETCH r.bookingInfo.time
        JOIN FETCH r.bookingInfo.theme
        JOIN FETCH r.bookingInfo.member
        WHERE r.bookingInfo.member.id = :memberId
    """)
    List<Reservation> findByMemberId(Long memberId);

    @Query("""
        SELECT CASE WHEN EXISTS (
            SELECT 1
            FROM Reservation r
            WHERE r.bookingInfo.theme.id = :themeId
              AND r.bookingInfo.time.id = :timeId
              AND r.bookingInfo.date = :date
        ) THEN false ELSE true END
    """)
    boolean isReservationSlotEmpty(LocalDate date, Long timeId, Long themeId);

    @Query("""
    SELECT EXISTS (
        SELECT 1 FROM Reservation r
        WHERE r.bookingInfo.time.id = :reservationTimeId
      )
    """)
    boolean existsByTimeId(Long reservationTimeId);

    @Query("""
    SELECT EXISTS (
        SELECT 1 FROM Reservation r
        WHERE r.bookingInfo.theme.id = :themeId
      )
    """)
    boolean existsByThemeId(Long themeId);

    @Query("""
    SELECT EXISTS (
        SELECT 1 FROM Reservation r
        WHERE r.bookingInfo.date = :reservationDate
          AND r.bookingInfo.time.id = :timeId
          AND r.bookingInfo.theme.id = :themeId
        )
    """)
    boolean existsByDateAndTimeIdAndThemeId(LocalDate reservationDate, Long timeId, Long themeId);

    @Query("""
    SELECT EXISTS (
        SELECT 1 FROM Reservation r
        WHERE r.bookingInfo.member.id = :memberId
          AND r.bookingInfo.theme.id = :themeId
          AND r.bookingInfo.time.id = :timeId
          AND r.bookingInfo.date = :date
        )
    """)
    boolean existsByMemberIdAndThemeIdAndTimeIdAndDate(Long memberId, Long themeId, Long timeId, LocalDate date);
}
