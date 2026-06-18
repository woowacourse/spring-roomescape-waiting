package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.date.domain.ReservationDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {


    @Query("""
            SELECT 
            r
            FROM reservation r
            WHERE (:status is null or r.status = :status)
            ORDER BY r.id DESC
        """)
    List<Reservation> findAllByStatusOptional(@Param("status") ReservationStatus status);

    @Query("""
            SELECT
            new roomescape.reservation.repository.dto.ReservationWithWaitingTurn(
            r.id,
            r.member.id,
            r.date.date,
            r.time.startAt,
            r.theme.id,
            r.theme.name,
            r.theme.thumbnailUrl,
            r.status,
            CASE 
                WHEN r.status = roomescape.reservation.domain.ReservationStatus.WAITING
                    AND(
                        r.date.date > CURRENT_DATE
                        OR (r.date.date = CURRENT_DATE AND r.time.startAt > CURRENT_TIME)
                    )
                THEN (
                SELECT COUNT(wait) + 1 
                FROM reservation wait
                WHERE wait.date = r.date
                    AND wait.time = r.time
                    AND wait.theme = r.theme
                    AND wait.status = roomescape.reservation.domain.ReservationStatus.WAITING
                    AND wait.waitingOrder < r.waitingOrder
                )
                ELSE NULL
            END
            )
            FROM reservation r
            WHERE r.member.id = :memberId
            ORDER BY r.id DESC
        """)
    List<ReservationWithWaitingTurn> findAllByMemberIdWithWaitingTurn(
        @Param("memberId") Long memberId);

    @Query("""
        SELECT coalesce(max(r.waitingOrder), 0) + 1
        FROM reservation r
        WHERE r.date = :date
        AND r.time = :time
        AND r.theme = :theme
        AND r.status = roomescape.reservation.domain.ReservationStatus.WAITING
        """)
    Long findNextWaitingOrderByDateAndTimeAndTheme(
        @Param("date") ReservationDate reservationDate,
        @Param("time") ReservationTime reservationTime,
        @Param("theme") Theme theme);

    @Query("""
        SELECT r
        FROM reservation r
        WHERE r.date = :date
        AND r.time = :time
        AND r.theme = :theme
        AND r.status <> roomescape.reservation.domain.ReservationStatus.CANCELED
        """)
    List<Reservation> findAllActiveByDateAndTimeAndTheme(
        @Param("date") ReservationDate reservationDate,
        @Param("time") ReservationTime reservationTime,
        @Param("theme") Theme theme);

    @Query("""
        SELECT r
        FROM reservation r
        WHERE r.status = roomescape.reservation.domain.ReservationStatus.WAITING
        AND r.date = :date
        AND r.time = :time
        AND r.theme = :theme
        AND (
            r.date.date > CURRENT_DATE 
            OR (r.date.date = CURRENT_DATE AND r.time.startAt > CURRENT_TIME)
        )
        ORDER BY r.waitingOrder ASC
        LIMIT 1
        """)
    Optional<Reservation> findFirstWaitingByDateAndTimeAndTheme(
        @Param("date") ReservationDate reservationDate,
        @Param("time") ReservationTime reservationTime,
        @Param("theme") Theme theme);

    @EntityGraph(attributePaths = {"date", "time", "theme"})
    List<Reservation> findAllByMemberIdOrderByIdDesc(Long id);
}
