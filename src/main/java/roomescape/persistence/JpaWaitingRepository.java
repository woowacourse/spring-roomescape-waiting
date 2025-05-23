package roomescape.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;

import java.time.LocalDate;
import java.util.List;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
        SELECT new roomescape.domain.WaitingWithRank(
            w, (SELECT COUNT(wr) + 1
                FROM Waiting wr
                WHERE wr.theme = w.theme
                    AND wr.date = w.date
                    AND wr.time = w.time
                    AND wr.createdAt < w.createdAt))
        FROM Waiting w
        WHERE w.member.id = :memberId
        ORDER BY w.date ASC, w.time.startAt ASC
    """)
    List<WaitingWithRank> findWaitingWithRankByMemberId(final Long memberId);

    boolean existsByMemberIdAndDateAndTimeIdAndThemeId(Long memberId, LocalDate date, Long timeId, Long themeId);

    List<Waiting> findByThemeIdAndDateAndTimeId(Long themeId, LocalDate date, Long timeId);
}
