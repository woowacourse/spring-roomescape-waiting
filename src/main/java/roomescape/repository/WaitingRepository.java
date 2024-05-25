package roomescape.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndTimeIdAndMemberId(ReservationDate reservationDate, Long timeId, Long memberId);

    @Query("SELECT new roomescape.domain.reservation.WaitingWithRank(" +
            "    w, " +
            "    (SELECT COUNT(w2) + 1 " +
            "     FROM Waiting w2 " +
            "     WHERE w2.theme = w.theme " +
            "       AND w2.date = w.date " +
            "       AND w2.time = w.time " +
            "       AND w2.createdAt < w.createdAt)) " +
            "FROM Waiting w " +
            "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);
}
