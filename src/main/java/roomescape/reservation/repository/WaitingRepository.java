package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
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

//    @Query("""
//       SELECT new roomescape.reservation.domain.WaitingWithRank(
//           w,
//           (SELECT COUNT(w2) + 1
//            FROM Waiting w2
//            WHERE w2.theme = w.theme
//              AND w2.date = w.date
//              AND w2.reservationTime = w.reservationTime
//              AND w2.id < w.id)
//       )
//       FROM Waiting w
//       WHERE w.member.id = :memberId
//       """)
//    List<WaitingWithRank> findWaitingsWithRankByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT new roomescape.reservation.domain.WaitingWithRank(" +
            "    w, " +
            "    (SELECT COUNT(w2) + 1 " +
            "     FROM Waiting w2 " +
            "     WHERE w2.theme = w.theme " +
            "       AND w2.date = w.date " +
            "       AND w2.reservationTime = w.reservationTime " +
            "       AND w2.id < w.id)) " +
            "FROM Waiting w " +
            "WHERE w.member.id = :memberId")
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);
}
