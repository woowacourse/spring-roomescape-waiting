package roomescape.service;

import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.domain.repository.ReservationSlotRepository;
import roomescape.dto.AdminReservationResponse;

@Service
public class AdminReservationService {

    private final ReservationSlotRepository reservationSlotRepository;

    public AdminReservationService(ReservationSlotRepository reservationSlotRepository) {
        this.reservationSlotRepository = reservationSlotRepository;
    }

    public List<AdminReservationResponse> getAllReservations() {
        return reservationSlotRepository.findAll().stream()
                .map(AdminReservationResponse::from)
                .toList();
    }
}
