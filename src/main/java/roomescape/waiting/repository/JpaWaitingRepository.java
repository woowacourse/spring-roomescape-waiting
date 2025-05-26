package roomescape.waiting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.member.entity.Member;
import roomescape.reservationTime.entity.ReservationTime;
import roomescape.theme.entity.Theme;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.entity.WaitingWithRank;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JpaWaitingRepository extends JpaRepository<Waiting,Long> {

    @Query("SELECT new roomescape.waiting.entity.WaitingWithRank(" +
            "    w, " +
            "    (SELECT COUNT(w2) " +
            "     FROM Waiting w2 " +
            "     WHERE w2.theme = w.theme " +
            "       AND w2.date = w.date " +
            "       AND w2.time = w.time " +
            "       AND w2.id < w.id)) " +
            "FROM Waiting w " +
            "WHERE w.member.id = :member_id")
    List<WaitingWithRank> findWaitingsWithRankByMemberId(@Param("member_id") Long memberId);


    @Query("SELECT new roomescape.waiting.entity.WaitingWithRank(" +
            "    w, " +
            "    (SELECT COUNT(w2) " +
            "     FROM Waiting w2 " +
            "     WHERE w2.theme = w.theme " +
            "       AND w2.date = w.date " +
            "       AND w2.time = w.time " +
            "       AND w2.id < w.id)) " +
            "FROM Waiting w ")
    List<WaitingWithRank> findAllWaitingWithRank();

    Optional<Waiting> findFirstByThemeAndDateAndTimeOrderByIdAsc(Theme theme, LocalDate date, ReservationTime time);

}
