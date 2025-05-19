package roomescape.waiting.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.entity.Member;
import roomescape.theme.entity.Theme;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.entity.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    long countByDateAndThemeAndMember(LocalDate date, Theme theme, Member member);

    @Query("""
            SELECT new roomescape.waiting.entity.WaitingWithRank(
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

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long theme, Long timeId, Long memberId);
}
