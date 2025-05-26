package roomescape.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.presentation.dto.response.WaitingWithRank;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    Waiting save(Waiting waiting);

    @Query("""
            SELECT new roomescape.presentation.dto.response.WaitingWithRank(
                w.id,
                w.theme.name,
                w.date,
                w.time.startAt,
                (
                    SELECT COUNT(w2) + 1
                    FROM Waiting w2
                    WHERE w2.theme = w.theme
                      AND w2.date = w.date
                      AND w2.time = w.time
                      AND w2.createdAt < w.createdAt
                )
            )
            FROM Waiting w
            WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    boolean existsByDateAndThemeAndTimeAndMember(LocalDate date, Theme theme, ReservationTime time, Member member);

    Optional<Waiting> findFirstByDateAndThemeIdAndTimeIdOrderByCreatedAtAsc(LocalDate date, Long themeId, Long timeId);
}
