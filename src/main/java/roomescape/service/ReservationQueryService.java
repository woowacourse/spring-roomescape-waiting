package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.repository.ReservationDao;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationQueryService {

    private final ReservationDao reservationDao;

    public List<Reservation> getAllReservations() {
        return reservationDao.findAll();
    }

    public List<Reservation> getByName(String name) {
        return reservationDao.findAllByName(new Member(name));
    }
}
