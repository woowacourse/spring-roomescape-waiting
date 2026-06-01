package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.dao.ReservationDao;
import roomescape.domain.reservation.Reservation;

@Service
@RequiredArgsConstructor
public class ReservationAdminCommandService {

    private final ReservationDao reservationDao;

    public void delete(Long reservationId) {
        Reservation reservation = reservationDao.findById(reservationId);
        reservationDao.delete(reservation);
    }
}
