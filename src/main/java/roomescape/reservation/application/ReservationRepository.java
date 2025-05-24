package roomescape.reservation.application;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationWithRank;
import roomescape.reservation.time.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Repository
public interface ReservationRepository extends ListCrudRepository<Reservation, Long> {
    List<Reservation> findAllByDateAndThemeId(LocalDate date, Long themeId);

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

    @Query("""
                    SELECT new roomescape.reservation.domain.ReservationWithRank(
                    r, CAST((SELECT COUNT(r2)
                    FROM Reservation r2
                    WHERE r2.theme = r.theme
                    AND r2.date = r.date
                    AND r2.reservationTime = r.reservationTime
                    AND r2.status = 'WAITING'
                    AND r2.id <= r.id) AS long))
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
                    AND r2.status = 'WAITING'
                    AND r2.id <= r.id) AS long))
                    FROM Reservation r
                    WHERE r.status = 'WAITING'
            """)
    List<ReservationWithRank> findReservationsWithRankOfWaitingStatus();

    @Query("""
                 SELECT r FROM Reservation r
                  WHERE r.date = :date
                  AND r.reservationTime = :reservationTime
                  AND r.theme = :theme
                  AND r.status = 'WAITING'
                  ORDER BY r.id ASC
                  LIMIT 1
            """)
    Reservation findFirstWaitingReservation(LocalDate date, ReservationTime reservationTime, Theme theme);

    boolean existsByReservationTimeId(Long timeId);

    boolean existsByDateAndReservationTimeStartAt(LocalDate date, LocalTime startAt);

    boolean existsByThemeId(Long themeId);
}
