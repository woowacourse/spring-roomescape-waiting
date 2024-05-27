package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.Waiting;
import roomescape.repository.dto.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndTimeIdAndMemberId(ReservationDate reservationDate, Long timeId, Long memberId);

    @Query("SELECT new roomescape.repository.dto.WaitingWithRank(" +
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

    Optional<Waiting> findFirstByDateAndTimeAndThemeOrderByCreatedAt(ReservationDate reservationDate,
                                                                     ReservationTime reservationTime,
                                                                     Theme theme);
}
