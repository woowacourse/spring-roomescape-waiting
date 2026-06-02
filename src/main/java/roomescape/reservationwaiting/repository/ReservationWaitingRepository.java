package roomescape.reservationwaiting.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.reservationwaiting.domain.ReservationWaiting;

public interface ReservationWaitingRepository {
    ReservationWaiting save(ReservationWaiting reservationWaiting);

    void deleteById(Long id);

    Map<Long, Long> calculateTurn(String name);

    List<ReservationWaiting> findByName(String name);

    Optional<ReservationWaiting> findReservationWaitingBySlot(LocalDate date, Long timeId,
                                                              Long themeId);

    boolean existsByNameAndSlot(String name, LocalDate date, Long timeId, Long themeId);

    Optional<ReservationWaiting> findReservationWaitingById(Long reservationWaitingId);
}
