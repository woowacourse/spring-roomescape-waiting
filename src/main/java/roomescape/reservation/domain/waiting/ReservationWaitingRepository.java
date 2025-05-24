package roomescape.reservation.domain.waiting;

import java.util.List;

public interface ReservationWaitingRepository {

    boolean existsByReservationIdAndMemberId(long reservationId, long memberId);

    ReservationWaiting save(ReservationWaiting reservationWaiting);

    void deleteById(long id);

    List<ReservationWaitingWithRank> findAllWithRankByMemberId(long memberId);
}
