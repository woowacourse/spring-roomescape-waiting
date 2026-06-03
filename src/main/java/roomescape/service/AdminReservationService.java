package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dto.AdminReservationResponse;
import roomescape.repository.ReservationSlotDao;

@Service
public class AdminReservationService {

    private final ReservationSlotDao reservationSlotDao;

    public AdminReservationService(ReservationSlotDao reservationSlotDao) {
        this.reservationSlotDao = reservationSlotDao;
    }

    public List<AdminReservationResponse> getAllReservations() {
        return reservationSlotDao.findAll().stream()
                .map(r -> AdminReservationResponse.from(r, r.getTheme()))
                .toList();
    }
}
