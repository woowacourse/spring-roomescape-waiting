package roomescape.reservation.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r
            WHERE r.time.id = :id
        """)
    boolean existsByTimeId(Long id);

    @Query("""
            SELECT r FROM Reservation r
            WHERE (:themeId IS NULL OR r.theme.id = :themeId)
              AND (:memberId IS NULL OR r.member.id = :memberId)
              AND (
                (:dateFrom IS NULL AND :dateTo IS NULL)
                OR (:dateFrom IS NULL AND r.date <= :dateTo)
                OR (:dateTo IS NULL AND r.date >= :dateFrom)
                OR (r.date BETWEEN :dateFrom AND :dateTo)
              )
        """)
    List<Reservation> findByMemberAndThemeAndVisitDateBetween(
        Long themeId,
        Long memberId,
        LocalDate dateFrom,
        LocalDate dateTo
    );

    List<Reservation> findAllByMember(Member member);

    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.date = :date
              AND r.time = :time
              AND r.theme = :theme
              AND r.priority.value = (
                  SELECT MAX(r2.priority.value)
                  FROM Reservation r2
                  WHERE r2.date = :date
                    AND r2.time = :time
                    AND r2.theme = :theme
              )
        """)
    Optional<Reservation> findByLowestPriorityByDateAndTimeAndTheme(
        LocalDate date,
        ReservationTime time,
        Theme theme
    );

    @Query("""
            SELECT COUNT(*)
            FROM Reservation r
            WHERE r.date = :date
                AND r.time = :time
                AND r.theme = :theme
                AND r.priority.value < :priority
        """)
    long findOrder(
        LocalDate date,
        ReservationTime time,
        Theme theme,
        long priority
    );

    @Query("""
            SELECT r FROM Reservation r
            WHERE NOT EXISTS (
                SELECT 1 FROM Reservation r2
                WHERE r2.date = r.date
                  AND r2.time = r.time
                  AND r2.theme = r.theme
                  AND r2.priority.value < r.priority.value
            )
        """)
    List<Reservation> findHighestPriorityReservations();

    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r
            WHERE r = :reservation
              AND NOT EXISTS (
                SELECT 1 FROM Reservation r2
                WHERE r2.date = r.date
                  AND r2.time = r.time
                  AND r2.theme = r.theme
                  AND r2.priority.value < r.priority.value
              )
        """)
    boolean isHighestPriorityWaiting(Reservation reservation);
}
