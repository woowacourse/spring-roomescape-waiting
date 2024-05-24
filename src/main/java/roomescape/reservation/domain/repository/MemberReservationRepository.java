package roomescape.reservation.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.ReservationStatus;

import java.util.List;
import java.util.Optional;

public interface MemberReservationRepository extends JpaRepository<MemberReservation, Long>,
        JpaSpecificationExecutor<MemberReservation> {

    List<MemberReservation> findAllByMember(Member member);

    List<MemberReservation> findAllByStatus(ReservationStatus status);

    Optional<MemberReservation> findFirstByReservationSlotOrderByCreatedTime(ReservationSlot reservationSlot);

    @Query("""
    SELECT count(*)
    FROM MemberReservation  mr
    WHERE mr.reservationSlot = 
    (SELECT mr.reservationSlot
    FROM MemberReservation mr
    WHERE mr.id = :memberReservationId)
    AND mr.id < :memberReservationId
    """)
    int countWaitingMemberReservation(Long memberReservationId);

    boolean existsByReservationSlot(ReservationSlot reservationSlot);

    void deleteByReservationSlot_Id(long reservationSlotId);

    boolean existsByReservationSlotAndMember(ReservationSlot reservationSlot, Member member);
}
