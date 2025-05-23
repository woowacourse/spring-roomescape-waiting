package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.dto.query.WaitingWithRank;
import roomescape.entity.WaitingReservation;

@Repository
public interface WaitingReservationRepository extends JpaRepository<WaitingReservation, Long> {
    boolean existsByMemberIdAndTimeIdAndThemeIdAndDate(Long memberId, Long timeId, Long themeId, LocalDate date);

    @Query("""
            SELECT new roomescape.dto.query.WaitingWithRank(
                w,
                (
                    SELECT COUNT(w2)
                    FROM WaitingReservation w2
                    WHERE w2.theme.id = w.theme.id
                      AND w2.date = w.date
                      AND w2.time.id = w.time.id
                      AND w2.id <= w.id
                )
            )
            FROM WaitingReservation w
            WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(@Param("memberId") Long memberId);

    @Query("""
            SELECT wr 
            FROM WaitingReservation wr 
            WHERE wr.date = :date 
              AND wr.theme.id = :themeId 
              AND wr.time.id = :timeId 
            ORDER BY wr.id ASC
            """)
    Optional<WaitingReservation> findFirstWaitingByDateAndThemeIdAndTimeId(
            @Param("date") LocalDate date,
            @Param("themeId") Long themeId,
            @Param("timeId") Long timeId
    );
}
