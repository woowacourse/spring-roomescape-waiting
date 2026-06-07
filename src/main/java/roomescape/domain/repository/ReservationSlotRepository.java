package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationSlotInfo;

public interface ReservationSlotRepository {
    Long save(LocalDate date, Long timeId, Long themeId);
    Reservation saveReservation(Reservation reservation);
    ReservationSlot findById(Long id);
    ReservationSlot findByReservationId(Long reservationId);
    Optional<Long> findIdByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
    void updateReservation(Reservation reservation);
    List<ReservationSlotInfo> findAll();
}
