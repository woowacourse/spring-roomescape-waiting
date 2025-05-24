package roomescape.reservation.domain.waiting;

import java.time.LocalDate;
import java.util.List;

public interface ReservationWaitingRepository {

    boolean existsByReservationAndMemberId(LocalDate date, long timeId, long themeId, long memberId);

    ReservationWaiting save(ReservationWaiting reservationWaiting);

    void deleteById(long id);

    List<ReservationWaiting> findAll();

    List<ReservationWaitingWithRank> findAllWithRankByMemberId(long memberId);
}
