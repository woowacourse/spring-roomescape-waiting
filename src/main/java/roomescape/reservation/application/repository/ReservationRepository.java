package roomescape.reservation.application.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationWithRank;

public interface ReservationRepository extends ListCrudRepository<Reservation, Long> {
    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    @Query("""
                SELECT r FROM Reservation r
                WHERE (:memberId IS NULL OR r.member.id = :memberId)
                  AND (:themeId IS NULL OR r.theme.id = :themeId)
                  AND (:fromDate IS NULL OR r.date >= :fromDate)
                  AND (:endDate IS NULL OR r.date <= :endDate)
            """)
    List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(
            Long memberId,
            Long themeId,
            LocalDate fromDate,
            LocalDate endDate
    );

    List<Reservation> findByMemberId(Long memberId);

    boolean existsByReservationTimeId(Long timeId);

    boolean existsByDateAndReservationTimeStartAt(LocalDate date, LocalTime startAt);

    boolean existsByThemeId(Long themeId);

    @Query("""
                    SELECT new roomescape.reservation.domain.ReservationWithRank(
                    r, CAST((SELECT COUNT(r2)
                    FROM Reservation r2
                    WHERE r2.theme = r.theme
                    AND r2.date = r.date
                    AND r2.reservationTime = r.reservationTime
                    AND r2.id < r.id) AS long))
                    FROM Reservation r
                    WHERE r.member.id = :memberId
            """)
    List<ReservationWithRank> findReservationsWithRankByMemberId(Long memberId);

    @Query("""
                    SELECT new roomescape.reservation.domain.ReservationWithRank(
                    r, CAST((SELECT COUNT(r2)
                    FROM Reservation r2
                    WHERE r2.theme = r.theme
                    AND r2.date = r.date
                    AND r2.reservationTime = r.reservationTime
                    AND r2.id < r.id) AS long))
                    FROM Reservation r
                    WHERE r.status = 'WAITING'
            """)
    List<ReservationWithRank> findReservationsWithRankOfWaitingStatus();
}
