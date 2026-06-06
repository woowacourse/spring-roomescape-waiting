package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;

@Repository
public class ReservationRepository {

    private final ReservationDao reservationDao;

    public ReservationRepository(ReservationDao reservationDao) {
        this.reservationDao = reservationDao;
    }

    public Reservation save(Reservation reservation) {
        if (reservation.getId() == null) {
            return reservationDao.save(reservation);
        }
        reservationDao.update(reservation);
        return reservation;
    }

    public List<Reservation> findAll() {
        return reservationDao.findAll();
    }

    public List<Reservation> findAllByName(String name) {
        return reservationDao.findAllByName(name);
    }

    public Optional<Reservation> findById(long id) {
        return reservationDao.findById(id);
    }

    public Optional<Reservation> findBySlot(ReservationSlot slot) {
        return reservationDao.findByDateAndTimeIdAndThemeId(
                slot.date(),
                slot.time().getId(),
                slot.theme().getId()
        );
    }

    public void delete(Reservation reservation) {
        reservationDao.delete(reservation);
    }

    public boolean hasBookingAtSameTime(Reservation reservation) {
        return reservationDao.existsByDateAndTimeIdAndName(
                reservation.getDate(),
                reservation.getTimeId(),
                reservation.getName()
        );
    }

    public boolean isAlreadyBookedByOthers(Reservation reservation) {
        return reservationDao.existsByDateAndTimeIdAndNameAndIdNot(
                reservation.getDate(),
                reservation.getTimeId(),
                reservation.getName(),
                reservation.getId()
        );
    }
}
