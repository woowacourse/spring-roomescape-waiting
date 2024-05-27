package roomescape.reservation.repository;

import roomescape.reservation.model.ReservationWaitingWithOrder;

import java.util.List;

public interface CustomReservationWaitingRepository {
    List<ReservationWaitingWithOrder> findAllReservationWaitingWithOrdersByMemberId(Long memberId);
}
