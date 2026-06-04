package roomescape.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.domain.reservation.Reservation;

@Component
public class ReservationRejectLogger {
    private final ReservationDao reservationDao;

    public ReservationRejectLogger(ReservationDao reservationDao) {
        this.reservationDao = reservationDao;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logRejection(Reservation rejected) {
        reservationDao.save(rejected);
    }
}
