package roomescape.repository.waiting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.member.Member;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingWithRank;

import java.time.LocalDate;
import java.util.List;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("SELECT new roomescape.domain.waiting.WaitingWithRank(" +
            "    w, " +
            "    (SELECT COUNT(w2) + 1L " +
            "     FROM Waiting w2 " +
            "     WHERE w2.theme = w.theme " +
            "       AND w2.date = w.date " +
            "       AND w2.time = w.time " +
            "       AND w2.id < w.id)) " +
            "FROM Waiting w " +
            "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    boolean existsByDateAndTimeAndThemeAndMember(LocalDate date, ReservationTime time, Theme theme, Member member);
}
