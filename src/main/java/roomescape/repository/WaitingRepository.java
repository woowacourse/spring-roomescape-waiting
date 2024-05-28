package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.Waiting;
import roomescape.repository.dto.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndTimeIdAndMemberId(ReservationDate reservationDate, Long timeId, Long memberId);

    @Query("""
            SELECT new roomescape.repository.dto.WaitingWithRank(
            w1, COUNT(w2)) 
            FROM Waiting w1 
            INNER JOIN Waiting w2 
            ON w1.theme = w2.theme
            AND w2.date = w1.date 
            AND w2.time = w1.time 
            AND w2.createdAt < w1.createdAt 
            WHERE w1.member.id = :memberId 
            GROUP BY w1.id
            """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    Optional<Waiting> findFirstByDateAndTimeAndThemeOrderByCreatedAt(ReservationDate reservationDate,
                                                                     ReservationTime reservationTime,
                                                                     Theme theme);
}
