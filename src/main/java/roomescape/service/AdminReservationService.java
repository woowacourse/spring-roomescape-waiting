package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dto.AdminReservationResponse;
import roomescape.infrastructure.dao.ReservationSlotDao;

@Service
public class AdminReservationService {

    private final ReservationSlotDao reservationSlotDao;

    public AdminReservationService(ReservationSlotDao reservationSlotDao) {
        this.reservationSlotDao = reservationSlotDao;
    }

    public List<AdminReservationResponse> getAllReservations() {
        return reservationSlotDao.findAll().stream()
                .map(AdminReservationResponse::from)
                .toList();
    }
}
