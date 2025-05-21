package roomescape.infrastructure.jpa;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.repository.ReservationSlots;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;
import roomescape.infrastructure.jpa.dao.JpaReservationSlotDao;

import java.time.LocalDate;
import java.util.Optional;

@Primary
@Repository
public class JpaReservationSlots implements ReservationSlots {

    private final JpaReservationSlotDao dao;

    public JpaReservationSlots(final JpaReservationSlotDao dao) {
        this.dao = dao;
    }

    @Override
    public void save(final ReservationSlot reservationSlot) {
        dao.save(reservationSlot);
    }

    @Override
    public Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(final LocalDate date, final Id reservationTimeId, final Id themeId) {
        return dao.findByReservationDateAndReservationTime_IdAndTheme_Id(new ReservationDate(date), reservationTimeId, themeId);
    }
}
