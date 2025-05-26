package roomescape.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.Reservations;
import roomescape.business.model.vo.Id;
import roomescape.infrastructure.repository.dao.JpaReservationDao;

import java.util.Optional;

@Primary
@Repository
@RequiredArgsConstructor
public class JpaReservations implements Reservations {

    private final JpaReservationDao dao;

    @Override
    public void save(Reservation reservation) {
        dao.save(reservation);
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
