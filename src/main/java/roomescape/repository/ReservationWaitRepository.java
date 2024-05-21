package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.ReservationWait;
import roomescape.domain.ReservationWaitWithRank;

public interface ReservationWaitRepository extends JpaRepository<ReservationWait, Long> {
    List<ReservationWait> findByMemberId(long memberId);

    @Query("""
            SELECT new roomescape.domain.ReservationWaitWithRank(
                w,
                (SELECT COUNT(w2)
                FROM ReservationWait w2
                WHERE w2.theme = w.theme
                AND w2.date = w.date
                AND w2.id < w.id))
            FROM ReservationWait w
            WHERE w.member.id = :memberId
            """)
    List<ReservationWaitWithRank> findReservationWaitWithRankByMemberId(Long memberId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, long timeId, long themeId, long memberId);
}
