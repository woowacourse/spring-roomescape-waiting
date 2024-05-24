package roomescape.reservation.facade;

import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginMember;
import roomescape.reservation.dto.*;
import roomescape.reservation.service.ReservationCreateService;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.WaitingReservationService;

import java.util.List;

@Service
public class ReservationFacadeService {

    private final ReservationService reservationService;
    private final ReservationCreateService reservationCreateService;
    private final WaitingReservationService waitingReservationService;

    public ReservationFacadeService(ReservationService reservationService,
                                    ReservationCreateService reservationCreateService,
                                    WaitingReservationService waitingReservationService
    ) {
        this.reservationService = reservationService;
        this.reservationCreateService = reservationCreateService;
        this.waitingReservationService = waitingReservationService;
    }

    public MemberReservationResponse createReservation(ReservationCreateRequest request) {
        return reservationCreateService.createReservation(request);
    }

    public MemberReservationResponse createReservation(MemberReservationCreateRequest request, LoginMember member) {
        ReservationCreateRequest reservationCreateRequest = ReservationCreateRequest.of(request, member);
        return reservationCreateService.createReservation(reservationCreateRequest);
    }

    public List<MemberReservationResponse> readReservations() {
        return reservationService.readReservations();
    }

    public List<MyReservationResponse> readMemberReservations(LoginMember loginMember) {
        return reservationService.readMemberReservations(loginMember);
    }

    public List<MemberReservationResponse> searchReservations(ReservationSearchRequestParameter searchCondition) {
        return reservationService.searchReservations(searchCondition);
    }

    public List<MemberReservationResponse> readWaitingReservations() {
        return waitingReservationService.readWaitingReservations();
    }

    public void confirmWaitingReservation(Long id) {
        waitingReservationService.confirmWaitingReservation(id);
    }

    public void deleteReservation(Long id) {
        reservationService.deleteReservation(id);
    }

    public void deleteReservation(Long id, LoginMember loginMember) {
        reservationService.deleteReservation(id, loginMember);
    }
}
