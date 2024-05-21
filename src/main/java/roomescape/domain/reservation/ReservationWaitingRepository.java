package roomescape.domain.reservation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {
    @Query("""
            SELECT
                new roomescape.domain.reservation.WaitingWithRank(
                    rw, CAST((
                    SELECT COUNT(rw2)
                    FROM ReservationWaiting rw2
                    WHERE rw2.schedule = rw.schedule
                        AND rw2.theme = rw.theme
                        AND rw2.createdAt <= rw.createdAt) AS Long)
                )
            FROM ReservationWaiting rw
            WHERE rw.member.id = :memberId""")
    List<WaitingWithRank> findWithRankByMemberId(long memberId);
}
