package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;

public interface WaitingListCrudRepository extends ListCrudRepository<Waiting, Long> {

    Optional<Waiting> findByIdAndMemberId(Long id, Long memberId);

    @Query("""
            SELECT w FROM Waiting w
            WHERE w.details.date = :date
            AND w.details.time.id = :timeId
            AND w.details.theme.id = :themeId
            ORDER BY w.id ASC
            LIMIT 1
            """)
    Optional<Waiting> findFirstWaitingByDetails_DateAndDetails_Time_IdAndDetails_Theme_Id(@Param("date") LocalDate date, @Param("timeId") Long timeId, @Param("themeId") Long themeId);

    @Query("""
            SELECT new roomescape.reservation.domain.WaitingWithRank(
                w,
                (SELECT COUNT(w2)
                FROM Waiting w2
                WHERE w2.details.theme = w.details.theme
                AND w2.details.date = w.details.date
                AND w2.details.time = w.details.time
                AND w2.id < w.id) + 1)
            FROM Waiting w
            WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingWithRankByMemberId(@Param("memberId") Long memberId);

    boolean existsByDetails_DateAndDetails_Time_IdAndDetails_Theme_IdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);
}
