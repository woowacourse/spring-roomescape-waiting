package roomescape.business.model.repository;

import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.entity.User;
import roomescape.business.model.vo.Id;

import java.util.Optional;

public interface Reservations {

    void save(Reservation reservation);

    Optional<Reservation> findById(Id id);

    boolean existByTimeId(Id timeId);

    boolean existByThemeId(Id themeId);

    boolean isSlotFreeFor(ReservationSlot slot, User user);

    void deleteById(Id reservationId);
}
