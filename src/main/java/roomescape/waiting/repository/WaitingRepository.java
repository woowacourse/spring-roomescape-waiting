package roomescape.waiting.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    default boolean existsBy(LocalDate date, Long timeId, Long themeId, Long memberId) {
        return existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId, memberId);
    }

    @Query("""
            select exists (
                select w
                from Waiting w
                where w.reservationDatetime.reservationDate.date = :date
                    and w.reservationDatetime.reservationTime.id = :timeId
                    and w.theme.id = :themeId
                    and w.waiter.id = :memberId)
            """)
    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);

    @Query("""
            select w
            from Waiting w
            where w.waiter.id = :memberId
            """)
    List<Waiting> findByMemberId(Long memberId);

    @Query("""
            select new roomescape.waiting.domain.WaitingWithRank(
                w,
                cast((
                    select count(w2)
                    from Waiting w2
                    where w2.theme = w.theme
                      and w2.reservationDatetime.reservationDate.date = w.reservationDatetime.reservationDate.date
                      and w2.reservationDatetime.reservationTime.id   = w.reservationDatetime.reservationTime.id
                      and w2.waitedAt < w.waitedAt
                ) as long)
            )
            from Waiting w
            where w.waiter.id = :memberId
            """)
    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);
}
