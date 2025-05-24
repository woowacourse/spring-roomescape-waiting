package roomescape.infrastructure.jpa;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.Reservations;
import roomescape.business.model.vo.Id;
import roomescape.infrastructure.jpa.dao.JpaReservationDao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class JpaReservations implements Reservations {

    private final JpaReservationDao dao;

    public JpaReservations(JpaReservationDao dao) {
        this.dao = dao;
    }

    @Override
    public void save(Reservation reservation) {
        dao.save(reservation);
    }

    @Override
    public List<Reservation> findAllWithFilter(Id themeId, Id memberId, LocalDate dateFrom, LocalDate dateTo) {
        return dao.findAllWithFilter(themeId, memberId, dateFrom, dateTo);
    }

    @Override
    public Optional<Reservation> findById(Id id) {
        return dao.findById(id);
    }

    @Override
    public boolean existByTimeId(Id timeId) {
        return dao.existsBySlotTimeId(timeId);
    }

    @Override
    public boolean existByThemeId(Id themeId) {
        return dao.existsBySlotThemeId(themeId);
    }

    @Override
    public boolean isSlotFreeFor(ReservationSlot slot, User user) {
        return !dao.existsBySlotAndUser(slot, user);
    }

    @Override
    public void deleteById(Id reservationId) {
        dao.deleteById(reservationId);
    }
}
