package roomescape.service.booking.reservation;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dto.reservation.ReservationFilter;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.UserReservationResponse;
import roomescape.service.booking.reservation.module.ReservationCancelService;
import roomescape.service.booking.reservation.module.ReservationResisterService;
import roomescape.service.booking.reservation.module.ReservationSearchService;

@Service
public class ReservationService {

    private final ReservationCancelService reservationCancelService;
    private final ReservationResisterService reservationResisterService;
    private final ReservationSearchService reservationSearchService;

    public ReservationService(ReservationCancelService reservationCancelService,
                              ReservationResisterService reservationResisterService,
                              ReservationSearchService reservationSearchService
    ) {
        this.reservationCancelService = reservationCancelService;
        this.reservationResisterService = reservationResisterService;
        this.reservationSearchService = reservationSearchService;
    }

    public Long resisterReservation(ReservationRequest request) {
        return reservationResisterService.resisterReservation(request);
    }

    public ReservationResponse findReservation(Long reservationId) {
        return reservationSearchService.findReservation(reservationId);
    }

    public List<ReservationResponse> findAllReservations() {
        return reservationSearchService.findAllReservations();
    }

    public List<UserReservationResponse> findReservationByMemberId(Long memberId) {
        return reservationSearchService.findReservationByMemberId(memberId);
    }

    public List<ReservationResponse> findReservationsByFilter(ReservationFilter filter) {
        return reservationSearchService.findReservationsByFilter(filter);
    }

    public void deleteReservation(Long reservationId) {
        reservationCancelService.deleteReservation(reservationId);
    }
}
