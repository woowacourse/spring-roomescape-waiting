package roomescape.infrastructure.jpa.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.entity.User;
import roomescape.business.model.vo.Id;

public interface JpaReservationDao extends JpaRepository<Reservation, Id> {

    boolean existsBySlotTimeId(Id timeId);

    boolean existsBySlotThemeId(Id themeId);

    boolean existsBySlotAndUser(ReservationSlot slot, User user);
}
