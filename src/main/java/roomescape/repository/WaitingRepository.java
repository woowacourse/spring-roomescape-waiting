package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
             SELECT new roomescape.domain.reservation.WaitingWithRank(
                 w,
                 (SELECT COUNT(w2)
                  FROM Waiting w2
                  WHERE w2.theme = w.theme
                  AND w2.date = w.date
                  AND w2.time = w.time
                  AND w2.id < w.id))
             FROM Waiting w
             WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    Optional<Waiting> findFirstByDateAndThemeAndTime(LocalDate date, Theme theme, ReservationTime time);

    boolean existsByDateAndThemeAndMember(LocalDate date, Theme theme, Member member);
}
