package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import roomescape.domain.reservation.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    boolean existsReservationByScheduleId(final Long scheduleId);

    List<Reservation> findByMember_Id(final Long memberId);

    boolean existsByScheduleId(Long scheduleId);

    Optional<Reservation> findByScheduleId(Long scheduleId);
}
