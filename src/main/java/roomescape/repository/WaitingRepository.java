package roomescape.repository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithSequence;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    default Waiting getById(Long id) {
        return findById(id).orElseThrow(() -> new NoSuchElementException("[ERROR] 존재하지 않는 예약 대기입니다."));
    }

    @EntityGraph(attributePaths = {"member", "reservation"})
    List<Waiting> findAll();

    @EntityGraph(attributePaths = {"member", "reservation"})
    Optional<Waiting> findById(Long id);

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

    @EntityGraph(attributePaths = {"member", "reservation"})
    Optional<Waiting> findFirstByReservationOrderByIdAsc(Reservation reservation);
}
