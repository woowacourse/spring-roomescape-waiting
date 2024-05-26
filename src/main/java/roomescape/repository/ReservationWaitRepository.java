package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.reservationwait.ReservationWait;
import roomescape.domain.reservationwait.ReservationWaitStatus;
import roomescape.domain.reservationwait.ReservationWaitWithRank;

public interface ReservationWaitRepository extends JpaRepository<ReservationWait, Long> {
    Optional<ReservationWait> findFirstByDateAndThemeIdAndTimeIdAndStatusOrderById(LocalDate date, long themeId, long timeId, ReservationWaitStatus status);

    List<ReservationWait> findAllByStatus(ReservationWaitStatus reservationWaitStatus);

    @Query("""
            SELECT new roomescape.domain.reservationwait.ReservationWaitWithRank(
                w,
                COUNT(w2)
            )
            FROM ReservationWait w
            LEFT JOIN ReservationWait w2
                ON w2.status = 'WAITING'
                AND w2.theme = w.theme
                AND w2.time = w.time
                AND w2.date = w.date
                AND w2.id < w.id
            WHERE w.status = 'WAITING'
            AND w.member.id = :memberId
            GROUP BY w
            ORDER BY w.id
            """)
    List<ReservationWaitWithRank> findReservationWaitWithRankByMemberId(Long memberId);

    boolean existsByDateAndTimeIdAndThemeIdAndStatus(LocalDate date, long timeId, long themeId, ReservationWaitStatus status);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberIdAndStatus(LocalDate date, long timeId, long themeId, long memberId, ReservationWaitStatus status);
}
