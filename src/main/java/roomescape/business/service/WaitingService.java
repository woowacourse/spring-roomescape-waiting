package roomescape.business.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import roomescape.business.dto.ReservationDto;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.repository.ReservationRepository;
import roomescape.business.model.vo.ReservationStatus;
import roomescape.presentation.dto.response.ReservationResponse;

@Service
@AllArgsConstructor
public class WaitingService {

    private final ReservationRepository reservationRepository;

    @Async
    public void updateWaitingReservations(Reservation reservation) {
        reservationRepository.updateWaitingReservations(reservation.getDate(), reservation.getTime(), reservation.getTheme());
    }

    public List<ReservationResponse> getAllWaitingReservations() {
        return ReservationResponse.from(
                ReservationDto.fromEntities(
                        reservationRepository.findAllReservationWithFilter(null, null, null, null, ReservationStatus.WAITING)
                )
        );
    }
}

