package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import roomescape.reservation.domain.ReservationWaiting;

public interface ReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {
    List<ReservationWaiting> findAllByMember_IdOrderByDetailDateAsc(Long memberId);

    Optional<ReservationWaiting> findByDetail_Id(Long id);

    List<ReservationWaiting> findAllByMember_Id(Long memberId);

    @Query("""
            SELECT r
            FROM ReservationWaiting r
            ORDER BY r.createAt DESC, r.detail.date ASC, r.detail.time.startAt ASC
            """)
    List<ReservationWaiting> findAllByOrderByDetailDateAsc();
}
