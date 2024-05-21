package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.ReservationWait;
import roomescape.domain.ReservationWaitStatus;
import roomescape.domain.ReservationWaitWithRank;

public interface ReservationWaitRepository extends JpaRepository<ReservationWait, Long> {
    Optional<ReservationWait> findFirstByDateAndThemeIdAndTimeIdAndStatusOrderById(LocalDate date, long themeId, long timeId, ReservationWaitStatus status);

    List<ReservationWait> findAllByStatus(ReservationWaitStatus reservationWaitStatus);

    @Query("""
            SELECT new roomescape.domain.ReservationWaitWithRank(
                w,
                (SELECT COUNT(w2)
                FROM ReservationWait w2
                WHERE w2.status = 'WAITING'
                AND w2.theme = w.theme
                AND w2.date = w.date
                AND w2.id < w.id))
            FROM ReservationWait w
            WHERE w.status = 'WAITING'
            AND w.member.id = :memberId
            """)
    List<ReservationWaitWithRank> findReservationWaitWithRankByMemberId(Long memberId);

    boolean existsByDateAndTimeIdAndThemeIdAndStatus(LocalDate date, long timeId, long themeId, ReservationWaitStatus status);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberIdAndStatus(LocalDate date, long timeId, long themeId, long memberId, ReservationWaitStatus status);
}
