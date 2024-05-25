package roomescape.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.ReservationWaitingWithRank;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {

    boolean existsByMemberIdAndDateAndTimeIdAndThemeId(Long memberId, ReservationDate date, Long timeId, Long themeId);

    @Query("""
            SELECT new roomescape.domain.ReservationWaitingWithRank(
                w,
                CAST((SELECT COUNT(w2) + 1
                    FROM ReservationWaiting w2
                    WHERE w2.theme = w.theme
                      AND w2.date = w.date
                      AND w2.time = w.time
                      AND w2.status = roomescape.domain.ReservationWaitingStatus.ALLOWED
                      AND w2.id < w.id) AS Long))
            FROM ReservationWaiting w
            WHERE w.member.id = :memberId
            """)
    List<ReservationWaitingWithRank> findAllWaitingWithRankByMemberId(Long memberId);

    Optional<ReservationWaiting> findTopByDateAndTimeIdAndThemeIdOrderById(ReservationDate date, Long timeId, Long themeId);
}
