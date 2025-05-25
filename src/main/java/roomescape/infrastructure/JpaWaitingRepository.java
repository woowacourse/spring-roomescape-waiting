package roomescape.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;

@Repository
public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findByMemberId(Long id);

    List<Waiting> findByThemeId(Long id);

    List<Waiting> findByReservationTimeId(Long id);

    Optional<Waiting> findByDateAndReservationTimeAndThemeAndMember(LocalDate date, ReservationTime time, Theme theme,
                                                                    Member member);

    @Query("""
             SELECT new roomescape.domain.WaitingWithRank(
             w,
             (SELECT COUNT(w2) * 1L + 1
             FROM Waiting w2 
             WHERE w2.theme = w.theme 
             AND w2.date = w.date 
             AND w2.reservationTime = w.reservationTime 
             AND w2.createAt < w.createAt)) 
             FROM Waiting w 
             WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findByMemberIdSortedByCreateAt(@Param("memberId") Long memberId);

    @Query("""
             SELECT new roomescape.domain.WaitingWithRank(
             w,
             (SELECT COUNT(w2) * 1L + 1
             FROM Waiting w2 
             WHERE w2.theme = w.theme 
             AND w2.date = w.date 
             AND w2.reservationTime = w.reservationTime 
             AND w2.createAt < w.createAt)) 
             FROM Waiting w 
             WHERE w.date = :date
             AND w.reservationTime.id = :timeId
             AND w.theme.id = :themeId 
            """)
    List<WaitingWithRank> findByDateAndReservationTimeAndThemeSortedByCreateAt(@Param("date") LocalDate date,
                                                                               @Param("timeId") Long timeId,
                                                                               @Param("themeId") Long themeId);
}
