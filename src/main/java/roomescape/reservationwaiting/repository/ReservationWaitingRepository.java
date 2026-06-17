package roomescape.reservationwaiting.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservationwaiting.domain.ReservationWaiting;

public interface ReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {

    List<ReservationWaiting> findByMemberId(Long memberId);

    boolean existsByMemberIdAndDateAndTimeIdAndThemeId(Long memberId, LocalDate date, Long timeId, Long themeId);

    @Query(value = """
            SELECT *
            FROM reservation_waiting
            WHERE date = :date AND time_id = :timeId AND theme_id = :themeId
            ORDER BY created_at, id
            LIMIT 1
            """, nativeQuery = true)
    Optional<ReservationWaiting> findFirstByDateAndTimeIdAndThemeId(@Param("date") LocalDate date,
                                                                    @Param("timeId") Long timeId,
                                                                    @Param("themeId") Long themeId);

    @Query("""
            SELECT new roomescape.reservationwaiting.repository.WaitingWithTurn(
                w,
                (SELECT COUNT(w2) FROM ReservationWaiting w2
                 WHERE w2.theme = w.theme
                   AND w2.date = w.date
                   AND w2.time = w.time
                   AND w2.id < w.id) + 1)
            FROM ReservationWaiting w
            WHERE w.member.id = :memberId
            ORDER BY w.id
            """)
    List<WaitingWithTurn> findWithTurnByMemberId(@Param("memberId") Long memberId);
}