package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.Optional;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;

public interface ReservationSlotRepository {
    Long save(LocalDate date, Long timeId, Long themeId);
    Reservation saveReservation(Reservation reservation);
    ReservationSlot findById(Long id);
    ReservationSlot findByReservationId(Long reservationId);
    boolean existsByNameAndReservationSlot(Long reservationSlotId, String name);
    Optional<Long> findIdByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
    void updateReservation(Reservation reservation);
}
