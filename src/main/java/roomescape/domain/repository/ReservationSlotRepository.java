package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationSlotInfo;

public interface ReservationSlotRepository {
    ReservationSlot findById(Long id);
    ReservationSlot findByIdForUpdate(Long reservationSlotId);
    ReservationSlot findByReservationId(Long reservationId);
    List<ReservationSlotInfo> findAll();
    Optional<Long> findIdByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
    Long save(LocalDate date, Long timeId, Long themeId);
    Reservation saveReservation(Reservation reservation);
    void updateReservation(Reservation reservation);
}
