package roomescape.infrastructure.jpa;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationSlot;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.Reservations;
import roomescape.business.model.vo.Id;
import roomescape.infrastructure.jpa.dao.JpaReservationDao;
import roomescape.infrastructure.jpa.dao.JpaReservationSlotDao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Primary
@Repository
@RequiredArgsConstructor
public class JpaReservations implements Reservations {

    private final JpaReservationDao dao;
    private final JpaReservationSlotDao slotDao;

    @Override
    public void save(Reservation reservation) {
        dao.save(reservation);
    }

    @Override
    public List<Reservation> findAllReservedWithFilter(Id themeId, Id userId, LocalDate dateFrom, LocalDate dateTo) {
        List<ReservationSlot> slots = slotDao.findAllBy(themeId, userId, dateFrom, dateTo);
        Map<Reservation, Integer> reservations = toWaitingNumberAndReservation(slots, userId);
        return reservations.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public List<Reservation> findAllNotReserved() {
        List<ReservationSlot> slots = slotDao.findAll();
        Map<Reservation, Integer> reservations = toWaitingNumberAndReservation(slots, null);
        return reservations.entrySet().stream()
                .filter(entry -> entry.getValue() != 0)
                .map(Map.Entry::getKey)
                .toList();
    }

    private Map<Reservation, Integer> toWaitingNumberAndReservation(List<ReservationSlot> slots, Id userId) {
        if (userId == null) {
            return ReservationSlot.toWaitingNumberAndReservation(slots);
        }
        return ReservationSlot.toWaitingNumberAndReservation(slots, userId);
    }

    @Override
    public Map<Reservation, Integer> findAllWithWaitingNumberByUserId(Id userId) {
        List<ReservationSlot> slots = slotDao.findAllBy(null, userId, null, null);
        return ReservationSlot.toWaitingNumberAndReservation(slots, userId);
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
