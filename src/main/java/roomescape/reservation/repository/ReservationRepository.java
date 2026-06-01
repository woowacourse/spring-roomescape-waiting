package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.dto.ReservationWithWaitingTurn;

public interface ReservationRepository {

    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    List<Reservation> findAllActiveByDateTimeAndThemeId(Long dateId, Long timeId, Long themeId);

    Reservation save(Reservation reservation);

    boolean updateStatus(Reservation reservation);

    boolean updateScheduleAndStatus(Reservation reservation);

    List<ReservationWithWaitingTurn> findMyReservationsWithWaitingTurn(String memberName);

}
