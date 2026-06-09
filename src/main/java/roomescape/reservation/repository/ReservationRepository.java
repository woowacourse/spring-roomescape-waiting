package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import roomescape.global.exception.ConflictException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.exception.ReservationErrorCode;

@Repository
public class ReservationRepository {

    private final ReservationDao reservationDao;

    public ReservationRepository(ReservationDao reservationDao) {
        this.reservationDao = reservationDao;
    }

    public Reservation save(Reservation reservation) {
        try {
            if (reservation.getId() == null) {
                return reservationDao.save(reservation);
            }
            reservationDao.update(reservation);
            return reservation;
        } catch (DuplicateKeyException e) {
            throw new ConflictException(ReservationErrorCode.DUPLICATE_RESERVATION);
        }
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

    public boolean hasBookingAtSameTime(String name, ReservationSlot reservationSlot) {
        return reservationDao.existsByDateAndTimeIdAndName(
                reservationSlot.date(),
                reservationSlot.time().getId(),
                name
        );
    }

    public boolean isAlreadyBookedByOthers(Long id, String name, ReservationSlot reservationSlot) {
        return reservationDao.existsByDateAndTimeIdAndNameAndIdNot(
                reservationSlot.date(),
                reservationSlot.time().getId(),
                name,
                id
        );
    }
}
