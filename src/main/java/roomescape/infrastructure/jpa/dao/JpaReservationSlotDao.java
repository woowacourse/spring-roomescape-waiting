package roomescape.infrastructure.jpa.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;

import java.util.Optional;

public interface JpaReservationSlotDao extends JpaRepository<ReservationSlot, Id> {

    Optional<ReservationSlot> findByReservationDateAndReservationTime_IdAndTheme_Id(ReservationDate reservationDate, Id reservationTimeId, Id themeId);
}
