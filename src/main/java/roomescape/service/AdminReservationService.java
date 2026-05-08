package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dto.AdminReservationResponse;
import roomescape.repository.ReservationDao;

@Service
public class AdminReservationService {

    private final ReservationDao reservationDao;

    public AdminReservationService(ReservationDao reservationDao) {
        this.reservationDao = reservationDao;
    }

    public List<AdminReservationResponse> getAllReservations() {
        return reservationDao.findAll().stream()
                .map(r -> AdminReservationResponse.from(r, r.getTheme()))
                .toList();
    }
}
