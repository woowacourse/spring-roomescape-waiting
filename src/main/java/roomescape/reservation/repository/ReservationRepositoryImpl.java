package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.ReservationSlot;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationDao reservationDao;

    public ReservationRepositoryImpl(ReservationDao reservationDao) {
        this.reservationDao = reservationDao;
    }

    @Override
    public Reservation save(Reservation reservation) {
        if (reservation.getId() == null) {
            return reservationDao.save(reservation);
        }
        reservationDao.update(reservation);
        return reservation;
    }

    @Override
    public List<Reservation> findAll() {
        return reservationDao.findAll();
    }

    @Override
    public List<Reservation> findAllByName(String name) {
        return reservationDao.findAllByName(name);
    }

    @Override
    public Optional<Reservation> findById(long id) {
        return reservationDao.findById(id);
    }

    @Override
    public Optional<Reservation> findBySlot(ReservationSlot slot) {
        return reservationDao.findByDateAndTimeIdAndThemeId(
                slot.date(),
                slot.time().getId(),
                slot.theme().getId()
        );
    }

    @Override
    public void delete(Reservation reservation) {
        reservationDao.delete(reservation);
    }

    @Override
    public boolean hasBookingAtSameTime(Reservation reservation) {
        return reservationDao.existsByDateAndTimeIdAndName(
                reservation.getDate(),
                reservation.getTimeId(),
                reservation.getName()
        );
    }

    @Override
    public boolean isAlreadyBookedByOthers(Reservation reservation) {
        return reservationDao.existsByDateAndTimeIdAndNameAndIdNot(
                reservation.getDate(),
                reservation.getTimeId(),
                reservation.getName(),
                reservation.getId()
        );
    }
}
