package roomescape.reservation.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.infrastructure.projection.ReservationSequenceProjection;
import roomescape.user.domain.UserId;

import java.util.List;

public interface JpaReservationRepository
        extends JpaRepository<Reservation, Long>,
        QuerydslPredicateExecutor<Reservation> {

    List<Reservation> findAllByUserId(UserId userId);

    @Query(value = """
            SELECT 
                ranked.id AS reservation_id,
                ranked.slot_rank AS slot_rank
            FROM (
                SELECT 
                    r.id,
                    ROW_NUMBER() OVER (
                        PARTITION BY r.date, r.time, r.theme_id
                        ORDER BY r.created_at, r.id
                    ) AS slot_rank
                FROM reservations r
            ) AS ranked
            WHERE ranked.id IN (:ids)
            """, nativeQuery = true)
    List<ReservationSequenceProjection> findSequenceOfSlotByIds(@Param("ids") List<Long> ids);
}
