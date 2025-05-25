package roomescape.reservation.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.infrastructure.projection.WaitingWithRankProjection;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByReservationSlotAndMember(ReservationSlot reservationSlot, Member member);

    @Query(value = """
               SELECT
                   w.id AS id,
                   w.date AS date,
                
                   rt.id AS timeId,
                   rt.start_at AS timeStartAt,
                   
                   th.id AS themeId,
                   th.name AS themeName,
                   th.description AS themeDescription,
                   th.thumbnail AS themeThumbnail,
                
                   m.id AS memberId,
                   m.name AS memberName,
                   
                   ROW_NUMBER() OVER (
                       PARTITION BY th.id, w.date, rt.id
                       ORDER BY w.created_at
                   ) AS rank
               FROM waitings w
               JOIN reservation_times rt ON w.time_id = rt.id
               JOIN themes th ON w.theme_id = th.id
               JOIN members m ON w.member_id = m.id
               WHERE w.member_id = ?
            """, nativeQuery = true
    )
    List<WaitingWithRankProjection> findAllWaitingWithRankProjectionByMemberId(Long memberId);

    @Query(value = """
               SELECT
                   w.id AS id,
                   w.date AS date,
                
                   rt.id AS timeId,
                   rt.start_at AS timeStartAt,
                   
                   th.id AS themeId,
                   th.name AS themeName,
                   th.description AS themeDescription,
                   th.thumbnail AS themeThumbnail,
                
                   m.id AS memberId,
                   m.name AS memberName,
                   
                   ROW_NUMBER() OVER (
                       PARTITION BY th.id, w.date, rt.id
                       ORDER BY w.created_at
                   ) AS rank
               FROM waitings w
               JOIN reservation_times rt ON w.time_id = rt.id
               JOIN themes th ON w.theme_id = th.id
               JOIN members m ON w.member_id = m.id
            """, nativeQuery = true
    )
    List<WaitingWithRankProjection> findAllWaitingWithRankProjection();

    Optional<Waiting> findFirstByReservationSlotOrderByCreatedAt(ReservationSlot reservationSlot);
}
