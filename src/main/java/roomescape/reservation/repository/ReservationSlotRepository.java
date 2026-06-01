package roomescape.reservation.repository;

import roomescape.reservation.domain.ReservationSlot;

public interface ReservationSlotRepository {

    void saveIfAbsent(ReservationSlot reservationSlot);

    void lockByDateTimeAndThemeId(Long dateId, Long timeId, Long themeId);

}
