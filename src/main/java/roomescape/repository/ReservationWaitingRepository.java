package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Member;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationWaiting;

public interface ReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {

    Optional<ReservationWaiting> findFirstBySlotOrderByCreatedAt(ReservationSlot slot);

    boolean existsByMemberAndSlot(Member member, ReservationSlot slot);

    List<ReservationWaiting> findByMember_IdOrderByCreatedAt(Long memberId);

    @Query("SELECT COUNT(rw) FROM ReservationWaiting rw WHERE rw.slot = :slot AND rw.id <= :id")
    int countOrder(@Param("slot") ReservationSlot slot, @Param("id") Long id);
}
