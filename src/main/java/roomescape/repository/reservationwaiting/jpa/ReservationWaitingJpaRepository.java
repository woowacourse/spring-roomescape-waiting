package roomescape.repository.reservationwaiting.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationWaitingJpaRepository extends JpaRepository<ReservationWaitingJpaEntity, Long> {

    @Override
    @EntityGraph(attributePaths = {"slot", "slot.theme", "slot.time"})
    List<ReservationWaitingJpaEntity> findAll();

    @Override
    @EntityGraph(attributePaths = {"slot", "slot.theme", "slot.time"})
    Optional<ReservationWaitingJpaEntity> findById(Long id);

    @EntityGraph(attributePaths = {"slot", "slot.theme", "slot.time"})
    List<ReservationWaitingJpaEntity> findAllBySlot_IdOrderByRequestedAtAscIdAsc(Long slotId);

    @EntityGraph(attributePaths = {"slot", "slot.theme", "slot.time"})
    List<ReservationWaitingJpaEntity> findAllByOrderBySlot_DateAscSlot_Theme_IdAscSlot_Time_IdAscRequestedAtAscIdAsc();

    @Query("""
            select new roomescape.repository.reservationwaiting.jpa.WaitingWithRank(
                    w.id,
                    w.slot.id,
                    w.name,
                    w.requestedAt,
                    (select count(w2) + 1
                     from ReservationWaitingJpaEntity w2
                     where w2.slot = w.slot
                       and (
                           w2.requestedAt < w.requestedAt
                           or (w2.requestedAt = w.requestedAt and w2.id < w.id)
                       )
                    )
            )
            from ReservationWaitingJpaEntity w
            where w.name = :name
            order by w.slot.date, w.slot.theme.id, w.slot.time.id, w.requestedAt, w.id
            """)
    List<WaitingWithRank> findAllWithRankByName(@Param("name") String name);
}
