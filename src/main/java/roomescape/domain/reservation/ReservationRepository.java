package roomescape.domain.reservation;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.RoomEscapeException;
import roomescape.domain.member.Member;

import java.util.List;

import static roomescape.domain.DomainErrorCode.RESOURCE_NOT_FOUND;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findAllByMember(Member member);

    List<Reservation> findBySlot_Id(Long slotId);

    @EntityGraph(attributePaths = {"member", "slot", "slot.theme", "slot.time"})
    List<Reservation> findByMemberId(Long memberId);

    @Query("select r from Reservation r " +
            "join fetch r.member " +
            "join fetch r.slot s " +
            "join fetch s.theme " +
            "join fetch s.time " +
            "where r.member.id = :memberId")
    List<Reservation> findMineWithDetails(@Param("memberId") Long memberId);

    default Reservation getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new RoomEscapeException(RESOURCE_NOT_FOUND, "해당 예약을 찾을 수 없습니다. : " + id));
    }
}
