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
                  WHERE w2.schedule.theme = w.schedule.theme
                  AND w2.schedule.date = w.schedule.date
                  AND w2.schedule.time = w.schedule.time
                  AND w2.id < w.id))
             FROM Waiting w
             WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    Optional<Waiting> findFirstBySchedule_DateAndSchedule_ThemeAndSchedule_Time(LocalDate date, Theme theme, ReservationTime time);

    boolean existsBySchedule_DateAndSchedule_ThemeAndMember(LocalDate date, Theme theme, Member member);
}
