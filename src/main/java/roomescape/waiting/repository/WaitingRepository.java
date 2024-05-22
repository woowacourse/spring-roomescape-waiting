package roomescape.waiting.repository;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.model.Reservation;
import roomescape.waiting.model.Waiting;
import roomescape.waiting.model.WaitingWithRanking;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    default Waiting getById(Long id) {
        return findById(id).orElseThrow(
                () -> new NoSuchElementException("식별자 " + id + "에 해당하는 예약 대기가 존재하지 않습니다."));
    }

    @Query("""
            SELECT new roomescape.waiting.model.WaitingWithRanking(
                 w,
                 (SELECT COUNT(w2)
                  FROM Waiting w2 
                  WHERE w2.reservation = w.reservation 
                    AND w2.id < w.id)) 
             FROM Waiting w 
             WHERE w.member = :member
             """)
    List<WaitingWithRanking> findWaitingsWithRankByMember(Member member);

    boolean existsByMemberIdAndReservationId(Long memberId, Long reservationId);

    // TODO: 대처하기
    boolean existsByMemberAndReservation(Member member, Reservation reservation);
}
