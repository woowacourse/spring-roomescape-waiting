package roomescape.reservation.model.repository;

import java.util.List;
import java.util.Optional;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.repository.dto.ReservationWaitingWithRank;
import roomescape.reservation.model.vo.Schedule;

public interface ReservationWaitingRepository {

    ReservationWaiting save(ReservationWaiting reservationWaiting);

    List<ReservationWaitingWithRank> findAllWithRankByMemberId(Long memberId);

    ReservationWaiting getById(Long reservationWaitingId);

    Optional<ReservationWaiting> findFirstPendingBySchedule(Schedule schedule);

    List<ReservationWaiting> getAll();

    boolean existsByScheduleAndMemberId(Schedule schedule, Long memberId);
}
