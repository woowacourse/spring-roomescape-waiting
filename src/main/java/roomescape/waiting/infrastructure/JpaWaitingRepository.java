package roomescape.waiting.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingStatus;

public interface JpaWaitingRepository extends ListCrudRepository<Waiting, Long> {

    @Query("""
            SELECT w 
            FROM Waiting w 
            JOIN FETCH w.bookingSlot r            
            JOIN FETCH r.time t 
            JOIN FETCH r.theme th                   
            WHERE w.member.id = :memberId
            """)
    List<Waiting> findByWaitingsMemberId(Long memberId);

    @Modifying
    @Query("""
            DELETE FROM Waiting w 
            WHERE w.bookingSlot.id = :reservationId
            AND w.member.id = :memberId
            """)
    void deleteByBookingSlotIdAndMemberId(Long reservationId, Long memberId);

    boolean existsByBookingSlotIdAndMemberId(Long reservationId, Long memberId);

    @Query("""
            SELECT w 
            FROM Waiting w 
            JOIN FETCH w.bookingSlot r            
            JOIN FETCH r.time t 
            JOIN FETCH r.theme th                   
            WHERE w.waitingStatus = :waitingStatus
            """)
    List<Waiting> findAllByWaitingStatus(WaitingStatus waitingStatus);
}
