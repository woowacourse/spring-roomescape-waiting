package roomescape.waiting.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingWithRank;

@Transactional(readOnly = true)
public interface WaitingJpaRepository extends JpaRepository<Waiting, Long>,
        WaitingRepository {

    @Query("""
                SELECT new roomescape.waiting.domain.WaitingWithRank(
                    w,
                    (SELECT COUNT(w2)
                     FROM Waiting w2
                     WHERE w2.theme = w.theme
                       AND w2.reservationDate = w.reservationDate
                       AND w2.reservationTime = w.reservationTime
                       AND w2.id < w.id)
                )
                FROM Waiting w
                WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findAllWaitingWithRankByMemberId(
            @Param("memberId") Long memberId
    );

    @Query("""
                SELECT new roomescape.waiting.domain.WaitingWithRank(
                    w,
                    (SELECT COUNT(w2)
                     FROM Waiting w2
                     WHERE w2.theme = w.theme
                       AND w2.reservationDate = w.reservationDate
                       AND w2.reservationTime = w.reservationTime
                       AND w2.id < w.id)
                )
                FROM Waiting w
            """)
    List<WaitingWithRank> findAllWithRank(
    );

    @Query("""
                select w
                from Waiting w
                join fetch w.reservationTime t
                join fetch w.theme th
                join fetch w.member m
            """)
    List<Waiting> findAll(
    );

    @Query("""
                select w
                from Waiting w
                join fetch w.reservationTime t
                join fetch w.theme th
                join fetch w.member m
                where w.id = :waitingId
            """)
    Optional<Waiting> findById(
            @Param("waitingId") Long id
    );
}
