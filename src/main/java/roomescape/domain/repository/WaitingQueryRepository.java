package roomescape.domain.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import roomescape.domain.Waiting;
import roomescape.domain.dto.WaitingWithRank;

public interface WaitingQueryRepository extends Repository<Waiting, Long> {

    @Query("""
            select new roomescape.domain.dto.WaitingWithRank(
            w,
                (select count(w2)
                from Waiting w2
                where w2.date = w.date
                and w2.time = w.time
                and w2.theme = w.theme
                and w2.id < w.id))
            from Waiting w
            where w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    boolean existsById(Long id);
}
