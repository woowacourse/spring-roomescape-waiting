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
            w, (SELECT COUNT(wr)
                FROM Waiting wr
                WHERE wr.theme = w.theme
                    AND wr.date = w.date
                    AND wr.time = w.time
                    AND wr.id < w.id))
        FROM Waiting w
        WHERE w.member.id = :memberId
    """)
    List<WaitingWithRank> findWaitingWithRankByMemberId(final Long memberId);

    boolean existsByMemberIdAndDateAndTimeIdAndThemeId(Long memberId, LocalDate date, Long timeId, Long themeId);
}
