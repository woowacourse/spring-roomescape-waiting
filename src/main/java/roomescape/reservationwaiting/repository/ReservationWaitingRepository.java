package roomescape.reservationwaiting.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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

    @Query(value = """
            SELECT sub.id AS id, sub.turn AS turn
            FROM (
                SELECT id, ROW_NUMBER() OVER (PARTITION BY date, time_id, theme_id ORDER BY created_at, id) AS turn
                FROM reservation_waiting
            ) sub
            JOIN reservation_waiting rw ON rw.id = sub.id
            WHERE rw.member_id = :memberId
            ORDER BY rw.id
            """, nativeQuery = true)
    List<WaitingTurnView> findWaitingTurnsByMemberId(@Param("memberId") Long memberId);

    default List<WaitingWithTurn> findWithTurnByMemberId(Long memberId) {
        List<WaitingTurnView> rows = findWaitingTurnsByMemberId(memberId);
        Map<Long, ReservationWaiting> byId = findAllById(
                rows.stream().map(WaitingTurnView::getId).toList()
        ).stream().collect(Collectors.toMap(ReservationWaiting::getId, waiting -> waiting));
        return rows.stream()
                .map(row -> new WaitingWithTurn(byId.get(row.getId()), row.getTurn()))
                .toList();
    }
}