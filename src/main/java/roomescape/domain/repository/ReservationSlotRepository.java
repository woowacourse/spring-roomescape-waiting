package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.vo.ReservationSlotInfo;

public interface ReservationSlotRepository {
    ReservationSlot findByIdForUpdate(long reservationSlotId);
    ReservationSlot findByReservationIdForUpdate(long reservationId);
    List<ReservationSlotInfo> findAll();
    Long findSlotIdByReservationId(long reservationId);
    Optional<Long> findIdByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);
    Long save(LocalDate date, long timeId, long themeId);
    Reservation saveReservation(Reservation reservation);
    void updateReservation(Reservation reservation);
}
