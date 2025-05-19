package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.BookingStatus;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.time
            JOIN FETCH r.theme
            JOIN FETCH r.member
            """)
    List<Reservation> findAllWithAssociations();

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.time
            WHERE r.date = :date AND r.theme.id = :themeId
            """)
    List<Reservation> findByDateAndThemeIdWithAssociations(
            @Param("date") LocalDate date,
            @Param("themeId") Long themeId
    );

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.time
            JOIN FETCH r.theme
            JOIN FETCH r.member
            WHERE (:themeId IS NULL OR r.theme.id = :themeId)
              AND (:memberId IS NULL OR r.member.id = :memberId)
              AND (:from IS NULL OR r.date >= :from)
              AND (:to IS NULL OR r.date <= :to)
              AND (:status IS NULL OR r.bookingStatus = :status)
            """)
    List<Reservation> findByFilteringWithAssociations(
            @Param("themeId") Long themeId,
            @Param("memberId") Long memberId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("status") BookingStatus status
    );

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.theme
            JOIN FETCH r.time
            WHERE r.member.id = :memberId
            """)
    List<Reservation> findByMemberIdWithAssociations(@Param("memberId") Long memberId);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.time
            JOIN FETCH r.theme
            JOIN FETCH r.member
            WHERE r.bookingStatus = :status
            """)
    List<Reservation> findByStatusWithAssociations(@Param("status") BookingStatus status);

    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    boolean existsByIdAndMemberId(Long reservationId, Long memberId);

    boolean existsByDateAndThemeAndTimeAndBookingStatus(LocalDate date, Theme theme, ReservationTime time,
                                                        BookingStatus bookingStatus);

    @Query("""
              SELECT COUNT(r)
              FROM Reservation r
              WHERE r.theme = :theme
                AND r.date = :date
                AND r.time = :time
                AND r.id < :id
            """)
    Long countByThemeAndDateAndTimeAndIdLessThan(
            @Param("theme") Theme theme,
            @Param("date") LocalDate date,
            @Param("time") ReservationTime time,
            @Param("id") Long id
    );

}
