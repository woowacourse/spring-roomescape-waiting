package roomescape.application.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;

@Service
public class ReservationBookingService {
    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;

    public ReservationBookingService(ReservationService reservationService,
                                     ReservationRepository reservationRepository) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationResponse bookReservation(ReservationRequest request) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                request.date(), request.timeId(), request.themeId())) {
            throw new IllegalArgumentException("이미 존재하는 예약입니다.");
        }
        Reservation reservation = reservationService.create(request);
        return ReservationResponse.from(reservation);
    }
}
