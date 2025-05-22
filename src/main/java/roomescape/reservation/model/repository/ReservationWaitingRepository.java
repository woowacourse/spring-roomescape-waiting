package roomescape.reservation.model.repository;

import java.util.List;
import roomescape.reservation.model.entity.ReservationWaiting;
import roomescape.reservation.model.repository.dto.ReservationWaitingWithRank;

public interface ReservationWaitingRepository {

    void save(ReservationWaiting reservationWaiting);

    List<ReservationWaitingWithRank> findAllWithRankByMemberId(Long memberId);

    ReservationWaiting getById(Long reservationWaitingId);

    void remove(ReservationWaiting reservationWaiting);
}
