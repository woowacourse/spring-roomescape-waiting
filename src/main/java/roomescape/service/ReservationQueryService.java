package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.repository.ReservationDao;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationQueryService {

    private final ReservationDao reservationDao;

    public List<Reservation> getAllReservations() {
        return reservationDao.findAllReservations()
                .stream()
                .toList();
    }

    public List<Reservation> getByName(String name) {
        return reservationDao.findByName(name);
    }
}
