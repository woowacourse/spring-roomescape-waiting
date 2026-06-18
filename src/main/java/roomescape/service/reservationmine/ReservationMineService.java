package roomescape.service.reservationmine;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.controller.reservation.dto.ReservationResponse;
import roomescape.domain.reservation.ReservationName;
import roomescape.repository.reservation.ReservationRepository;

@Service
public class ReservationMineService {

    private final ReservationRepository reservationRepository;

    public ReservationMineService(final ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationResponse> getAllByName(final String name) {
        if (name == null || name.isBlank()) {
            return List.of();
        }

        return reservationRepository.findByName(ReservationName.from(name)).stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
