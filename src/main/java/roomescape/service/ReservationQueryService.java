package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.domain.reservation.Reservation;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationQueryService {

    private final ReservationDao reservationDao;

    public List<Reservation> getAllReservations() {
        return reservationDao.findAllReservations();
    }

    public List<Reservation> getByName(String name) {
        return reservationDao.findByName(name);
    }
}
