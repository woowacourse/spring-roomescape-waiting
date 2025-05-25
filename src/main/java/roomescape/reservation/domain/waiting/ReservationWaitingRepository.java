package roomescape.reservation.domain.waiting;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationWaitingRepository {

    boolean existsByReservation(LocalDate date, long timeId, long themeId);

    boolean existsByReservationAndMemberId(LocalDate date, long timeId, long themeId, long memberId);

    ReservationWaiting save(ReservationWaiting reservationWaiting);

    void deleteById(long id);

    List<ReservationWaiting> findAll();

    List<ReservationWaitingWithRank> findAllWithRankByMemberId(long memberId);

    Optional<ReservationWaiting> findTopByReservation(LocalDate date, long timeId, long themeId);
}
