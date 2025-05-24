package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);

    @Query("""
            SELECT new roomescape.reservation.domain.WaitingWithRank(
            	w,
            	(SELECT COUNT(w2)
            		FROM Waiting w2
            		WHERE w2.theme = w.theme
            		AND w2.date = w.date
            		AND w2.time = w.time
            		AND w2.createdAt <= w.createdAt)
            	)
            FROM Waiting w
            WHERE w.member.id = :memberId
                        """
    )
    List<WaitingWithRank> findAllWaitingWithRankByMemberId(Long memberId);

    @Query("""
            SELECT new roomescape.reservation.domain.WaitingWithRank(
            	w,
            	(SELECT COUNT(w2)
            		FROM Waiting w2
            		WHERE w2.theme = w.theme
            		AND w2.date = w.date
            		AND w2.time = w.time
            		AND w2.createdAt <= w.createdAt)
            	)
            FROM Waiting w
                        """
    )
    List<WaitingWithRank> findAllWaitingWithRank();
}
