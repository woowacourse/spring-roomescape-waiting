package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.vo.ReservationSlotInfo;

public interface ReservationSlotRepository {
    ReservationSlot findByIdForUpdate(Long reservationSlotId);
    ReservationSlot findByReservationIdForUpdate(Long reservationId);
    List<ReservationSlotInfo> findAll();
    Long findSlotIdByReservationId(Long reservationId);
    Optional<Long> findIdByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
    Long save(LocalDate date, Long timeId, Long themeId);
    Reservation saveReservation(Reservation reservation);
    void updateReservation(Reservation reservation);
}
