package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.projection.ReservationWaitingWithOrder;

public interface ReservationWaitingQueryRepository extends JpaRepository<ReservationWaiting, Long> {

    @Query("""
            SELECT new roomescape.domain.projection.ReservationWaitingWithOrder(
                waiting.id,
                waiting.waiter.name,
                waiting.slot.date,
                waiting.slot.time,
                waiting.slot.theme,
                (
                    SELECT COUNT(previous)
                    FROM ReservationWaiting previous
                    WHERE previous.slot.date = waiting.slot.date
                        AND previous.slot.time = waiting.slot.time
                        AND previous.slot.theme = waiting.slot.theme
                        AND previous.id <= waiting.id
                )
            )
            FROM ReservationWaiting waiting
            WHERE waiting.id = :id
            """)
    Optional<ReservationWaitingWithOrder> findWithOrderById(@Param("id") Long id);

    @Query("""
            SELECT new roomescape.domain.projection.ReservationWaitingWithOrder(
                waiting.id,
                waiting.waiter.name,
                waiting.slot.date,
                waiting.slot.time,
                waiting.slot.theme,
                (
                    SELECT COUNT(previous)
                    FROM ReservationWaiting previous
                    WHERE previous.slot.date = waiting.slot.date
                        AND previous.slot.time = waiting.slot.time
                        AND previous.slot.theme = waiting.slot.theme
                        AND previous.id <= waiting.id
                )
            )
            FROM ReservationWaiting waiting
            WHERE waiting.waiter.name = :name
            ORDER BY waiting.slot.date DESC, waiting.slot.time.startAt ASC, waiting.id ASC
            """)
    List<ReservationWaitingWithOrder> findByName(@Param("name") String name);
}
