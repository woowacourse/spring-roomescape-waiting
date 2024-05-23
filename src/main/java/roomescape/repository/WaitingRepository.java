package roomescape.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.member.Member;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithSequence;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
             SELECT new roomescape.domain.waiting.WaitingWithSequence(
                 w,
                 (
                     SELECT count(*) + 1
                     FROM Waiting w2
                     WHERE
                         w2.reservation = w.reservation AND
                         w2.id < w.id
                 )
             )
            FROM Waiting w
            JOIN FETCH w.reservation
            JOIN FETCH w.member
            WHERE w.member = :member
             """
    )
    List<WaitingWithSequence> findWaitingsWithSequenceByMember(Member member);
}
