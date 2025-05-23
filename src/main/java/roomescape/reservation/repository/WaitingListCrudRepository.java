package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;

public interface WaitingListCrudRepository extends ListCrudRepository<Waiting, Long> {

    Optional<Waiting> findByIdAndMemberId(Long id, Long memberId);

    @Query("""
            SELECT w FROM Waiting w
            WHERE w.date = :date
            AND w.time.id = :timeId
            AND w.theme.id = :themeId
            ORDER BY w.id ASC
            LIMIT 1
            """)
    Optional<Waiting> findFirstWaitingByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    @Query("""
            SELECT new roomescape.reservation.domain.WaitingWithRank(
                w,
                (SELECT COUNT(w2)
                FROM Waiting w2
                WHERE w2.theme = w.theme
                AND w2.date = w.date
                AND w2.time = w.time
                AND w2.id < w.id) + 1)
            FROM Waiting w
            WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);
}
