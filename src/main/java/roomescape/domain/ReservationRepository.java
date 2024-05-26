package roomescape.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long id);

    List<Reservation> findByMemberId(Long id);

    boolean existsByReservationTimeId(Long id);

    boolean existsByThemeId(Long id);

    boolean existsByDateAndReservationTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    @Query("""
            SELECT COUNT(r)
            FROM Reservation r
            JOIN Reservation s
            ON r.date = s.date
            AND r.theme.id = s.theme.id
            AND r.reservationTime.id = s.reservationTime.id
            WHERE r.id < s.id
            AND s.id = :reservationId
            AND s.reservationStatus = :reservationStatus
    """)
    Long countPreviousReservationsWithSameDateThemeTimeAndStatus(@Param("reservationId") Long reservationId, @Param("reservationStatus") ReservationStatus reservationStatus);

    @Query("""
            SELECT r.theme.id
            FROM Reservation r
            WHERE r.date >= :from AND r.date < :to
            GROUP BY r.theme
            ORDER BY COUNT(r.id) DESC, r.theme.id ASC
            """)
    List<Long> findMostReservedThemesId(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
            SELECT r
            FROM Reservation r
            WHERE (:themeId IS NULL OR r.theme.id = :themeId)
            AND (:memberId IS NULL OR r.member.id = :memberId)
            AND (:from IS NULL OR r.date >= :from)
            AND (:to IS NULL OR r.date <= :to)
            AND (r.reservationStatus = :reservationStatus)
            """)
    List<Reservation> filter(
            @Param("themeId") Long themeId,
            @Param("memberId") Long memberId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("reservationStatus") ReservationStatus reservationStatus);
}
