package roomescape.reservation.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.dto.response.FindWaitingRankResponse;
import roomescape.reservation.model.Slot;
import roomescape.reservation.model.Waiting;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
            SELECT w
            FROM Waiting w
            JOIN FETCH w.member m
            JOIN FETCH w.slot.reservationTime rt
            JOIN FETCH w.slot.theme t
            """)
    List<Waiting> findAll();

    boolean existsBySlot(Slot slot);

    boolean existsBySlotAndMemberId(Slot slot, Long memberId);

    @Query("""
            SELECT new roomescape.member.dto.response.FindWaitingRankResponse(
                myWaiting.id AS waitingId,
                myWaiting.slot.theme.name AS theme,
                myWaiting.slot.date AS date,
                myWaiting.slot.reservationTime.startAt AS time,
                COUNT(otherWaiting) AS waitingNumber
            )
            FROM Waiting myWaiting
            JOIN Waiting otherWaiting
              ON myWaiting.slot = otherWaiting.slot
            WHERE myWaiting.member.id = :memberId
              AND otherWaiting.id <= myWaiting.id
            GROUP BY otherWaiting.slot
            """)
    List<FindWaitingRankResponse> findAllWaitingResponses(Long memberId);

    boolean existsBySlotAndIdLessThan(Slot slot, Long id);
}
