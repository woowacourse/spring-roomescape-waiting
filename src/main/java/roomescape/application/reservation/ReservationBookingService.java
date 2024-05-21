package roomescape.application.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.domain.reservation.BookStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationStatusRepository;
import roomescape.exception.UnAuthorizedException;

@Service
public class ReservationBookingService {
    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final ReservationStatusRepository reservationStatusRepository;

    public ReservationBookingService(ReservationService reservationService,
                                     ReservationRepository reservationRepository,
                                     ReservationStatusRepository reservationStatusRepository) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
        this.reservationStatusRepository = reservationStatusRepository;
    }

    @Transactional
    public ReservationResponse bookReservation(ReservationRequest request) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                request.date(), request.timeId(), request.themeId())) {
            throw new IllegalArgumentException("이미 존재하는 예약입니다.");
        }
        Reservation reservation = reservationService.create(request);
        ReservationStatus reservationStatus = new ReservationStatus(reservation, BookStatus.BOOKED);
        reservationStatusRepository.save(reservationStatus);
        return ReservationResponse.from(reservation);
    }

    @Transactional
    public void cancelReservation(long memberId, long id) {
        if (reservationService.hasNoAccessToReservation(memberId, id)) {
            throw new UnAuthorizedException();
        }
        ReservationStatus reservationStatus = reservationStatusRepository.getById(id);
        reservationStatus.cancelBooking();
    }
}
