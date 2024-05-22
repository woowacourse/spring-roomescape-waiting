package roomescape.registration.waiting.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import roomescape.registration.waiting.Waiting;
import roomescape.registration.waiting.WaitingWithRank;

public interface WaitingRepository extends CrudRepository<Waiting, Long> {

    List<Waiting> findAllByMemberId(long memberId);

    List<Waiting> findAll();

    @Query("SELECT new roomescape.registration.waiting.WaitingWithRank(" +
            "    w, " +
            "    (SELECT COUNT(w2) + 1" +
            "     FROM Waiting w2 " +
            "     WHERE w2.theme.name = w.theme.name " +
            "       AND w2.date = w.date " +
            "       AND w2.reservationTime.startAt = w.reservationTime.startAt " +
            "       AND w2.id < w.id))" +
            "FROM Waiting w " +
            "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);
}
