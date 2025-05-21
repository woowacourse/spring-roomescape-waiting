package roomescape.business.model.repository;

import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.vo.Id;

import java.time.LocalDate;
import java.util.Optional;

public interface ReservationSlots {

    Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(LocalDate date, Id reservationTimeId, Id themeId);

    void save(ReservationSlot reservationSlot);
}
