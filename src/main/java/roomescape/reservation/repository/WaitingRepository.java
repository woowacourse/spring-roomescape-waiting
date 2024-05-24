package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.theme.domain.Theme;

public interface WaitingRepository extends ListCrudRepository<Waiting, Long> {

    boolean existsByDateAndReservationTimeAndThemeAndMember(
            LocalDate date,
            ReservationTime reservationTime,
            Theme theme,
            Member member
    );

    Optional<Waiting> findFirstByDateAndReservationTimeAndThemeOrderByIdAsc(
            LocalDate date,
            ReservationTime reservationTime,
            Theme theme
    );

    @Query("""
            SELECT new roomescape.reservation.domain.WaitingWithRank(
                w1,
                COUNT(w2) + 1
            )
            FROM Waiting w1
            LEFT JOIN Waiting w2
              ON w1.theme = w2.theme
             AND w1.date = w2.date
             AND w1.reservationTime = w2.reservationTime
             AND w2.id < w1.id
            WHERE w1.member.id = :memberId
            GROUP BY w1
            """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);
}
