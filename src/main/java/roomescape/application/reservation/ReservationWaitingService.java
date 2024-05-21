package roomescape.application.reservation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.application.reservation.dto.response.ReservationWaitingResponse;
import roomescape.domain.reservation.BookStatus;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationStatusRepository;
import roomescape.exception.UnAuthorizedException;

@Service
public class ReservationWaitingService {
    private final ReservationService reservationService;
    private final ReservationStatusRepository reservationStatusRepository;

    public ReservationWaitingService(ReservationService reservationService,
                                     ReservationStatusRepository reservationStatusRepository) {
        this.reservationService = reservationService;
        this.reservationStatusRepository = reservationStatusRepository;
    }

    @Transactional
    public ReservationWaitingResponse enqueueWaitingList(ReservationRequest request) {
        Reservation reservation = reservationService.create(request);
        if (reservationStatusRepository.existsAlreadyWaitingOrBooked(reservation)) {
            throw new IllegalArgumentException("이미 예약했거나 대기한 항목입니다.");
        }
        ReservationStatus status = reservationStatusRepository.save(
                new ReservationStatus(reservation, BookStatus.WAITING)
        );
        long waitingCount = reservationStatusRepository.getWaitingCount(status.getReservation());
        return new ReservationWaitingResponse(
                ReservationResponse.from(reservation),
                waitingCount
        );
    }

    @Transactional
    public void cancelWaitingList(long memberId, long id) {
        ReservationStatus reservationStatus = reservationStatusRepository.getById(id);
        if (reservationService.hasNoAccessToReservation(memberId, id)) {
            throw new UnAuthorizedException();
        }
        reservationStatus.cancelWaiting();
        reservationStatusRepository.findFirstWaiting(reservationStatus.getReservation())
                .ifPresent(ReservationStatus::book);
    }
}
