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
        JOIN FETCH r.bookingSlot.time t
        JOIN FETCH r.bookingSlot.theme tm
        JOIN FETCH r.member m
        WHERE (:memberId IS NULL OR r.member.id = :memberId)
          AND (:themeId IS NULL OR r.bookingSlot.theme.id = :themeId)
          AND (:dateFrom IS NULL OR r.bookingSlot.date >= :dateFrom)
          AND (:dateTo IS NULL OR r.bookingSlot.date <= :dateTo)
        ORDER BY r.id
    """)
    List<Reservation> findReservationsInConditions(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);

    @Query("""
        SELECT r
        FROM Reservation r
        JOIN FETCH r.bookingSlot.time
        JOIN FETCH r.bookingSlot.theme
        JOIN FETCH r.member
        WHERE r.member.id = :memberId
    """)
    List<Reservation> findByMemberId(Long memberId);

    @Query("""
        SELECT CASE WHEN EXISTS (
            SELECT 1
            FROM Reservation r
            WHERE r.bookingSlot.theme.id = :themeId
              AND r.bookingSlot.time.id = :timeId
              AND r.bookingSlot.date = :date
        ) THEN false ELSE true END
    """)
    boolean isBookingSlotEmpty(LocalDate date, Long timeId, Long themeId);

    @Query("""
    SELECT EXISTS (
        SELECT 1 FROM Reservation r
        WHERE r.bookingSlot.time.id = :reservationTimeId
      )
    """)
    boolean existsByTimeId(Long reservationTimeId);

    @Query("""
    SELECT EXISTS (
        SELECT 1 FROM Reservation r
        WHERE r.bookingSlot.theme.id = :themeId
      )
    """)
    boolean existsByThemeId(Long themeId);

    @Query("""
    SELECT EXISTS (
        SELECT 1 FROM Reservation r
        WHERE r.bookingSlot.date = :reservationDate
          AND r.bookingSlot.time.id = :timeId
          AND r.bookingSlot.theme.id = :themeId
        )
    """)
    boolean existsByDateAndTimeIdAndThemeId(LocalDate reservationDate, Long timeId, Long themeId);

    @Query("""
    SELECT EXISTS (
        SELECT 1 FROM Reservation r
        WHERE r.member.id = :memberId
          AND r.bookingSlot.theme.id = :themeId
          AND r.bookingSlot.time.id = :timeId
          AND r.bookingSlot.date = :date
        )
    """)
    boolean existsByMemberIdAndThemeIdAndTimeIdAndDate(Long memberId, Long themeId, Long timeId, LocalDate date);
}
