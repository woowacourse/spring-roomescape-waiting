package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
             SELECT new roomescape.domain.WaitingWithRank(
                 w,
                 (SELECT COUNT(w2)
                  FROM Waiting w2
                  WHERE w2.theme = w.theme
                  AND w2.date = w.date
                  AND w2.time = w.time
                  AND w2.id < w.id))
             FROM Waiting w
             WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    Optional<Waiting> findFirstByDateAndThemeAndTime(LocalDate date, Theme theme, ReservationTime time);

    boolean existsWaitingByDateAndAndThemeAndMember(LocalDate date, Theme theme, Member member);
}
