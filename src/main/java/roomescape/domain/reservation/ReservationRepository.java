package roomescape.domain.reservation;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.RoomEscapeException;
import roomescape.domain.member.Member;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static roomescape.domain.DomainErrorCode.RESOURCE_NOT_FOUND;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByMember(Member member);

    List<Reservation> findBySlot_Id(Long slotId);

    @EntityGraph(attributePaths = {"member", "slot", "slot.time", "slot.theme"})
    Optional<Reservation> findById(Long id);

    @EntityGraph(attributePaths = {"member", "slot", "slot.time", "slot.theme"})
    List<Reservation> findByMemberId(Long memberId);

    @Query("""
            SELECT CASE WHEN r.status = roomescape.domain.reservation.Status.APPROVED THEN 0L
                        ELSE (SELECT COUNT(w) + 1L FROM Reservation w
                              WHERE w.slot = r.slot
                                AND w.status = roomescape.domain.reservation.Status.WAITING
                                AND w.createdAt < r.createdAt)
                   END
            FROM Reservation r WHERE r.id = :id
            """)
    Long findRankById(@Param("id") Long id);

    @Query("""
            SELECT r.id,
                CASE WHEN r.status = roomescape.domain.reservation.Status.APPROVED THEN 0L
                     ELSE (SELECT COUNT(w) + 1L FROM Reservation w
                           WHERE w.slot = r.slot
                             AND w.status = roomescape.domain.reservation.Status.WAITING
                             AND w.createdAt < r.createdAt)
                END
            FROM Reservation r WHERE r.member.id = :memberId
            """)
    List<Object[]> findRanksByMemberId(@Param("memberId") Long memberId);

    default Reservation getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new RoomEscapeException(RESOURCE_NOT_FOUND, "해당 예약을 찾을 수 없습니다. : " + id));
    }

    default ReservationWithRank getByIdWithRank(Long id) {
        Reservation reservation = findById(id)
                .orElseThrow(() -> new RoomEscapeException(RESOURCE_NOT_FOUND, "해당 예약을 찾을 수 없습니다. : " + id));
        return new ReservationWithRank(reservation, findRankById(id));
    }

    default List<ReservationWithRank> findAllByMemberIdWithRank(Long memberId) {
        Reservations reservations = new Reservations(findByMemberId(memberId));
        Map<Long, Long> rankMap = findRanksByMemberId(memberId).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
        return reservations.withRanks(rankMap);
    }
}
